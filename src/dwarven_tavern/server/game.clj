(ns dwarven-tavern.server.game
  (:require [clojure.core.async :as a]
            [cats.labs.lens :as l]
            [catacumba.handlers.postal :as pc]
            [dwarven-tavern.server.state :as state]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private +turn-time+ 5000)

(defn- broadcast-start-round
  [bus roomid round]
  (let [msg {:event :round-start :round round}]
    (state/transact! [:game/update-round roomid round])
    (a/go
      (a/>! bus (pc/frame :message msg)))))

(defn- broadcast-start-turn
  [bus roomid round]
  (let [msg {:event :turn-start :round round :time +turn-time+}]
    (state/transact! [:game/update-round roomid round])
    (a/go
      (a/>! bus (pc/frame :message msg)))))

(defn- broadcast-stop-turn
  [bus roomid round]
  (let [msg {:event :turn-end :round round}]
    (a/go
      (a/>! bus (pc/frame :message msg)))))


(defn- broadcast-turn-resolution
  [bus resolution]
  (let [msg {:event :turn-result :result resolution}]
    (a/go
      (a/>! bus (pc/frame :message msg)))))

(defn- materialize-moviment
  "A reducing function used for materialize the all collected
  moviments into new room state."
  [room [playerid dir :as moviment]]
  (let [[cx cy] (get-in room [:players playerid :pos])
        xmax state/+default-room-width+
        ymax state/+default-room-height+
        rpos (case dir
               :north (if (= cy 0) [cx cy] [cx (dec cy)])
               :south (if (= cy ymax) [cx cy] [cx (inc cy)])
               :east (if (= cx xmax) [cx cy] [(inc cx) cy])
               :west (if (= cx 0) [cx cy] [(dec cx) cy]))]
    (update-in room [:players playerid] merge {:pos rpos :dir dir})))

(defn- reposition-players
  "Secondary algorithm that resolves the colisions of players
  with barrel postion after the barrel has ben moved.

  The default behavior of this algorithm is just choice an
  empty position in the table and relocate the colided player
  to the new position."
  [{:keys [barrel players] :as room}]
  (let [players (vals players)
        players (filter #(= (:post barrel) (:pos %)) players)]
    (loop [players players
           room room]
      (if-let [player (first players)]
        (let [attempt [(rand-int state/+default-room-width+)
                        (rand-int state/+default-room-height+)]
              result (filter #(= (:pos %) attempt)
                             (into [barrel] (vals (:players room))))]
          (if (empty? result)
            (recur (rest players)
                   (update-in room [:players (:id player)] assoc :pos attempt))
            (recur players room)))
        room))))

(defn- resolve-colisions
  "Main algorithm that resolves the colisions of players
  with barrel position.

  This function can be considered as first phase of two
  or thre phases. This is becuase after resolve the new
  barril position in case of colision, the new position
  can colide with other players. That players should be
  randomly repositioned in other steps."
  [{:keys [barrel players] :as room}]
  (let [players (vals players)
        [bx by] (:pos barrel)]
    (loop [players' players
           room room]
      (if-let [player (first players')]
        (cond
          (not= (:pos player) (= :pos barrel))
          (recur (rest players') room)

          (contains? #{:north :south} (:dir player))
          (let [opfn (case (:pos player) :north inc :south dec)
                restplayers (filter #(not= % player) players)
                [bx by] (:pos barrel)]
            (update room :barrel assoc :pos [bx (opfn by)]))

          (and (= (:dir player) :east) (not= bx state/+default-room-width+))
          (update room :barrel assoc :pos [(inc bx) by])

          (and (= (:dir player) :west) (not= bx 0))
          (update room :barrel assoc :pos [(dec bx) by])

          :else room)
        room))))

(defn- materialize-moviments
  [room moviments]
  (as-> room room
    (reduce materialize-moviment room (:movimens room))
    (resolve-colisions room)
    (reposition-players room)))

(defn- resolve-movements
  [roomid]
  (let [room (state/get-room-by-id roomid)
        moviments (into [] (:moviments room))]
    (materialize-moviments room moviments)))

(defn start-turn
  [bus roomid round turn]
  (a/go
    (a/<! (broadcast-start-turn bus roomid round turn))
    (a/<! (a/timeout +turn-time+))
    (a/<! (broadcast-stop-turn bus roomid round turn))
    (resolve-movements roomid)))

(defn start-round
  [bus roomid round]
  (a/go
    (a/<! (broadcast-start-round bus roomid round))
    (loop [turn 0]
      (let [resolution (a/<! (start-turn bus roomid round turn))]
        (a/<! (broadcast-turn-resolution bus resolution))
        (when-not (:end resolution)
          (recur (inc turn)))))))

(defn start
  [bus roomid]
  (a/go
    (loop [round 1]
      (when (< round 3)
        (a/<! (start-round bus roomid round))
        (a/<! (a/timeout 500))
        (recur (inc round))))))
