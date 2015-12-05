(ns dwarven-tavern.server.api
  (:require [clojure.core.async :as a]
            [catacumba.core :as ct]
            [catacumba.handlers.postal :as pc]
            [dwarven-tavern.common.state :as state]
            [dwarven-tavern.server.schema :as schema]
            ;; [dwarven-tavern.server.game :as game]
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
  (let [out (a/chan 8)
        mix (a/mix out)]
    {:players {}
     :socket out
     :mix mix
     :status :incomplete
     :responses {}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce state
  (atom {:rooms {}}))

(defmethod state/transition :join-room
  [state [_ {:keys [name]}]]
  ;; TODO
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Postal Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handler
  (comp (juxt :type :dest) second vector))

(defmethod handler [:novelty :join-room]
  [context {:keys [data]}]
  )

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


