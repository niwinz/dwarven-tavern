(ns dwarven-tavern.client.state
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
  [event]
  (println "EMIT:" event)
  (rx/push! bus event))

(defn- identity-event
  []
  (reify
    UpdateEvent
    (-apply-update [_ state] state)

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:state/identity>"))))


(defn init
  "Initializes the stream event loop and
  return a stream with model changes."
  [state]
  (let [update-s (rx/filter update? bus)
        update-s (rx/tap (fn [item] (println "DEBUG:" item)) update-s)
        watch-s  (rx/filter watch? bus)
        effect-s (rx/filter effect? bus)
        model-s (->> update-s
                     (rx/scan #(do
                                 (println 111 %2 %1 %3)
                                 (let [r (-apply-update %2 %1)]
                                   (println 222 r)
                                   r))
                              state))]

    ;; Process effects: combine with the latest model to process the new effect
    (-> (rx/with-latest-from vector model-s effect-s)
        (rx/subscribe (fn [[event model]] (-apply-effect event model))))

    ;; Process event sources: combine with the latest model and the result will be
    ;; pushed to the event-stream bus
    (as-> (rx/with-latest-from vector model-s watch-s) $
      (rx/flat-map (fn [[event model]] (-apply-watch event model)) $)
      (rx/on-value $ (fn [event]
                       (println event)
                       (emit! event))))
    (emit! (identity-event))

    model-s))
