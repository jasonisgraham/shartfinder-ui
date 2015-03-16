(ns shartfinder-web.routes.home
  (:require [compojure.core :refer [defroutes ANY GET]]
            [shartfinder-web.views.layout :as layout]
            [liberator.core :refer [defresource resource request-method-in]]
            [cheshire.core :refer [generate-string]]
            [ring.middleware.anti-forgery :refer :all]
            [org.httpkit.server :as server]
            [shartfinder-web.models.db :as db]))

(def users (atom (db/get-all-users)))

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
  :handle-ok (generate-string (map :name @users))
  :available-media-types ["application/json"])

(defresource add-user
  :allowed-methods [:post]
  :post! (fn [context] (let [params (get-in context [:request :form-params])
                             new-user {:name (get params "user")
                                       :pass (get params "password")}]
                         (swap! users conj new-user)
                         (db/add-user new-user)))
  :handle-created (do
                    (doseq [client @clients]
                      (server/send! (key client) (generate-string (map :name @users)) false))
                    (fn [_] (generate-string (map :name @users))))


  :malformed? (fn [context] (let [params (get-in context [:request :form-params])]
                              (or
                               (empty? (get params "user"))
                               (empty? (get params "password"))
                               (empty? (get params "password_confirm"))
                               (not= (get params "password") (get params "password_confirm")))))
  :handle-malformed "user name and password must be filled in and password must match"
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
