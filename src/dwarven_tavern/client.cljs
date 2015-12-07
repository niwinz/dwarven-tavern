(ns dwarven-tavern.client
  (:require [goog.dom :as dom]
            [cats.core :as m]
            [rum.core :as rum]
            [promesa.core :as p]
            [bidi.router :as bidi]
            [dwarven-tavern.client.game :as g]
            [dwarven-tavern.client.view.root :as v]
            [dwarven-tavern.client.state :as s]))

(enable-console-print!)

(defonce stream
  (s/init {:location :home}))

;; (s/emit! (g/load-rooms))

;; (defn render!
;;   [element db]
;;   (rum/mount (v/root {:state db
;;                       :signal (partial st/transact! db)
;;                       :router router})
;;              element))

;; (defn start-router!
;;   [db]
;;   (bidi/start-router!
;;                       {:on-navigate (fn [location]
;;                                       (swap! db assoc :location (:handler location)))
;;                        :default-location {:handler :home}}))

;; (defn init
;;   [db]
;;   (fetch-rooms! db)
;;   (start-router! db)
;;   (render! (gdom/getElement "app") db))

;; Main
(let [el (dom/getElement "app")]
  (rum/mount (v/root {}) el))


