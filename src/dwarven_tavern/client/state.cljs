(ns dwarven-tavern.client.state
  (:require [beicon.core :as rx]
            [promesa.core :as prom]))

(def initial-state
  {:location :home
   :room-list [{:id "Room 1"
                :players 2
                :max 4
                :joinable true}
               {:id "Room 2"
                :players 2
                :max 10
                :joinable true}
               {:id "Room 3"
                :players 4
                :max 4
                :joinable true}]
   :current-game {:width 10
                  :height 10
                  :total-time 10
                  :time-progress 9
                  :team1 {:score 1
                          :members [{:id :dialelo
                                     :pos [1 2]
                                     :dir :south}]}
                  :team2 {:score 2
                          :members [{:id :alotor
                                     :pos [3 4]
                                     :dir :north}]}
                  :barrel {:pos [5 5]
                           :dir :south}}})

(defmulti transition (fn [state [ev]] ev))

(defn transact!
  [db ev]
  (let [tx (transition @db ev)]
    (cond
      (rx/observable? tx)
      (rx/on-value tx #(transact! db %))

      (prom/promise? tx)
      (prom/then tx #(transact! db %))

      :else
      (reset! db tx))))
