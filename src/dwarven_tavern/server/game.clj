(ns dwarven-tavern.server.game
  (:require [clojure.core.async :as a]
            [catacumba.handlers.postal :as pc]
            [dwarven-tavern.state :as state]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basic game components constructors
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
     :movements {}
     :round 1
     :start-time nil
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- choice-team
  [room playerid]
  (let [team1 (:team1 room)
        team2 (:team2 room)]
    (cond
      (empty? team1) :team1
      (empty? team2) :team2
      (contains? team1 playerid) :team1
      (contains? team2 playerid) :team2
      (> (count team1) (count team2)) :team2
      (> (count team2) (count team1)) :team1
      :else :team1)))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod state/transition :assoc-player
  [state [_ room player]]
  (let [room' (get-in state [:rooms room])
        team (choice-team room' player)
        player' (mk-player player team)]
    (-> state
        (update-in [:rooms room team] conj player)
        (update-in [:rooms room :players] assoc player player'))))

(defmethod state/transition :create-room
  [state [_ roomid]]
  (let [room (mk-room roomid)]
    (update-in state [:rooms] assoc roomid room)))

(defmethod state/transition :join
  [state [_ {:keys [room player] :as msg}]]
  (if-let [room' (get-in state [:rooms room])]
    (if-let [player' (get-in room' [:players player])]
      state
      (state/transition state [:assoc-player room player]))
    (-> state
        (state/transition [:create-room room])
        (state/transition [:assoc-player room player]))))

(defmethod state/transition :start-game
  [state [_ roomid]]
  (let [state (update-in state [:rooms roomid] merge
                         {:status :playing
                          :start-time (System/nanoTime)})
        room (get-in state [:rooms roomid])]
    (start room)
    state))

(defmethod state/transition :move
  [state [_ data]]
  (let [roomid (:room data)
        playerid (:player data)
        direction (:dir data)]
    (update-in state [:rooms roomid :moviments] playerid direction)))
