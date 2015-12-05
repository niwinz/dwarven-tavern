(defproject dwarven-tavern "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "BSD (2-Clause)"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.189"]

                 [org.slf4j/slf4j-simple "1.7.13"]
                 [funcool/catacumba "0.9.0-SNAPSHOT"]
                 [funcool/cats "1.2.0"]
                 [funcool/promesa "0.6.0"]
                 [funcool/beicon "0.2.0"]
                 [funcool/postal "0.2.0"]

                 [rum "0.6.0"]
                 [figwheel-sidecar "0.5.0-2" :scope "test"]]
  ;; :main ^:skip-aot dwarven-tavern.server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
