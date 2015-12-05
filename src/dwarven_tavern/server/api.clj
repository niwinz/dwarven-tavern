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

(def +room-statuses+ #{:incomplete
                       :ready
                       :playing
                       :closing})

(defn initialize-room
  []
  (let [in (a/chan)
        mult (a/mult in)]
    (merge (game/mk-room)
           {:players {}
            :in in
            :mult mult
            :status :incomplete})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce state
  (atom {:rooms {}}))

(defmethod state/transition :assoc-player
  [state [_ room player]]
  (let [room' (get-in state [:rooms room])
        team1 (:team1 room')
        team2 (:team2 room')
        teamid (cond
                 (empty? team1) 1
                 (empty? team2) 2
                 (contains? team1 player) 1
                 (contains? team2 player) 2
                 :else (throw (ex-info "Inconsistentcy!" {})))
        player' (game/mk-player player teamid)]
    (-> state
        (update-in [:rooms room (keyword (str "team" teamid))] conj player)
        (update-in [:rooms room :players] assoc player player'))))

(defmethod state/transition :creat-room
  [state [_ roomname]]
  (let [room (game/mk-room)]
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
;; Postal Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handler
  (comp (juxt :type :dest) second vector))

;; (defmethod handler [:novelty :join]
;;   [context {:keys [data]}]
;;   (letfn [(join [{:keys [room player] :as msg}]
;;             (swap! state state/transition [:join msg])
;;             (contains? (get-in @state [:rooms room :players] player))]


;;   (if-not (schema/valid? schema/+join-msg+ data)
;;     (pc/frame :error {:message "invalid message"})
;;   )

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
