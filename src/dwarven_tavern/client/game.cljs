(ns dwarven-tavern.client.game
  (:require [beicon.core :as rx]
            [dwarven-tavern.client.state :as s]
            [dwarven-tavern.client.postal :as p]))

(defn swap
  "A helper for just apply some function to state
  without a need to declare additional event."
  [f]
  (reify s/UpdateEvent
    (-apply-update [_ state]
      (f state))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:game/swap>"))))


(defn set-roomlist
  "A helper for just apply some function to state
  without a need to declare additional event."
  [roomlist]
  (reify s/UpdateEvent
    (-apply-update [_ state]
      (assoc state :rooms roomlist))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:game/set-roomlist>"))))

(defn swapfn
  [f]
  #(swap f ))

(defn load-rooms
  []
  (println "load-rooms")
  (reify s/WatchEvent
    (-apply-watch [_ _]
      (println "-apply-watch")
      (->> (rx/from-promise (p/get-room-list))
           ;; (rx/catch (rx/empty))
           (rx/map (fn [{:keys [data] :as msg}]
                     (set-roomlist data)))))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:game/load-rooms>"))))

