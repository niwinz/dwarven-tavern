(ns dwarven-tavern.client.view.root
  (:require [sablono.core :as html :refer-macros [html]]
            [rum.core :as rum]
            [dwarven-tavern.client.view.util :as util]
            ))

(enable-console-print!)

(def sprites {:team1  "url(/images/team1.png)"
              :team2  "url(/images/team2.png)"
              :barrel "url(/images/barrel.png)"})

(def dwarf-animation
  {:north [{ :x 0  :y 32 :width 33 :height 32}
           { :x 33 :y 32 :width 33 :height 32}
           { :x 66 :y 32 :width 33 :height 32}]
   :east  [{ :x 0  :y 64 :width 33 :height 32}
           { :x 33 :y 64 :width 33 :height 32}
           { :x 66 :y 64 :width 33 :height 32}]
   :south [{ :x 0  :y 0  :width 33 :height 33}
           { :x 33 :y 0  :width 33 :height 32}
           { :x 66 :y 0  :width 33 :height 32}]
   :west  [{ :x 0  :y 96 :width 33 :height 32}
           { :x 33 :y 96 :width 33 :height 32}
           { :x 66 :y 96 :width 33 :height 32}]})

(def barrel-animation
  {:north [{ :x 32 :y 0 :width 32 :height 32}]
   :east  [{ :x 0  :y 0 :width 32 :height 32}]
   :south [{ :x 32 :y 0 :width 32 :height 32}]
   :west  [{ :x 0  :y 0 :width 32 :height 32}]})


(defonce heartbeat-atom (atom 0))
(defonce interval-timer
  (js/setInterval (fn [] (swap! heartbeat-atom inc)) 200))


(defn render-barrel [direction]
  (let [sprite-url (:barrel sprites)
        sprite (-> barrel-animation direction first)
        {:keys [x y width height]} sprite]
    [:.barrel {:style {:background-image sprite-url
                       :width (str width "px")
                       :height (str height "px")
                       :background-position (str x "px " y "px")}}]))

(defn render-dwarf [team animation idx]
  (let [sprite-url (team sprites)
        sprite (-> dwarf-animation animation (nth idx))
        {:keys [x y width height]} sprite]
    [:.dwarf.team1 {:style {:background-image sprite-url
                            :width (str width "px")
                            :height (str height "px")
                            :background-position (str x "px " y "px")}}]))

(def north-keys? #{87 38})
(def south-keys? #{83 40})
(def west-keys? #{65 37})
(def east-keys? #{68 39})

(defn movement
  [signal room]
  (fn [ev]
    (.preventDefault ev)
    (let [key (.-keyCode ev)]
      (cond
        (north-keys? key)
        (signal [:move {:room room
                        :direction :north}])

        (south-keys? key)
        (signal [:move {:room room
                        :direction :south}])

        (west-keys? key)
        (signal [:move {:room room
                        :direction :west}])

        (east-keys? key)
        (signal [:move {:room room
                        :direction :east}])))))

(defn game-grid
  [own]
  (let [{:keys [width height team1 team2 barrel] } (->  own :rum/props :state deref :current-game)
        signal (get-in own [:rum/props :signal])
        {[posx-team1  posy-team1] :pos dir-team1 :dir} (first (:members team1))
        {[posx-team2  posy-team2] :pos dir-team2 :dir} (first (:members team2))
        {[posx-barrel posy-barrel] :pos dir-barrel :dir} barrel]
    [:table.grid
     {:tab-index 0
      :on-key-down (movement signal :foo)} ;; TODO: pass actual room instead of `:foo`
     [:tbody
      (for [row (range 0 height)]
        [:tr
         (for [column (range 0 width)]
           [:td
            (when (and (= row posy-barrel) (= column posx-barrel))
              (render-barrel dir-barrel))
            (when (and (= row posy-team1) (= column posx-team1))
              (render-dwarf :team1 dir-team1 (mod (rum/react heartbeat-atom) 3)))
            (when (and (= row posy-team2) (= column posx-team2))
              (render-dwarf :team2 dir-team2 (mod (rum/react heartbeat-atom) 3)))])])]]))


(defn render-game
  [own]
  (let [{:keys [total-time time-progress team1 team2]} (-> own :rum/props :state deref :current-game)
        {score-t1 :score members-t1 :members} team1
        {score-t2 :score members-t2 :members} team2]
    [:.container.game
     [:img#logo {:src "/images/tavern-logo.png"}]
     [:.turnprogress
      [:span.time-label "Turn time"]
      [:.time-slider [:.progress {:style {"width" (str (* 100 (/ time-progress total-time)) "%")}}]]
      [:.time-counter time-progress]]
     [:.scoreboard
      [:.team.team1
       (for [{id :id} members-t1]
         [:span.name (name id)])
       [:span.score score-t1]]
      [:.vs "VS"]
      [:.team.team2
       (for [{id :id} members-t2]
         [:span.name (name id)])
       [:span.score score-t2]]]
     (game-grid own)]))

(defn render-room-list
  [own]
  (let [{:keys [room-list]} (-> own :rum/props :state deref)]
    [:.container.room-list
     [:img#logo {:src "/images/tavern-logo.png"}]
     [:.room-list-container
      [:div.room-list-header
       [:h2.room-list-title "Available games"]
       [:a.new-game {:href "#"} "New game!"]]
      [:ul
       (for [{:keys [id players max joinable]} room-list]
         [:li
          [:div.room-element
           [:span.room-element-name id]
           [:span.room-element-room (str "(" players "/" max ")")]
           [:a.join {:href "#"} "Join"]]])]]]))

(defn render-root
  [own]
  (let [{:keys [location]} (-> own :rum/props :state deref)]
    (html
     (condp = location
       :home (render-room-list own)
       :game (render-game own)
       [:div (str "Not found: " location)]))))

(def root
  (util/component
   {:render render-root
    :name "root"
    :mixins [util/cursored rum/reactive]}))


