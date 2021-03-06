(ns shartfinder-ui.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [shartfinder-ui.routes.home :refer [home-routes ws-routes]]
            [ring.middleware.reload :as reload]))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes ws-routes home-routes app-routes)
      reload/wrap-reload
      (handler/site)))
