(ns dwarven-tavern.client.state
  (:require [beicon.core :as rx]
            [promesa.core :as prom]))

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
    (cond
      (rx/observable? tx)
      (rx/on-value tx #(transact! db %))

      (prom/promise? tx)
      (prom/then tx #(transact! db %))

      :else
      (reset! db tx))))

(defn move
  [[x y] direction]
  (case direction
    :north [x (dec y)]
    :south [x (inc y)]
    :west  [(dec x) y]
    :east  [(inc x) y]))

(defn clamp
  [[x y] width height]
  [(max (min x (dec width)) 0)
   (max (min y (dec height)) 0)])