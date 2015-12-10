(defproject dwarven-tavern "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "BSD (2-Clause)"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.189"
                  :exclusions [org.clojure/tools.reader]]
                 [org.slf4j/slf4j-simple "1.7.13"]
                 [org.clojure/core.async "0.2.374"]

                 [funcool/catacumba "0.9.0-SNAPSHOT"]

                  ;; :exclusions [com.fasterxml.jackson.core/jackson-core]]
                 [funcool/cats "1.2.0"]
                 [funcool/promesa "0.6.0"]
                 [funcool/beicon "0.3.0-SNAPSHOT"]
                 [funcool/postal "0.2.0"]

                 [bouncer "0.3.3" :exclusions [clj-time]]
                 [rum "0.6.0"]
                 [figwheel-sidecar "0.5.0-2" :scope "test"]
                 [bidi "1.21.1"]]
  ;; :main ^:skip-aot dwarven-tavern.server.main
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
