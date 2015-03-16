(ns liberator-service.core
  (:require [org.httpkit.server :as server]
            [ring.middleware.reload :as reload]
            [compojure.handler :as compojure-handler]
            [ring.middleware.defaults :as defaults]
            [liberator-service.handler :refer [ app ]])
  (:use [ring.middleware file-info file]))


(defonce server (atom nil))
(def in-dev? false)

(defn handler []
  (if in-dev?
    (reload/wrap-reload (compojure-handler/site #'app))
    (compojure-handler/site app)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 8080)]
    (reset! server (server/run-server (handler) {:port port}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  (start-server args))
