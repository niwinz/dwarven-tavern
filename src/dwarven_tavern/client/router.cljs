(ns dwarven-tavern.client.router
  (:require [bidi.router :as bidi]
            [dwarven-tavern.client.state :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Router declaration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:static
  routes ["/" {"home"        :home
               "rooms"       :rooms
               ["game/" :id] :game
               "help"        :help}])

(declare update-location)

(defn- on-navigate
  [data]
  (println "onnavigate:" data)
  (s/emit! (update-location data)))

(defonce +router+
  (bidi/start-router! routes {:on-navigate on-navigate
                              :default-location {:handler :home}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn update-location
  [{:keys [handler route-params]}]
  (reify
    s/UpdateEvent
    (-apply-update [_ state]
      (assoc state
             :location handler
             :location-params route-params))))

(defn navigate
  ([name] (navigate name nil))
  ([name params]
   {:pre [(keyword? name)]}
   (reify
     s/EffectEvent
     (-apply-effect [_ state]
       (bidi/set-location! +router+ {:handler name
                                     :route-params params})))))

