(ns dwarven-tavern.client.postal
  (:require [postal.client :as postal]
            [cats.core :as m]
            [beicon.core :as rx]
            [promesa.core :as prom]))

(def client (postal/client "http://localhost:5050/api"))

(defn get-room-list
  []
  (postal/query client :room/list))

(defn join-room
  [player room]
  (postal/novelty client :room/join {:player player
                                     :room room}))

;; (defn start-game
;;   [room]
;;   (postal/novelty client :start {:room room}))

;; (defn subscribe-to-room
;;   [room]
;;   (postal/subscribe client :game/events {:room room :player :unknown}))

;; (defn play-in-room
;;   [player room]
;;   (rx/flat-map #(subscribe-to-room room)
;;                (rx/from-promise (join-room player room))))

;; (defn move
;;   [player room direction]
;;   (postal/novelty client :movement {:player player
;;                                     :room room
;;                                     :dir direction}))


