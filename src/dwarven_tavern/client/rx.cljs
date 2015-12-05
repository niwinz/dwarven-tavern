(ns dwarven-tavern.client.rx
  (:require [beicon.core :as rx]))

(defn from-promise
  [p]
  (js/Rx.Observable.fromPromise p))
