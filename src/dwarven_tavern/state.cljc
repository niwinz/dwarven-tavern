(ns dwarven-tavern.state)

(defmulti transition
  (fn [_ [event]] event))
