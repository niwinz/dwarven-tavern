(ns dwarven-tavern.client
  (:require [goog.dom :as gdom]
            [rum.core :as rum]
            [promesa.core :as prom]
            [beicon.core :as rx]
            [dwarven-tavern.client.view.root :as v]
            [dwarven-tavern.client.postal :as p]
            [dwarven-tavern.client.view.util :as util]))

(enable-console-print!)

(defonce db (atom {:width 10
                   :height 10
                   :total-time 10
                   :time-progress 9
                   :team1 [{:id :dialelo
                            :pos [1 2]
                            :dir :south
                            :score 1}]
                   :team2 [{:id :alotor
                            :pos [3 4]
                            :dir :north
                            :score 2}]
                   :barrel {:pos [5 5]
                            :dir :north}}))

(defmulti transition (fn [state [ev]] ev))

(defn transact!
  [db ev]
  (let [tx (transition @db ev)]
    (if (rx/observable? tx)
      (rx/on-value tx #(transact! db %))
      (reset! db tx))))

(defmethod transition :join-room
  [state [_ room-id]]
  (assoc state room-id {}))

(defmethod transition :create-room
  [state [_ name]]
  (rx/from-coll [[:new-room {:width 10
                             :height 10
                             :team1 []
                             :team2 []
                             :barrel {:pos [5 5]}}]]))

(defmethod transition :new-room
  [state [_ room]]
  (update state :rooms (fnil merge {}) room))

(rx/subscribe
  (p/play-in-room :foo)
  #(println :joined-room %)
  #(println :failed-to-join %)
  #(println :fin))

(let [state (util/focus db)]
  (rum/mount (v/root {:state state
                      :signal (partial transact! db)})
             (gdom/getElement "app")))
