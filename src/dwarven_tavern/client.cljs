(ns dwarven-tavern.client
  (:require [goog.dom :as dom]
            [cats.core :as m]
            [rum.core :as rum]
            [promesa.core :as p]
            [bidi.router :as bidi]
            [dwarven-tavern.client.view.root :as v]
            [dwarven-tavern.client.state :as s]))

(enable-console-print!)

(defonce stream
  (s/init {:location :home}))

(defonce router
  (bidi/start-router!
   ["/" {"home"  :home
         "rooms" :rooms
         ["game/" :id] :game}]
   {:on-navigate (fn [item]
                   (println "on-navigate"))
    :default-location {:handler :home}}))

;; (defmethod st/transition :join-room
;;   [{:keys [player]} [_ room]]

;;   (let [join-promise (p/join-room player room)]
;;     (rx/merge (-> join-promise
;;                   (prom/then #(vector :room-joined (-> % :data :room)))
;;                   (prom/then (sideffect
;;                               #(bidi/set-location! router {:handler :game
;;                                                            :route-params {:id room}})))
;;                   (from-promise))
;;               (p/subscribe-to-room room))))

;; (defmethod st/transition :room-joined
;;   [state [_ room-data]]
;;   (-> state
;;       (assoc :location :game)
;;       (assoc :current-game room-data))
;;   )

;; (defmethod st/transition :start-game
;;   [_ room]
;;   (prom/then (p/start-game room)
;;              (fn [game]
;;                ;; TODO: assoc current game
;;                [:noop])))

;; (defmethod st/transition :new-room
;;   [state room]
;;   ;; TODO: tell the server
;;   (update state :rooms (fnil merge {}) room))

;; (defmethod st/transition :room-list
;;   [state [_ rooms]]
;;   (assoc state :room-list rooms))

;; (defmethod st/transition :move
;;   [state {:keys [room direction]}]
;;   (let [player (:player state)]
;;     (prom/then (p/move player room direction)
;;                (fn [response]
;;                  [:noop]))))

;; (defn fetch-rooms!
;;   [db]
;;   (m/mlet [rooms (p/get-room-list)]
;;     (st/transact! db [:room-list rooms])))

;; (defn render!
;;   [element db]
;;   (rum/mount (v/root {:state db
;;                       :signal (partial st/transact! db)
;;                       :router router})
;;              element))

;; (defn start-router!
;;   [db]
;;   (bidi/start-router! ["/" {"home"        :home
;;                             "rooms"       :rooms
;;                             ["game/" :id] :game
;;                             "help"        :help}]
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


