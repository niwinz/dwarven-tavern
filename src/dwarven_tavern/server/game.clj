(ns dwarven-tavern.server.game
  (:require [clojure.core.async :as a]))

(def ^:static +default-room-width+ 10)
(def ^:static +default-room-height+ 10)

(def +room-statuses+ #{:pending
                       :playing
                       :closing})

(defn mk-room
  []
  (let [in (a/chan)
        mult (a/mult in)]
    {:width +default-room-width+
     :height +default-room-height+
     :status :pending
     :players {}
     :in in
     :mult mult
     :team1 #{}
     :team2 #{}
     :barrel {:pos [(quot +default-room-width+ 2)
                    (quot +default-room-height+ 2)]}}))

(defn mk-player
  [id team]
  (case team
    :team1 {:id id :pos [(rand-int +default-room-width+)
                         (rand-int (quot +default-room-height+ 2))]
            :team team :dir :south}
    :team2 {:id id :pos [(rand-int +default-room-width+)
                         (+ (rand-int (quot +default-room-height+ 2))
                            (quot +default-room-height+ 2))]
            :team team :dir :north}))

(comment
  {:rooms
   {:kakaroom
    {:width 10
     :height 10
     :team1 [{:id :dialelo
              :pos [1 2]
              :dir :south}]
     :team2 [{:id :alotor
              :pos [3 4]
              :dir :south}]
     :barrel {:pos [5 5]}}}})
