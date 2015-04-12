(defproject shartfinder-ui "0.1.5"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.2"]
                 [hiccup "1.0.5"]
                 [liberator "0.12.2"]
                 [cheshire "5.4.0"]
                 [http-kit "2.1.18"]
                 [sonian/carica "1.1.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [com.taoensso/carmine "2.9.0"]
                 [postgresql/postgresql "9.3-1102.jdbc41"]
                 [ring/ring-devel "1.3.1"]
                 [ring/ring-defaults "0.1.4"]
                 [clj-http "1.0.1"]
                 [lib-noir "0.9.5"]
                 [danlentz/clj-uuid "0.1.5"]]

  :min-lein-version "2.0.0"

  :classpath-add ["config/config.edn"]

  :plugins [[lein-ring "0.9.3"]]

  :main ^{:skip-aot true} shartfinder-ui.core

  :uberjar-name "shartfinder-ui-standalone.jar")
