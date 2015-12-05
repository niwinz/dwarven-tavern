(ns dwarven-tavern.client.postal
  (:require [postal.client :as postal]
            [cats.core :as m]
            [promesa.core :as prom]
            [beicon.core :as rx]
            [dwarven-tavern.client.rx :refer [from-promise]]))

(def client (postal/client "http://localhost:5050/api"))

(defn join-room
  [room]
  (postal/novelty client :join {:player :me :room room}))

(defn start-game
  [room]
  (postal/novelty client :start {:player :me :room room}))

(defn subscribe-to-room
  [room]
  (postal/subscribe client :game {:player :me :room room}))

(defn get-room-list
  []
  (prom/then (postal/query client :rooms) #(:data %)))

(defn play-in-room
  [room]
  (rx/flat-map
   #(subscribe-to-room room)
   (from-promise (join-room room))))
