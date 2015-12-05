(ns dwarven-tavern.client.view.root
  (:require [sablono.core :as html :refer-macros [html]]
            [dwarven-tavern.client.view.util :as util]))

(defn game
  [own]
  (html
   [:h1 "TEST"]))

(def root
  (util/component
   {:render game
    :name "root"
    :mixins [util/cursored]}))



