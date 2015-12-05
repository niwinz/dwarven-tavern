(ns dwarven-tavern.server.api
  (:require [clojure.core.async :as a]
            [catacumba.core :as ct]
            [catacumba.handlers.postal :as pc]
            [dwarven-tavern.state :as state]
            [dwarven-tavern.game :as game]
            [dwarven-tavern.server.schema :as schema]
            ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; NOTE: move to other ns when this section grows

(def +room-statuses+ #{:pending
                       :playing
                       :closing})

(defn mk-room
  []
  (let [in (a/chan)
        mult (a/mult in)]
    (merge (game/mk-room)
           {:players {}
            :in in
            :mult mult
            :status :pending})))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce state
  (atom {:rooms {}}))

(defmethod state/transition :assoc-player
  [state [_ room player]]
  (let [room' (get-in state [:rooms room])
        team (choice-team room' player)
        player' (game/mk-player player team)]
    (-> state
        (update-in [:rooms room team] conj player)
        (update-in [:rooms room :players] assoc player player'))))

(defmethod state/transition :creat-room
  [state [_ roomname]]
  (let [room (mk-room)]
    (update-in state [:rooms] assoc roomname room)))

(defmethod state/transition :join
  [state [_ {:keys [room player] :as msg}]]
  (if-let [room' (get-in state [:rooms room])]
    (if-let [player' (get-in room' [:players player])]
      state
      (state/transition state [:assoc-player room player]))
    (-> state
        (state/transition [:creat-room room])
        (state/transition [:assoc-player room player]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- in-room?
  [state roomid playerid]
  (let [room (get-in state [:rooms roomid :players])]
    (contains? room playerid)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Postal Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handler
  (comp (juxt :type :dest) second vector))

(defmethod handler [:novelty :join]
  [context {:keys [data]}]
  (if (schema/valid? schema/+join-msg+ data)
    (do
      (swap! state state/transition [:join data])
      (pc/frame {:ok true}))
    (pc/frame :error {:message "invalid message"})))

(declare start-game)

(defmethod handler [:novelty :start]
  [context {:keys [data]}]
  (if (schema/valid? schema/+start-msg+ data)
    (let [roomid (:room data)]
      (start-game roomid)
      (pc/frame {:ok true}))
    (pc/frame :error {:message "invalid message"})))

(defmethod handler [:subscribe :game]
  [context frame]
  (letfn [(on-socket [{:keys [in out ctrl]}]
            )
          (joined? [message]
            (let [room (:room message)
                  player (:player message)]
              (contains? (get-in @state [:rooms room :players] player))))]
    (let [message (:data frame)
          valid? (schema/valid? schema/+join-msg+)]
      (if-not (valid? message)
        (pc/frame :error {:message "invalid data"})
        (do
          (swap! state state/transition [:join-room message])
          (if (joined? (:player message))
            (pc/socket context on-socket)
            (pc/frame :error {:message "not joined"})))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game Logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (defn start-game
;;   [roomid]
;;   )
