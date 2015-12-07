(ns dwarven-tavern.server.api
  (:require [clojure.core.async :as a]
            [catacumba.core :as ct]
            [catacumba.handlers.postal :as pc]
            [cats.labs.lens :as l]
            [dwarven-tavern.server.game :as game]
            [dwarven-tavern.server.state :as state]
            [dwarven-tavern.schema :as schema]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Postal Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handler
  (comp (juxt :type :dest) second vector))

(defmethod handler [:query :room/list]
  [context {:keys [data]}]
  (pc/frame (state/get-room-list)))

(defmethod handler [:novelty :room/join]
  [context {:keys [data]}]
  (println "RECEIVED:" data)
  (if-let [errors (schema/validate-join-msg data)]
    (pc/frame :error errors)
    (let [roomid (:room data)
          state (state/transact! [:room/join data])
          room (state/get-room-by-id state roomid)]
      (game/broadcast room {:event :room/join
                            :room (state/strip-room room)})
      (pc/frame {:room (state/strip-room room)}))))

(defmethod handler [:novelty :game/start]
  [context {:keys [data]}]
  (if-let [errors (schema/validate-start-msg data)]
    (pc/frame :error errors)
    (let [roomid (:room data)
          state (state/transact! [:game/mark-as-started roomid])
          room (state/get-room-by-id state roomid)]
      (game/start room)
      (pc/frame {:ok true}))))

(defmethod handler [:novelty :game/reset]
  [context {:keys [data]}]
  (if-let [errors (schema/validate-reset-msg data)]
    (pc/frame :error errors)
    (let [roomid (:room data)
          state (state/transact! [:room/reset roomid])
          room (state/get-room-by-id state roomid)]
      (pc/frame {:room (state/strip-room room)}))))

(defmethod handler [:novelty :game/movement]
  [context {:keys [data]}]
  (if-let [errors (schema/validate-move-msg data)]
    (pc/frame :error errors)
    (do
      (state/transact! [:game/move data])
      (pc/frame {}))))

(defn on-subscribe
  [{:keys [out ctrl] :as context}]
  (let [room (::room context)
        ch (a/tap (:mult room) (a/chan) true)]
    (a/go-loop []
      (let [[val p] (a/alts! [ch ctrl])]
        (cond
          ;; If close is received from catacumba
          ;; it means that client closes the
          ;; connection.
          (identical? p ctrl)
          (a/close! ch)

          ;; If something from room broadcast
          ;; channel is received, retransmit
          ;; it to the client.
          (identical? p ch)
          (if (a/>! out (pc/frame :message val))
            (recur)
            (a/close! ch)))))))

(defmethod handler [:subscribe :game/events]
  [context {:keys [data] :as frame}]
  (if-let [errors (schema/validate-subscribe-msg data)]
    (pc/frame :error errors)
    (let [roomid (:room data)
          room (state/get-room-by-id roomid)]
      (-> (assoc context ::room room)
          (pc/socket on-subscribe)))))
