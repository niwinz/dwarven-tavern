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

(defn signal
  "Emits an event."
  [event]
  (rx/push! bus event))

(def ^:static bus (rx/bus))

(defn init-loop
  "Initializes the event loop."
  [state on-error]
  (let [update-s (rx/filter update? bus)
        watch-s  (rx/filter watch? bus)
        effect-s (rx/filter effect? bus)
        model-s (rx/merge (rx/scan #(-apply-update %2 %1) @state update-s)
                          (rx/from-atom state))]

    ;; Process effects: combine with the latest model to process the new effect
    (-> (rx/with-latest-from effect-s model-s vector)
        (rx/subscribe (fn [[event model]] (-apply-effect event model))))

    ;; Process event sources: combine with the latest model and the result will be
    ;; pushed to the event-stream bus
    (as-> (rx/with-latest-from effect-s model-s vector) $
      (rx/flat-map (fn [[event model]] (-apply-watch event model)) $)
      (rx/subscribe $ signal on-error))

    model-s))

;; (def initial-state
;;   {:player :dialelo
;;    :location :home
;;    :room-list []
;;    :current-game {:room "Room 1"
;;                   :width 10
;;                   :height 10
;;                   :total-time 10
;;                   :time-progress 9
;;                   :players {:dialelo {:pos [1 2]
;;                                       :dir :south}
;;                             :alotor {:pos [3 4]
;;                                      :dir :north}}
;;                   :team1 {:score 1
;;                           :members [:dialelo]}
;;                   :team2 {:score 2
;;                           :members [:alotor]}
;;                   :barrel {:pos [5 5]
;;                            :dir :south}}})

;; (defmulti transition (fn [state [ev]] ev))

;; (defn transact!
;;   [db ev]
;;   (let [tx (transition @db ev)]
;;     (println "Transition: " tx)
;;     (cond
;;       (rx/observable? tx)
;;       (rx/on-value tx (fn [acc]
;;                         (println acc)
;;                         (transact! db acc)))

;;       (prom/promise? tx)
;;       (prom/then tx #(transact! db %))

;;       :else
;;       (reset! db tx))))
