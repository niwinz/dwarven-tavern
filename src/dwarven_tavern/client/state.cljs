(ns dwarven-tavern.client.state
  (:require [beicon.core :as rx]))

(def initial-state
  {:width 10
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
            :dir :south}})

(defmulti transition (fn [state [ev]] ev))

(defn transact!
  [db ev]
  (let [tx (transition @db ev)]
    (if (rx/observable? tx)
      (rx/on-value tx #(transact! db %))
      (reset! db tx))))


