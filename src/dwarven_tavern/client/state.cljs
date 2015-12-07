(ns dwarven-tavern.client.state
  (:require [dwarven-tavern.client.rstore :as rs]
            [beicon.core :as rx]))

(defonce stream (rs/init {:location :home}))
(defonce state (atom {}))

(rx/to-atom stream state)
