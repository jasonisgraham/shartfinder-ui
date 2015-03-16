(ns liberator-service.routes.home
  (:require [compojure.core :refer [defroutes ANY GET]]
            [liberator-service.views.layout :as layout]
            [liberator.core :refer [defresource resource request-method-in]]
            [cheshire.core :refer [generate-string]]
            [ring.middleware.anti-forgery :refer :all]
            [org.httpkit.server :as server]))

(def users (atom ["Jaymang" "Dogman"]))

(def clients (atom {}))
(defn ws [request]
  (server/with-channel request con
    (swap! clients assoc con true)
    (println con " connected")
    (server/on-close con (fn [status]
                           (swap! clients dissoc con)
                           (println con " disconnected. status: " status)))))

(defresource get-users
  :allowed-methods [:get]
  :handle-ok (generate-string @users)
  :available-media-types ["application/json"])

(defresource add-user
  :allowed-methods [:post]
  :post! (fn [context] (let [params (get-in context [:request :form-params])]
                         (swap! users conj (get params "user"))))
  :handle-created (do
                    (doseq [client @clients]
                      (server/send! (key client) (generate-string @users) false))
                    (fn [_] (generate-string @users)))


  :malformed? (fn [context] (let [params (get-in context [:request :form-params])]
                              (empty? (get params "user"))))
  :handle-malformed "user name cannot be empty!"
  :available-media-types ["application/json"])

(defresource home
  :service-available? true
  :allowed-methods [:get]
  :handle-service-not-available "service not available, yo!"
  :handle-ok (layout/main)
  :etag "fixed-etag"
  :available-media-types ["text/html"])


(defroutes home-routes
  (ANY "/" request home)
  (ANY "/add-user" request add-user)
  (ANY "/users" request get-users))

(defroutes ws-routes
  (GET "/happiness" [] ws))
