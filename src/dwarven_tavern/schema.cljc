(ns dwarven-tavern.schema
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schemas
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def +player+ [v/required keyword?])
(def +room+ [v/required keyword?])

(def +join-msg+
  {:room +room+
   :player +player+})

(def +start-msg+
  {:room +room+})

(def +subscribe-msg+
  +join-msg+)

(def +move-msg+
  {:room +room+
   :player +room+
   :dor keyword?})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn valid?
  "Check if the data matches the schema."
  {:deprecated true}
  ([schema] #(valid? schema %))
  ([schema data] (b/valid? data schema)))

(defn validate
  ([schema] #(validate schema %))
  ([schema data] (first (b/validate data schema))))

;; Aliases

(def validate-join-msg (validate +join-msg+))
(def validate-start-msg (validate +start-msg+))
(def validate-subscribe-msg (validate +subscribe-msg+))
