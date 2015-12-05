(ns dwarven-tavern.server.api
  (:require [clojure.core.async :as a]
            [catacumba.core :as ct]
            [catacumba.handlers.postal :as pc]
            [dwarven-tavern.server.game :as game]
            [dwarven-tavern.server.state :as state]
            [dwarven-tavern.schema :as schema]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (defn- in-room?
;;   [state roomid playerid]
;;   (let [room (get-in state [:rooms roomid :players])]
;;     (contains? room playerid)))

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
          state (state/transact! [:room/join data])
          room (state/get-room-by-id state roomid)]
      (pc/frame {:room (strip-room room)}))))

(defmethod handler [:novelty :start]
  [context {:keys [data]}]
  (if-let [errors (schema/validate-start-msg data)]
    (pc/frame :error errors)
    (let [roomid (:room data)]
      (state/transact! [:game/start roomid])
      (let [room (state/get-room-by-id roomid)]
        (game/start room)
        (pc/frame {:ok true})))))

(defmethod handler [:novelty :movement]
  [context {:keys [data]}]
  (if-let [errors (schema/validate-move-msg data)]
    (pc/frame :error errors)
    (do
      (state/transact! [:game/move data])
      (pc/frame {}))))

(defmethod handler [:subscribe :game]
  [context {:keys [data] :as frame}]
  (letfn [(on-subscribe [{:keys [out ctrl] :as context}]
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
                      (a/close! ch)))))))]
    (if-let [errors (schema/validate-subscribe-msg data)]
      (pc/frame :error errors)
      (let [roomid (:room data)
            room (state/get-room-by-id roomid)]
        (-> (assoc context ::room room)
            (pc/socket on-subscribe))))))
