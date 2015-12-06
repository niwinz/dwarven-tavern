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

(def ^:static bus (rx/bus))

(defn emit!
  "Emits an event."
  [event]
  (rx/push! bus event))

(defn init
  "Initializes the stream event loop and
  return a stream with model changes."
  [initial-state]
  (let [update-s (rx/filter update? bus)
        watch-s  (rx/filter watch? bus)
        effect-s (rx/filter effect? bus)
        model-s (rx/scan #(-apply-update %2 %1) initial-state update-s)]

    ;; Process effects: combine with the latest model to process the new effect
    (-> (rx/with-latest-from vector effect-s model-s)
        (rx/subscribe (fn [[event model]] (-apply-effect event model))))

    ;; Process event sources: combine with the latest model and the result will be
    ;; pushed to the event-stream bus
    (as-> (rx/with-latest-from vector watch-s model-s) $
      (rx/flat-map (fn [[event model]] (-apply-watch event model)) $)
      (rx/on-value $ emit!))

    model-s))
