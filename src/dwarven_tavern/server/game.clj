(ns dwarven-tavern.server.game
  (:require [clojure.core.async :as a]
            [catacumba.handlers.postal :as pc]
            [dwarven-tavern.server.state :as state]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private +round-time+ 5000)

(defn- broadcast-start
  [{:keys [bus id] :as room}]
  (let [msg {:room id :event :start}]
    (a/go
      (a/>! bus (pc/frame :message msg)))))

(defn- start-round
  [{:keys [id bus]} round]
  (let [state (state/transact! [:game/set-round id round])
        msg {:event :round-start
             :round round
             :time +round-time+}]
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
