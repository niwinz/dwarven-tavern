(ns dwarven-tavern.server.game
  (:require [clojure.core.async :as a]
            [catacumba.handlers.postal :as pc]))

(def ^:static +default-room-width+ 10)
(def ^:static +default-room-height+ 10)

(def +room-statuses+ #{:pending
                       :playing
                       :closing})

(defn mk-room
  [roomid]
  (let [in (a/chan)
        mult (a/mult in)]
    {:width +default-room-width+
     :height +default-room-height+
     :id roomid
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

(defn broadcast-start
  [{:keys [in id] :as room}]
  (let [msg {:room id :event :start}]
    (a/go
      (a/>! in (pc/frame :message msg)))))

(defn start
  [room]
  (a/go
    (a/<! (broadcast-start room))
    ;; TODO
    ))
