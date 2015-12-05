(ns dwarven-tavern.client
  (:require [goog.dom :as gdom]
            [rum.core :as rum]
            [beicon.core :as rx]
            [dwarven-tavern.client.view.root :as v]
            [dwarven-tavern.client.view.util :as util]
            [dwarven-tavern.common.state :as st]))

(enable-console-print!)

(defonce db (atom {:width 10
                   :height 10
                   :team1 [{:id :dialelo
                            :pos [1 2]
                            :dir :south}]
                   :team2 [{:id :alotor
                            :pos [3 4]
                            :dir :south}]
                   :barrel {:pos [5 5]}}))

(defn transact!
  [db ev]
  (let [tx (st/transition @db ev)]
    (if (rx/observable? tx)
      (rx/on-value tx #(transact! db %))
      (reset! db tx))))

(defmethod st/transition :join-room
  [state [_ room-id]]
  (assoc state room-id {}))

(defmethod st/transition :create-room
  [state [_ name]]
  (rx/from-coll [[:new-room {:width 10
                             :height 10
                             :team1 []
                             :team2 []
                             :barrel {:pos [5 5]}}]]))

(defmethod st/transition :new-room
  [state [_ room]]
  (update state :rooms (fnil merge {}) room))

#_(transact! db [:join-room "foo"])
#_(transact! db [:create-room "foobar"])

(let [state (util/focus db)]
  (rum/mount (v/root {:state state
                      :signal (partial transact! db)})
             (gdom/getElement "app")))
