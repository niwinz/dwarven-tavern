(ns dwarven-tavern.server.api
  (:require [clojure.core.async :as a]
            [catacumba.core :as ct]
            [catacumba.handlers.postal :as pc]
            [dwarven-tavern.server.game :as game]
            [dwarven-tavern.state :as state]
            [dwarven-tavern.schema :as schema]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; NOTE: move to other ns when this section grows

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

(defmethod state/transition :create-room
  [state [_ roomid]]
  (let [room (game/mk-room roomid)]
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
  (let [state (update-in state [:rooms roomid] assoc :status :playing)
        room (get-in state [:rooms roomid])]
    (game/start room)
    state))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- in-room?
  [state roomid playerid]
  (let [room (get-in state [:rooms roomid :players])]
    (contains? room playerid)))

(defn- strip-room
  [room]
  (select-keys room [:width :height :players :team1 :team2 :barrel]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Postal Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handler
  (comp (juxt :type :dest) second vector))

(defmethod handler [:novelty :join]
  [context {:keys [data]}]
  (if-let [errors (schema/validate-join-msg data)]
    (pc/frame :error errors)
    (let [roomid (:room data)
          state (swap! state state/transition [:join data])
          room (get-in state [:rooms roomid])]
      (pc/frame {:room (strip-room room)}))))

(defmethod handler [:novelty :start]
  [context {:keys [data]}]
  (if-let [errors (schema/validate-start-msg data)]
    (pc/frame :error errors)
    (let [roomid (:room data)
          room (get-in @state [:rooms roomid])]
      (swap! state state/transition [:start-game roomid])
      (pc/frame {:ok true}))))

;; (defmethod handler [:novelty :movement]
;;   [context {:keys [data]}]
;;   (if-not (schema/valid? schema/+move-msg+ data)
;;     (pc/frame :error {:message "Invalid message"})


(defn on-subscribe
  [{:keys [out ctrl] :as context}]
  (let [room (::room context)
        ch (a/tap (:mult room) (a/chan) true)]
    (a/go-loop []
      (let [[val p] (a/alts! [ch ctrl])]
        (cond
          (identical? p ctrl)
          (a/close! ch)

          (identical? p ch)
          (if (a/>! out val)
            (recur)
            (a/close! ch)))))))

(defmethod handler [:subscribe :game]
  [context {:keys [data] :as frame}]
  (if-let [errors (schema/validate-subscribe-msg data)]
    (pc/frame :error errors)
    (let [roomid (:room data)
          room (get-in @state [:rooms roomid])]
      (-> (assoc context ::room room)
          (pc/socket on-subscribe)))))
