(ns dwarven-tavern.client.state
  (:require [beicon.core :as rx]
            [promesa.core :as prom]))

(def initial-state
  {:player :dialelo
   :location :home
   :room-list [{:id "Room1"
                :players 2
                :max 4
                :status :pending}
               {:id "Room2"
                :players 2
                :max 10
                :status :pending}
               {:id "Room3"
                :players 4
                :max 4
                :status :pending}]
   :current-game {:room "Room 1"
                  :width 10
                  :height 10
                  :total-time 10
                  :time-progress 9
                  :players {:dialelo {:pos [1 2]
                                      :dir :south}
                            :alotor {:pos [3 4]
                                     :dir :north}}
                  :team1 {:score 1
                          :members [:dialelo]}
                  :team2 {:score 2
                          :members [:alotor]}
                  :barrel {:pos [5 5]
                           :dir :south}}})

(defmulti transition (fn [state [ev]] ev))

(defn transact!
  [db ev]
  (let [tx (transition @db (second ev))]
    (cond
      (rx/observable? tx)
      (rx/on-value tx #(transact! db (second %)))

      (prom/promise? tx)
      (prom/then tx #(transact! db (second %)))

      :else
      (reset! db tx))))
