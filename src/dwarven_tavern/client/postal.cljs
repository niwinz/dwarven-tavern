(ns dwarven-tavern.client.postal
  (:require [postal.client :as postal]
            [cats.core :as m]
            [promesa.core :as prom]
            [beicon.core :as rx]
            [dwarven-tavern.client.rx :refer [from-promise]]))

(def client (postal/client "http://localhost:5050/api"))

(defn get-room-list
  []
  (prom/then (postal/query client :rooms) #(:data %)))

(defn join-room
  [player room]
  (postal/novelty client :join {:player player
                                :room room}))

(defn start-game
  [room]
  (postal/novelty client :start {:room room}))

(defn subscribe-to-room
  [room]
  (postal/subscribe client :game {:room room}))

(defn play-in-room
  [player room]
  (rx/flat-map
   #(subscribe-to-room room)
   (from-promise (join-room player room))))

(defn move
  [player room direction]
  (postal/novelty client :movement {:player player
                                    :room room
                                    :dir direction}))


