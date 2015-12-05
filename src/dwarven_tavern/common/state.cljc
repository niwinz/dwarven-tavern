(ns dwarven-tavern.common.state)

(defmulti transition
  (fn [_ [event]] event))
