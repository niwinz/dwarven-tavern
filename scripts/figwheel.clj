(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra])

(ra/start-figwheel!
  {:figwheel-options {:css-dirs ["resources/public/css"]}
   :build-ids ["dev"]
   :all-builds
   [{:id "dev"
     :figwheel true
     :source-paths ["src"]
     :compiler {:main 'dwarven-tavern.client.main
                :asset-path "js"
                :optimizations :none
                :pretty-print true
                :language-in  :ecmascript5
                :language-out :ecmascript5
                :output-to "resources/public/js/main.js"
                :output-dir "resources/public/js"
                :verbose true}}]})

(ra/cljs-repl)
