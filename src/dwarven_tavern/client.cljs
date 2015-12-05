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

(defonce router
  (bidi/start-router!
   ["/" {"home"  :home
         "rooms" :rooms
         ["game/" :id] :game}]
   {:on-navigate (fn [location]
                   (println location)
                   (swap! db assoc :location (:handler location)))
    :default-location {:handler :home}}))

(defmethod st/transition :default
  [state _]
  state)

(defmethod st/transition :join-room
  [{:keys [player]} room]
  (rx/map (p/play-in-room player room)
          (fn [msg]
            ;; TODO: process incoming messages
            [:noop])))

(defmethod st/transition :start-game
  [_ room]
  (prom/then (p/start-game room)
             (fn [game]
               ;; TODO: assoc current game
               [:noop])))

(defmethod st/transition :new-room
  [state room]
  ;; TODO: tell the server
  (update state :rooms (fnil merge {}) room))

(defmethod st/transition :room-list
  [state rooms]
  (assoc state :room-list rooms))

(defmethod st/transition :move
  [state {:keys [room direction]}]
  (let [player (:player state)]
    (prom/then (p/move player room direction)
               (fn [response]
                 [:noop]))))

(defn fetch-rooms!
  [db]
  (m/mlet [rooms (p/get-room-list)]
    (st/transact! db [:room-list rooms])))

(defn render!
  [element db]
  (rum/mount (v/root {:state db
                      :signal (partial st/transact! db)
                      :router router})
             element))

(defn start-router!
  [db]
  (bidi/start-router! ["/" {"home" :home
                            "rooms" :rooms
                            ["game/" :id] :game}]
                      {:on-navigate (fn [location]
                                      (swap! db assoc :location (:handler location)))
                       :default-location {:handler :home}}))

(defn init
  [db]
  (fetch-rooms! db)
  (start-router! db)
  (render! (gdom/getElement "app") db))

(init db)

(comment
  (in-ns 'dwarven-tavern.client)
  (fetch-rooms! db)
  (:rooms @db)
  )



