(ns dwarven-tavern.schema
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schemas
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def +join-msg+
  {:room [v/required keyword?]
   :player [v/required keyword?]})

(def +start-msg+
  {:room [v/required keyword?]})

(def +subscribe-msg+ +join-msg+)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn valid?
  "Check if the data matches the schema."
  ([schema]
   (fn [data]
     (b/valid? data schema)))
  ([schema data]
   (b/valid? data schema)))
