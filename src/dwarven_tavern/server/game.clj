(ns dwarven-tavern.server.game
  (:require [clojure.core.async :as a]
            [dwarven-tavern.server.state :as state]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private +turn-time+ 5000)
(def ^:private +max-rounds+ 3)

(defn broadcast
  [room message]
  (a/go
    (a/>! (:bus room) message)))

(defn materialize-moviment
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

(defn reposition-players
  "Secondary algorithm that resolves the colisions of players
  with barrel postion after the barrel has ben moved.

  The default behavior of this algorithm is just choice an
  empty position in the table and relocate the colided player
  to the new position."
  [{:keys [barrel players] :as room}]
  (let [players (vals players)
        players (filter #(= (:pos barrel) (:pos %)) players)]
    (loop [player (first players)
           players (rest players)
           room room]
      (if (nil? player)
        room
        (let [attempt [(rand-int state/+default-room-width+)
                       (rand-int state/+default-room-height+)]
              result (filter #(= (:pos %) attempt)
                             (into [barrel] (vals (:players room))))]
          (if (empty? result)
            (recur (first players)
                   (rest players)
                   (update-in room [:players (:id player)] assoc :pos attempt))
            (recur player players room)))))))

(defn resolve-colisions
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
    (loop [players' (rest players)
           player (first players)
           room room]
      (cond
        (nil? player) room

        (not= (:pos player) (:pos barrel))
        (recur (rest players') (first players') room)

        (contains? #{:north :south} (:dir player))
        (let [opfn (case (:dir player) :north dec :south inc)
              restplayers (filter #(not= % player) players)
              [bx by] (:pos barrel)
              [bx by] [bx (opfn by)]]
          (cond
            (= by 0)
            (-> room
                (assoc :status :round/ended)
                (update :barrel assoc :pos [bx by])
                (update-in [:punctuations :team1] inc))

            (and (= by (dec state/+default-room-height+)))
            (-> room
                (assoc :status :round/ended)
                (update :barrel assoc :pos [bx by])
                (update-in [:punctuations :team2] inc))

            :else
            (update room :barrel assoc :pos [bx by])))

        (and (= (:dir player) :east) (not= bx state/+default-room-width+))
        (update room :barrel assoc :pos [(inc bx) by])

        (and (= (:dir player) :west) (not= bx 0))
        (update room :barrel assoc :pos [(dec bx) by])

        :else
        (recur (rest players')
               (first players')
               room)))))

(defn materialize-moviments
  [room]
  (as-> room room
    (reduce materialize-moviment room (:moviments room))
    (resolve-colisions room)
    (reposition-players room)))

(defn resolve-movements
  [roomid]
  (let [room (state/get-room-by-id roomid)]
    (materialize-moviments room)))

(defn start-turn
  [room round turn]
  (let [roomid (:id room)]
    (a/go
      (println "===> [" roomid "] round " round " turn " turn)
      (a/<! (broadcast room {:event :turn/start :round round
                            :turn turn :time +turn-time+}))
      (a/<! (a/timeout +turn-time+))
      (a/<! (broadcast room {:event :turn/end :round round :turn turn}))
      (resolve-movements roomid))))

(defn start-round
  [room round]
  (let [closed (:closed room)
        roomid (:id room)]
    (a/go
      (println "==> [" roomid "] round " round)

      (state/transact! [:game/update-round roomid round])
      (a/<! (broadcast room {:event :round/start :round round}))

      (loop [turn 0]
        (let [[room p] (a/alts!! [(start-turn room round turn) closed])]
          (when (not= p closed)
            (let [room' (state/strip-room room)]
              (a/<! (broadcast room {:event :result :room room'})))
            (when-not (= :round/ended (:status room))
              (recur (inc turn)))))))))

(defn start
  [room]
  (let [closed (:closed room)
        roomid (:id room)]
    (a/go
      (println "=> [" (:id room) "] game started")
      (loop [round 1]
        (when (< round +max-rounds+)
          (let [[val p] (a/alts!! [(start-round room round) closed])]
            (when (not= p closed)
              (a/<! (a/timeout 500))
              (recur (inc round))))))
      (state/transact! [:game/end roomid])
      (println "=> [" roomid "] game finished"))))
