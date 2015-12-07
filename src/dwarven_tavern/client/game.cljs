(ns dwarven-tavern.client.game
  (:require [beicon.core :as rx]
            [dwarven-tavern.client.router :as r]
            [dwarven-tavern.client.rstore :as rs]
            [dwarven-tavern.client.postal :as p]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn swap
  "A helper for just apply some function to state
  without a need to declare additional event."
  [f]
  (reify
    rs/UpdateEvent
    (-apply-update [_ state]
      (f state))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:game/swap>"))))

(defn set-roomlist
  "A helper for just apply some function to state
  without a need to declare additional event."
  [roomlist]
  (reify
    rs/UpdateEvent
    (-apply-update [_ state]
      (assoc state :rooms roomlist))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:game/set-roomlist>"))))

(defn set-player
  [pname]
  (let [pname (keyword pname)]
    (reify
      rs/UpdateEvent
      (-apply-update [_ state]
        (assoc state :player pname))

      IPrintWithWriter
      (-pr-writer [mv writer _]
        (-write writer "#<event:game/persist-player>")))))

(defn load-rooms
  []
  (letfn [(assoc-rooms [state rooms]
            (assoc state :rooms (mapv :id rooms)))
          (index-rooms [state rooms]
            (reduce #(assoc-in %1 [:rooms-by-id (:id %2)] %2) state rooms))]
    (reify
      rs/WatchEvent
      (-apply-watch [_ _]
        (->> (rx/from-promise (p/get-room-list))
             (rx/catch (rx/empty))
             (rx/map :data)
             (rx/map (fn [rooms]
                       (rx/of
                        (swap #(assoc-rooms % rooms))
                        (swap #(index-rooms % rooms)))))))

      IPrintWithWriter
      (-pr-writer [mv writer _]
        (-write writer "#<event:game/load-rooms>")))))

(defn join-game
  ([] (join-game (str (gensym "room-"))))
  ([room]
   (reify
     rs/WatchEvent
     (-apply-watch [_ state]
       (let [room (keyword room)
             player (:player state)]
         (->> (p/join-room player room)
              (rx/from-promise)
              (rx/map #(get-in % [:data :room]))
              (rx/map (fn [room]
                        (rx/of
                         (load-rooms)
                         (swap #(assoc % :current room))))))))

    IPrintWithWriter
    (-pr-writer [mv writer _]
      (-write writer "#<event:game/new-game>")))))


