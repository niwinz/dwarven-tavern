(ns dwarven-tavern.server
  (:require [com.stuartsierra.component :as component]
            [catacumba.core :as ct]
            [catacumba.components :as ctc]
            [catacumba.handlers.postal :as pc]
            [dwarven-tavern.server.api :as api])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord App [server]
  component/Lifecycle
  (start [this]
    (let [routes [[:any (ctc/extra-data {::app this})]
                  [:any "api" (pc/router api/handler)]
                  [:assets "" {:dir "public"
                               :indexes ["index.html"]}]]]
      (ctc/assoc-routes! server ::web routes)))

  (stop [this]
    ;; noop
    ))

(alter-meta! #'->App assoc :private true)
(alter-meta! #'map->App assoc :private true)

(def app #(->App nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Application System
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn system
  "The application system constructor."
  []
  (-> (component/system-map
       :catacumba (ctc/catacumba-server {:port 5050})
       :app (app))
      (component/system-using
       {:app {:server :catacumba}})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entry Point
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -main
  "The main entry point to your application."
  [& args]
  (component/start (system)))
