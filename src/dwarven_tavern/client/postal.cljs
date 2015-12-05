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

(defn- join-room->event
  [response]
  [:join-room response])

(defn join-room
  [room]
  (-> (postal/novelty client :join {:player :me
                                    :room room})
      (prom/then join-room->event)
      (promise->observable)))

(comment
  (rx/on-value (promise->observable (prom/resolved 42))
               #(println :value %))

  (rx/on-error (promise->observable (prom/rejected :nope))
               #(println :error %))

  (rx/subscribe
   (join-room :foo)
   #(println :joined-room %)
   #(println :failed-to-join %)
   #(println :fin))

  (rx/subscribe
   (m/mlet [[_ room] (join-room "room")]
    (subscribe-to-room room))
   #(println :sub-room %)
   #(println :failed-to-subroom %)
   #(println :fin))
  )
