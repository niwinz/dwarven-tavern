(ns dwarven-tavern.client.view.root
  (:require [sablono.core :as html :refer-macros [html]]
            [dwarven-tavern.client.view.util :as util]))

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
   :south [{ :x 0  :y 0  :width 33 :height 32}
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


(defn render-barrel [direction]
  (println "Render barrel")
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

(defn game-grid
  [own]
  (let [{:keys [width height team1 team2 barrel] } (->  own :rum/props :state deref)
        {[posx-team1  posy-team1] :pos dir-team1 :dir} (first team1)
        {[posx-team2  posy-team2] :pos dir-team2 :dir} (first team2)
        {[posx-barrel posy-barrel] :pos} barrel]
    (println posx-barrel posy-barrel)
    [:table.grid
     [:tbody
      (for [row (range 0 height)]
        [:tr
         (for [column (range 0 width)]
           [:td
            (when (and (= row posy-barrel) (= column posx-barrel))
              (render-barrel :north))
            (when (and (= row posy-team1) (= column posx-team1))
              (render-dwarf :team1 dir-team1 0))
            (when (and (= row posy-team2) (= column posx-team2))
              (render-dwarf :team2 dir-team2 0))
            ])])]]))


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



