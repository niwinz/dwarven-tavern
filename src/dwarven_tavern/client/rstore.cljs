(ns dwarven-tavern.client.rstore
  "Reactive storage management architecture helpers."
  (:require [beicon.core :as rx]
            [promesa.core :as prom]))

(defprotocol UpdateEvent
  ;; Event -> Model -> Model
  (-apply-update [event model]))

(defprotocol WatchEvent
  ;; Event -> Model -> Stream[Event]
  (-apply-watch [event model]))

(defprotocol EffectEvent
  ;; Event -> Model -> nil
  (-apply-effect [event model]))

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
  "Emits an event."
  ([event]
   (rx/push! bus event))
  ([event & events]
   (run! #(rx/push! bus %) (into [event] events))))

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
      (-write writer "#<event:state/reset-state>"))))

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
      (rx/on-value $ emit!))

    ;; Initialize the stream machinary with initial state.
    (emit! (reset-state state))

    state-s))
