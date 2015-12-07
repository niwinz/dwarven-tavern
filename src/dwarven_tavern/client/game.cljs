(ns dwarven-tavern.client.game
  (:require [beicon.core :as rx]
            [dwarven-tavern.client.rstore :as rs]
            [dwarven-tavern.client.postal :as p]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn swap
  "A helper for just apply some function to state
  without a need to declare additional event."
  [f]
  (reify rs/UpdateEvent
    (-apply-update [_ state]
      (f state))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:game/swap>"))))

(defn set-roomlist
  "A helper for just apply some function to state
  without a need to declare additional event."
  [roomlist]
  (reify rs/UpdateEvent
    (-apply-update [_ state]
      (assoc state :rooms roomlist))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:game/set-roomlist>"))))

(defn persist-player
  [pname]
  (let [pname (keyword pname)]
    (reify rs/UpdateEvent
      (-apply-update [_ state]
        (assoc state :player pname))

      IPrintWithWriter
      (-pr-writer [mv writer _]
        (-write writer "#<event:game/persist-player>")))))

(defn load-rooms
  []
  (reify rs/WatchEvent
    (-apply-watch [_ _]
      (->> (rx/from-promise (p/get-room-list))
           (rx/catch (rx/empty))
           (rx/map (fn [{:keys [data] :as msg}]
                     (set-roomlist data)))))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:game/load-rooms>"))))

