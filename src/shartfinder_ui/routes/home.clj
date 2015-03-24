(ns shartfinder-ui.routes.home
  (:require [compojure.core :refer [defroutes ANY GET]]
            [shartfinder-ui.views.layout :as layout]
            [liberator.core :refer [defresource resource request-method-in]]
            [cheshire.core :refer [generate-string parse-string]]
            [org.httpkit.server :as server]
            [shartfinder-ui.models.db :as db]
            [taoensso.carmine :as car :refer (wcar)]
            [clojure.java.io :refer [file]]
            [noir.io :as io]
            [shartfinder-ui.common :refer :all]))

(def ^:private encounter-id 69)

(def users (atom (db/get-all-users)))
(def clients (atom {}))
(def combatants (atom #{}))

(defn- ws-send-to-clients [event-name payload]
  (println "ws-send-to-clients.  event-name: '" event-name "' payload: " payload)
  (doseq [client @clients]
    (server/send! (key client)
                  (generate-string {:event-name event-name
                                    :payload payload})
                  false)))

(defn- handle-roll-initiative-request [context]
  (let [payload {:diceRoll (get-in context ["data" "diceRoll"])
                 :combatantName (get-in context ["data" "combatantName"])
                 :user (get-in context ["data" "user"])}]
    (println "payload: " payload)

    (wcar* (car/publish (:roll-initiative-command channels)
                        (generate-string payload)))))

(defn- handle-roll-initiative-response [initiative-payload]
  (println "here handle-roll-initiative-on-success")
  (ws-send-to-clients "roll-initiative" initiative-payload))

(defn- handle-add-combatant-request [context]
  (let [payload {:maxHP (get-in context ["data" "maxHP"])
                 :combatantName (get-in context ["data" "combatantName"])
                 :user (get-in context ["data" "user"])}]

    (when-not (clojure.string/blank? (:combatantName payload))
      (println "add-combatant-payload: " (generate-string payload))
      (wcar* (car/publish (:combatant-added channels)
                          (generate-string payload))))))

(defn- handle-add-combatant-response [combatant-payload]
  (swap! combatants conj combatant-payload)
  (ws-send-to-clients "add-combatant" combatant-payload))

(defn- handle-initiative-created-reponse [initiative-created-payload]
  (ws-send-to-clients "initiative-created" initiative-created-payload))

(defn- handle-start-encounter-request [_]
  (println "handling start encounter")
  (println "combatants:" @combatants)
  (let [payload {:encounterId encounter-id
                 :combatants @combatants}]
    (println "start-encounter payload: " payload)
    (wcar* (car/publish (:encounter-created channels)
                        (generate-string payload))))

  (ws-send-to-clients "start-encounter" {:combatants @combatants}))

;; (defn- handle-start-encounter-on-success [_]
;; (map #(assoc {} (:combatantName %) %) @combatants)
;;   )

(defn ws [request]
  (server/with-channel request con
    (swap! clients assoc con true)
    (println con " connected")
    (server/on-receive con
                       (fn [context-str]
                         (let [context (parse-string context-str)
                               event-name (context "eventName")]
                           (cond
                             (= "roll-initiative" event-name) (handle-roll-initiative-request context)
                             (= "add-combatant" event-name) (handle-add-combatant-request context)
                             (= "start-encounter" event-name) (handle-start-encounter-request context)
                             :else (println "not found")))))

    (server/on-close con (fn [status]
                           (swap! clients dissoc con)
                           (println con " disconnected. status: " status)))))

(defresource get-users
  :allowed-methods [:get]
  :handle-ok (fn [_] (generate-string (map :name @users)))
  :available-media-types ["application/json"])

(defresource add-user
  :allowed-methods [:post]
  :post! (fn [context] (let [params (get-in context [:request :form-params])
                             new-user {:name (get params "user")
                                       :pass (get params "password")}]
                         (db/add-user new-user)
                         (swap! users conj new-user)
                         (ws-send-to-clients "add-user" (map :name @users))))

  :handle-created (fn [_] (generate-string (map :name @users)))

  :malformed? (fn [context] (let [params (get-in context [:request :form-params])]
                              (or
                               (empty? (get params "user"))
                               (not= (get params "password")
                                     (get params "passwordConfirm")))))

  :handle-malformed "user name and password must be filled in and password must match"
  :available-media-types ["application/json"])

(defresource home
  :service-available? true
  :allowed-methods [:get]
  :etag "fixed-etag"
  :available-media-types ["text/html"]

  :handle-ok (fn [{{{ resource :resource} :route-params } :request}]
               (clojure.java.io/input-stream (io/get-resource "/home.html"))))


(defresource home-test
  :service-available? true
  :allowed-methods [:get]
  :handle-service-not-available "service not available, yo!"
  :handle-ok (do (reset! users (db/get-all-users))
                 (reset! combatants #{})
                 (layout/main))
  :etag "fixed-etag"
  :available-media-types ["text/html"])

(defroutes home-routes
  (ANY "/" request home)
  (ANY "/test" request home-test)
  (ANY "/add-user" request add-user)
  (ANY "/users" request get-users))

(defroutes ws-routes
  (GET "/ws" [] ws))

(defonce listener
  (car/with-new-pubsub-listener (:spec server-connection)
    {(:initiative-created channels) (handle-pubsub-subscribe handle-initiative-created-reponse)
     (:combatant-added channels) (handle-pubsub-subscribe handle-add-combatant-response)
     (:initiative-rolled channels) (handle-pubsub-subscribe handle-roll-initiative-response)}

    (car/subscribe (:initiative-created channels)
                   (:combatant-added channels)
                   (:initative-rolled channels))))
