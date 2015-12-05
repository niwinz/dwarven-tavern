(ns dwarven-tavern.server.api
  (:require [catacumba.core :as ct]
            [catacumba.handlers.postal :as pc]))

(defmulti handler
  (comp (juxt :type :dest) second vector))

(defmethod handler [:novelty :foobar]
  [context {:keys [data]}]
  )

(defmethod handler [:subscribe :chats]
  [context frame]
  (let [bus (get-in context [::app :eventbus])]
    (letfn [(on-socket [{:keys [in out ctrl]}]
              )]
      (pc/socket context on-socket))))
