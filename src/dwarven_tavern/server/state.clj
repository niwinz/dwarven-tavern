(ns dwarven-tavern.server.state
  (:require [clojure.core.async :as a]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce db (atom {:rooms {}}))

(def ^:static +default-room-width+ 10)
(def ^:static +default-room-height+ 10)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti transition
  (fn [_ [event]] event))

(defn transact!
  ([event]
   (swap! db transition event))
  ([event & events]
   (run! transact! (into [event] events))
   @db))

(defn get-room-by-id
  ([id] (get-room-by-id @db id))
  ([state id] (get-in state [:rooms id])))

(defn get-room-player-by-id
  ([roomid id] (get-room-player-by-id @db roomid id))
  ([state roomid id] (get-in state [:rooms roomid :players id])))

(defn get-room-list
  ([] (get-room-list @db))
  ([state]
   (letfn [(extract [[key value]]
             {:id key
              :players (+ (count (:team1 value)) (count (:team2 value)))
              :status (:status value)})]
     (mapv extract (:rooms state)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Transitions
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

(defmethod transition :room/assoc-player
  [state [_ room player]]
  (let [room' (get-in state [:rooms room])
        team (choice-team room' player)
        player' (mk-player player team)]
    (-> state
        (update-in [:rooms room team] conj player)
        (update-in [:rooms room :players] assoc player player'))))

(defn mk-room
  [roomid]
  (let [bus (a/chan)
        mult (a/mult bus)]
    {:width +default-room-width+
     :height +default-room-height+
     :id roomid
     :status :pending
     :players {}
     :moviments {}
     :round nil
     :start-time nil
     :bus bus
     :mult mult
     :team1 #{}
     :team2 #{}
     :barrel {:pos [(quot +default-room-width+ 2)
                    (quot +default-room-height+ 2)]}}))

(defmethod transition :room/create
  [state [_ roomid]]
  (let [room (mk-room roomid)]
    (update-in state [:rooms] assoc roomid room)))

(defmethod transition :room/join
  [state [_ {:keys [room player] :as msg}]]
  (if-let [room' (get-room-by-id state room)]
    (if-let [player' (get-room-player-by-id state room player)]
      state
      (transition state [:room/assoc-player room player]))
    (-> state
        (transition [:room/create room])
        (transition [:room/assoc-player room player]))))

(defmethod transition :game/mark-as-started
  [state [_ roomid]]
  (update-in state [:rooms roomid]
             merge
             {:status :started
              :start-time (System/nanoTime)}))

(defmethod transition :game/update-round
  [state [_ roomid round]]
  (update-in state [:rooms roomid] assoc :round round))

(defmethod transition :game/move
  [state [_ data]]
  (let [roomid (:room data)
        playerid (:player data)
        direction (:dir data)]
    (update-in state [:rooms roomid :moviments] assoc playerid direction)))
