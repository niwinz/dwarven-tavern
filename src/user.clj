(ns user
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer (refresh)]
            [clojure.pprint :refer (pprint)]
            [clojure.core.async :as a]
            [dwarven-tavern.server :as app]
            [dwarven-tavern.server.api :as api]
            [dwarven-tavern.server.state :as s]
            [dwarven-tavern.server.game :as g]))

(def system nil)

(defn init
  []
  (alter-var-root #'system
    (constantly (app/system))))

(defn start
  []
  (alter-var-root #'system component/start))

(defn stop
  []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go
  []
  (init)
  (start))

(defn reset
  []
  (stop)
  (refresh :after 'user/go))

#_(do
  (def state
    (-> {:rooms {}}
        (s/transition [:room/join {:room :test :player :foo}])
        (s/transition [:room/join {:room :test :player :bar}])
        (s/transition [:game/move {:room :test :player :foo :dir :south}])
        ))

  (def state2
    (-> state
        (s/transition [:room/reset :test])))

  (def room (get-in state [:rooms :test]))

  (defn materialize
    []
    (println "Before: ")
    (pprint room)
    (let [room (g/materialize-moviments room)]
      (println "After: ")
      (pprint room)
      ))

  (defn start-game
    []
    (reset! s/db {:rooms {}})
    (api/handler nil {:dest :room/join
                      :type :novelty
                      :data {:room :test :player :foo}})

    (api/handler nil {:dest :room/join
                      :type :novelty
                      :data {:room :test :player :bar}})

    (api/handler nil {:dest :game/start
                      :type :novelty
                      :data {:room :test}})
    )

  (defn stop-game
    []
    (let [room (s/get-room-by-id :test)]
      (a/close! (:closed room))))
  )






