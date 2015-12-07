(ns dwarven-tavern.client.router
  (:require [bidi.router :as bidi]
            [dwarven-tavern.client.rstore :as rs]))

(enable-console-print!)

(declare +router+)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn update-location
  [{:keys [handler route-params]}]
  (reify
    rs/UpdateEvent
    (-apply-update [_ state]
      (merge state
             {:location handler}
             (when route-params
               {:location-params route-params})))))

(defn navigate
  ([name] (navigate name nil))
  ([name params]
   {:pre [(keyword? name)]}
   (reify
     rs/EffectEvent
     (-apply-effect [_ state]
       (let [loc (merge {:handler name}
                        (when params
                          {:route-params params}))]
         (println "navigate$-apply-effect" loc)
         (bidi/set-location! +router+ loc))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Router declaration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:static
  routes ["/" [["home" :home]
               ["rooms" :rooms]
               [["game/" :id] :game]
               ["help" :help]]])

(declare update-location)

(defn- on-navigate
  [data]
  (println "onnavigate:" data)
  (rs/emit! (update-location data)))

(defonce +router+
  (bidi/start-router! routes {:on-navigate on-navigate
                              :default-location {:handler :home}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn go
  "Redirect the user to other url."
  ([name] (go name nil))
  ([name params] (rs/emit! (navigate name params))))
