(ns dwarven-tavern.client
  (:require [goog.dom :as gdom]
            [cats.core :as m]
            [rum.core :as rum]
            [promesa.core :as prom]
            [beicon.core :as rx]
            [bidi.router :as bidi]
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


(defmethod st/transition :move
  [state [_ {:keys [room direction]}]]
  ;; TODO
  state)

(defmethod st/transition :room-list
  [state [_ rooms]]
  (assoc state :room-list rooms))

(defn fetch-rooms!
  [db]
  (m/mlet [rooms (p/get-room-list)]
    (st/transact! db [:room-list rooms])))

(defn render!
  [element db]
  (rum/mount (v/root {:state db
                      :signal (partial st/transact! db)})
             element))

(defn init
  [db]
  (fetch-rooms! db)
  (render! (gdom/getElement "app") db))

(init db)

(comment
  (in-ns 'dwarven-tavern.client)
  (fetch-rooms! db)
  (:rooms @db)
  )


(bidi/start-router! ["/" {"home" :home
                          ["game/" :id] :game}]
                    {:on-navigate (fn [location]
                                    (println location)
                                    (swap! db assoc :location (:handler location)))
                     :default-location {:handler :home}})
