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
    {:players []
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

;; (defmethod handler [:subscribe :game]
;;   [context frame]
;;   (letfn [(on-socket [{:keys [in out ctrl]}]
;;             )]
;;     (let [mesage (:data frame)]
;;       (swap! state/transition [:join-room message]
;;     (if (
;;     (pc/socket context on-socket)))
