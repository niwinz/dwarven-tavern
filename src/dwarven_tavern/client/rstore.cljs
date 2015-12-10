(ns dwarven-tavern.client.rstore
  "Reactive storage management architecture helpers."
  (:require [beicon.core :as rx]
            [promesa.core :as prom]))

;; An abstraction for implement a simple state
;; transition. The `-apply-update` function receives
;; the state and shoudl return the transformed state.

(defprotocol UpdateEvent
  (-apply-update [event state]))

;; An abstraction for perform some async stuff such
;; as communicate with api rest or other resources
;; that implies asynchronous access.
;; The `-apply-watch` receives the state and should
;; return a reactive stream of events (that can be
;; of `UpdateEvent`, `WatchEvent` or `EffectEvent`.

(defprotocol WatchEvent
  (-apply-watch [event state]))

;; An abstraction for perform just side effects. It
;; receives state and its return value is completly
;; ignored.

(defprotocol EffectEvent
  (-apply-effect [event state]))

(defn update?
  "Return `true` when `e` satisfies
  the UpdateEvent protocol."
  [e]
  (satisfies? UpdateEvent e))

(defn watch?
  "Return `true` when `e` satisfies
  the WatchEvent protocol."
  [e]
  (satisfies? WatchEvent e))

(defn effect?
  "Return `true` when `e` satisfies
  the EffectEvent protocol."
  [e]
  (satisfies? EffectEvent e))

(defonce ^:static bus (rx/bus))

(defn emit!
  "Emits an event or a collection of them.
  The order of events does not matters."
  ([event]
   (rx/push! bus event))
  ([event & events]
   (run! #(rx/push! bus %) (into [event] events))))

(defn swap-state
  "A helper for just apply some function to state
  without a need to declare additional event."
  [f]
  (reify
    rs/UpdateEvent
    (-apply-update [_ state]
      (f state))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:rstore/swap-state>"))))

(defn reset-state
  "A event that resets the internal state with
  the provided value."
  [state]
  (reify
    UpdateEvent
    (-apply-update [_ _]
      state)

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:rstore/reset-state>"))))

(defn init
  "Initializes the stream event loop and
  return a stream with model changes."
  [state]
  (let [update-s (rx/filter update? bus)
        watch-s  (rx/filter watch? bus)
        effect-s (rx/filter effect? bus)
        state-s (->> update-s
                     (rx/scan #(-apply-update %2 %1) state)
                     (rx/share))]

    ;; Process effects: combine with the latest model to process the new effect
    (-> (rx/with-latest-from vector state-s effect-s)
        (rx/subscribe (fn [[event model]] (-apply-effect event model))))

    ;; Process event sources: combine with the latest model and the result will be
    ;; pushed to the event-stream bus
    (as-> (rx/with-latest-from vector state-s watch-s) $
      (rx/flat-map (fn [[event model]] (-apply-watch event model)) $)
      (rx/merge-all $)
      (rx/on-value $ emit!))

    ;; Initialize the stream machinary with initial state.
    (emit! (reset-state state))

    state-s))

(comment
  ;; Import all dependencies.
  (require '[somewhere.rstore :as rs])
  (require '[beicon.core :as rx])

  ;; Creates the stream loop with the initial state.
  ;; The state now, lives as reactive stream and
  ;; only can be modified emiting events. The returned
  ;; stream will emit the state snapshots.

  (defn initial-state
    []
    {:msg "hello world"})

  (defonce stream (rt/init (initial-state)))

  ;; You can materialize the last state into
  ;; the atom for using it in the same way as
  ;; you have done before using any of the
  ;; react wrappers like rum, reagent or om.

  (defonce state (atom nil))
  (rx/to-atom stream state)

  ;; You can reinitialize the state completly
  ;; emiting an already provided defined event:

  (let [state (initial-state)]
    (rs/emit! (reset-state state)))

  ;; You can transform the state in the same
  ;; way as you can do it with a plain atom
  ;; with `swap-state` event:

  (rs/emit! (swap-state #(assoc % :foo "bar")))

  )
