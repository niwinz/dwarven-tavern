(ns dwarven-tavern.client.main
  (:require [goog.dom :as dom]
            [cats.core :as m]
            [cats.labs.lens :as l]
            [rum.core :as rum]
            [promesa.core :as p]
            [bidi.router :as bidi]
            [beicon.core :as rx]
            [dwarven-tavern.client.game :as g]
            [dwarven-tavern.client.views :as v]
            [dwarven-tavern.client.state :as s]
            [dwarven-tavern.client.rstore :as rs]))

(enable-console-print!)

;; DEBUG
(rx/on-value s/stream #(println "state:" %))
(rs/emit! (g/load-rooms))

(let [el (dom/getElement "app")]
  (rum/mount (v/root) el))

