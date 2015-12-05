(ns dwarven-tavern.client.view.root
  (:require [sablono.core :as html :refer-macros [html]]
            [dwarven-tavern.client.view.util :as util]))

(def sprites {:team1 "url(/images/team1.png)"
              :team2 "url(/images/team2.png)"})

(def dwarf-animation
  {:top        [{ :x 0  :y 96 :width 29 :height 32}]
   :bottom     [{ :x 0  :y 0  :width 29 :height 32}]
   :left       [{ :x 0  :y 32 :width 29 :height 32}]
   :right      [{ :x 0  :y 64 :width 29 :height 32}]
   :walktop    [{ :x 0  :y 96 :width 29 :height 32}
                { :x 33 :y 96 :width 29 :height 32}
                { :x 66 :y 96 :width 29 :height 32}]
   :walkbottom [{ :x 0  :y 0  :width 29 :height 32}
                { :x 33 :y 0  :width 29 :height 32}
                { :x 66 :y 0  :width 29 :height 32}]
   :walkleft   [{ :x 0  :y 32 :width 29 :height 32}
                { :x 33 :y 32 :width 29 :height 32}
                { :x 66 :y 32 :width 29 :height 32}]
   :walkright  [{ :x 0  :y 64 :width 29 :height 32}
                { :x 33 :y 64 :width 29 :height 32}
                { :x 66 :y 64 :width 29 :height 32}]})


(defn dwarf [team animation idx]
  (let [sprite-url (team sprites)
        sprite (-> dwarf-animation animation (nth idx))
        {:keys [x y width height]} sprite]
    [:.dwarf.team1 {:style {:background-image sprite-url
                      :width (str width "px")
                      :height (str height "px")
                      :background-position (str x "px " y "px")}}]))

(defn game-grid
  [own]
  (let [{:keys [width height team1 team2] } (->  own :rum/props :state deref)]
    [:table.grid
     [:tbody
      (for [row (range 0 height)]
        [:tr
         (for [column (range 0 width)]
           [:td (dwarf :team1 :bottom 0)])])]]))


(defn application
  [own]
  (html
   [:.container
    [:img#logo {:src "/images/tavern-logo.png"}]
    (game-grid own)]))

(def root
  (util/component
   {:render application
    :name "root"
    :mixins [util/cursored]}))



