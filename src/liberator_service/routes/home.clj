(ns liberator-service.routes.home
  (:require [compojure.core :refer [defroutes ANY GET]]
            [liberator-service.views.layout :as layout]
            [liberator.core :refer [defresource resource request-method-in]]
            [clojure.data.json :as json]
            [ring.middleware.anti-forgery :refer :all]))

(def users (atom ["Jaymang" "Dogman"]))

(defresource get-users
  :allowed-methods [:get]
  :handle-ok (json/write-str @users)
  :available-media-types ["application/json"])

(defresource add-user
  :allowed-methods [:post]
  :post! (fn [context] (let [params (get-in context [:request :form-params])]
                         (swap! users conj (get params "user"))))
  :handle-created (fn [_]  (json/write-str @users))

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
