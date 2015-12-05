(ns dwarven-tavern.client
  (:require [goog.dom :as gdom]
            [rum.core :as rum]
            [dwarven-tavern.client.view.root :as v]
            [dwarven-tavern.client.view.util :as util]))

(enable-console-print!)

(println "Hello world")


(defonce state (atom {:width 10
                      :height 10
                      :team1 [{:id :dialelo
                               :pos [1 2]
                               :dir :south}]
                      :team2 [{:id :alotor
                               :pos [3 4]
                               :dir :south}]
                      :barrel {:pos [5 5]}}))

(let [state (util/focus state)]
  (rum/mount (v/root {:state state})
             (gdom/getElement "app")))
