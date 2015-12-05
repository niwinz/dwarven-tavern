(ns dwarven-tavern.client
  (:require [goog.dom :as gdom]
            [rum.core :as rum]
            [promesa.core :as prom]
            [beicon.core :as rx]
            [dwarven-tavern.client.view.root :as v]
            [dwarven-tavern.client.postal :as p]
            [dwarven-tavern.client.state :as st]
            [dwarven-tavern.client.view.util :as util]))

(enable-console-print!)

(defonce db (atom st/initial-state))

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

(let [state (util/focus db)]
  (rum/mount (v/root {:state state
                      :signal (partial st/transact! db)})
             (gdom/getElement "app")))
