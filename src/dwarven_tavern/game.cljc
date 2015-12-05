(ns dwarven-tavern.game)

(def ^:static +default-room-width+ 10)
(def ^:static +default-room-height+ 10)

(defn mk-room
  []
  {:width +default-room-width+
   :height +default-room-height+
   :team1 []
   :team2 []
   :barrel {:pos [(quot +default-room-width+ 2)
                  (quot +default-room-height+ 2)]}})

(defn mk-player
  [id team]
  (case team
    1 {:id id :pos [(rand-int +default-room-width+)
                    (rand-int (quot +default-room-height+ 2))]
       :dir :south}
    2 {:id id :pos [(rand-int +default-room-width+)
                    (+ (rand-int (quot +default-room-height+ 2))
                       (quot +default-room-height+ 2))]
       :dir :north}))

(comment
  {:rooms
   {:kakaroom
    {:width 10
     :height 10
     :team1 [{:id :dialelo
              :pos [1 2]
              :dir :south}]
     :team2 [{:id :alotor
              :pos [3 4]
              :dir :south}]
     :barrel {:pos [5 5]}}}})

