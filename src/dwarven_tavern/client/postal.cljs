(ns dwarven-tavern.client.postal
  (:require [postal.client :as postal]
            [cats.core :as m]
            [promesa.core :as prom]
            [beicon.core :as rx]))

(def client (postal/client "http://localhost:5050/api"))

(defn- promise->observable
  [p]
  (rx/create (fn [sink]
                (prom/branch p sink #(sink (js/Error. %))))))

(defn join-room
  [room]
  (postal/novelty client :join {:player :me :room room}))

(defn start-game
  [room]
  (postal/novelty client :start {:player :me :room room}))

(defn subscribe-to-room
  [room]
  (postal/subscribe client :game {:player :me :room room}))

(defn play-in-room
  [room]
  (rx/flat-map
   #(subscribe-to-room :foo)
   (promise->observable
    (prom/then (join-room :foo) #(start-game :foo)))))
