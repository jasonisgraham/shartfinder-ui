(defproject shartfinder-web "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.2"]
                 [hiccup "1.0.5"]
                 [liberator "0.12.2"]
                 [cheshire "5.4.0"]
                 [ring/ring-defaults "0.1.4"]
                 [http-kit "2.1.18"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [ring-cors "0.1.6"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [postgresql/postgresql "9.3-1102.jdbc41"]
                 [com.taoensso/carmine "2.9.0"]
                 [clj-http "1.0.1"]]

  :plugins [[lein-ring "0.8.12"]]

  :main shartfinder-web.core

  :profiles {:uberjar {:aot :all}
             :production {:ring
                          {:open-browser? false
                           :stacktraces? false
                           :auto-reload? false}}
             :dev {:dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.1"]]}})
