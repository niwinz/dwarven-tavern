(ns dwarven-tavern.server.game
  (:require [clojure.core.async :as a]
            [cats.labs.lens :as l]
            [catacumba.handlers.postal :as pc]
            [dwarven-tavern.server.state :as state]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private +round-time+ 5000)

(defn- broadcast-start
  [room]
  (let [roomid (:id @room)
        bus (:bus @room)
        msg {:room roomid :event :start}]
    (a/go
      (a/>! bus (pc/frame :message msg)))))

(defn- start-round
  [room round]
  (let [roomid (:id @room)
        bus (:bus @room)
        msg {:event :round-start
             :round round
             :time +round-time+}]
    (swap! room assoc :round round)
    ;; (state/transact! [:game/set-round roomid round])
    (a/go
      (a/>! bus (pc/frame :message msg)))))

(defn start
  [room]
  (a/go
    (a/<! (broadcast-start room))
    (loop [round 1]
      (a/<! (start-round room round))
      (a/<! (a/timeout +round-time+))
      (recur (inc round)))
    ))
