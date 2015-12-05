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
  [bus]
  (let [msg {:event :start}]
    (a/go
      (a/>! bus (pc/frame :message msg)))))

(defn- start-round
  [bus roomid round]
  (let [msg {:event :round-start :round round :time +round-time+}]
    (state/transact! [:game/update-round roomid round])
    (a/go
      (a/>! bus (pc/frame :message msg)))))

(defn start
  [bus roomid]
  (a/go
    (a/<! (broadcast-start bus))
    (loop [round 1]
      (a/<! (start-round bus roomid round))
      (a/<! (a/timeout +round-time+))
      (recur (inc round)))))
