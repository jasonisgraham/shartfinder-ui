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
            [shartfinder-ui.common :refer :all]
            [carica.core :refer [config]]
            [clj-http.client :as client]
            [clj-uuid :as uuid]))

(def users (atom (db/get-all-users)))
(def clients (atom {}))
(def combatants (atom #{}))
(def encounter-id (atom nil))
(def initiative-rolls (atom #{}))
(def round-info (atom {}))
(def turn-info (atom {}))

(defn initiative-created? []
  "FIXME: i dont want to ping initiative service for this info do i?"
  (let [url (str (:initiative service-urls))
        response (client/get url {:throw-exceptions false})
        ordered-initiative (:ordered-initiative (parse-string (:body response) true))]
    (not (empty? ordered-initiative))))

(defn encounter-created? []
  (not (nil? @encounter-id)))

(defn get-encounter-phase
  ([encounter-id]
   "TODO not implemented"
   (get-encounter-phase))
  ([]
   (cond
     (initiative-created?) "round"
     (encounter-created?) "initiative"
     :else nil)))

(defn construct-encounter-payload
  ([]
   (construct-encounter-payload nil))
  ([event-name]
   (let [encounter-phase (get-encounter-phase)]
     (construct-encounter-payload event-name encounter-phase)))
  ([event-name encounter-phase]
   {:eventName event-name
    :encounterPhase encounter-phase
    :encounterId @encounter-id
    :combatants @combatants
    :initiativeRolls @initiative-rolls
    ;; :whoHasntRolled nil
    :roundStatus {:round @round-info
                  :turn @turn-info}}))

(defn- ws-send-to-clients [event-name payload]
  (println "ws-send-to-clients.  eventName: '" event-name "' payload: " payload)
  (doseq [client @clients]
    (server/send! (key client)
                  (generate-string {:eventName event-name
                                    :payload payload})
                  false)))

(defn- ws-send-encounter-status-to-clients [event-name]
  (let [payload (construct-encounter-payload event-name)]
    (println "ws-send-encounter-status-to-clients ... payload: " payload)
    (ws-send-to-clients event-name (generate-string payload))))

(defn- handle-roll-initiative-command [context]
  (let [initiative-info {:diceRoll (get-in context ["data" "diceRoll"])
                         :combatantName (get-in context ["data" "combatantName"])
                         :user (get-in context ["data" "user"])}]
    (println "handle-roll-initiative-command ... payload: " initiative-info)

    (wcar* (car/publish (:roll-initiative-command channels)
                        (generate-string initiative-info)))))

(defn- handle-initiative-rolled [initiative-payload]
  (println "handle-initiative-rolled: payload ... " initiative-payload)
  (swap! initiative-rolls conj initiative-payload)
  (ws-send-encounter-status-to-clients "initiative-rolled"))

(defn- handle-round-started [context]
  ;; (ws-send-encounter-status-to-clients "")
  )

(defn- handle-turn-started [context])

(defn- handle-add-combatant-command [context]
  (let [payload {:maxHP (get-in context ["data" "maxHP"])
                 :combatantName (get-in context ["data" "combatantName"])
                 :user (get-in context ["data" "user"])}]

    (when-not (clojure.string/blank? (:combatantName payload))
      (println "add-combatant-command-payload: " (generate-string payload))
      (wcar* (car/publish (:add-combatant-command channels)
                          (generate-string payload))))))

;; this is here cuz combatant service doesn't exist yet
(defn- handle-add-combatant-request [context]
  (println "in service-placeholders handle-add-combatant-request")
  (wcar* (car/publish (:combatant-added channels)
                      (generate-string context))))

(defn- handle-combatant-added [combatant-payload]
  (swap! combatants conj combatant-payload)
  (ws-send-encounter-status-to-clients "combatant-added"))

(defn- handle-initiative-created-reponse [initiative-created-payload]
  (ws-send-to-clients "initiative-created" initiative-created-payload))

(defn handle-start-encounter-command [_]
  (println "handling start encounter")
  (println "combatants:" @combatants)
  ;; FIXME
  (reset! encounter-id (.toString (uuid/v1)))
  (let [payload {:encounterId @encounter-id
                 :combatants @combatants}]

    (println "start-encounter payload: " payload)
    (wcar* (car/publish (:encounter-created channels)
                        (generate-string payload))))

  (ws-send-encounter-status-to-clients "start-encounter"))

(defn ws [request]
  (server/with-channel request con
    (swap! clients assoc con true)
    (println con " connected")
    (server/on-receive con
                       (fn [context-str]
                         (println "context-str: " context-str)
                         (let [context (parse-string context-str)
                               event-name (context "eventName")]
                           (println "event-name: " event-name)
                           (cond
                             (= "add-combatant-command" event-name) (handle-add-combatant-command context)
                             (= "start-encounter" event-name) (handle-start-encounter-command context)
                             (= "roll-initiative-command" event-name) (handle-roll-initiative-command context)
                             (= "get-encounter-data" event-name) (ws-send-encounter-status-to-clients nil)
                             :else (println "event-name: '" event-name "' not expected")))))

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
  :handle-ok (layout/main (construct-encounter-payload))
  :etag "fixed-etag"
  :available-media-types ["text/html"])

(defresource get-encounter-data
  :service-available? true
  :allowed-methods [:get]
  :handle-service-not-available "service not available, yo!"
  :handle-ok (generate-string (construct-encounter-payload))
  :etag "fixed-etag"
  :available-media-types ["text/html"])

(defresource clear-in-memory-data
  :service-available? true
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok (do (reset! users (db/get-all-users))
                 (reset! combatants #{})
                 (reset! encounter-id nil)
                 (reset! initiative-rolls #{})
                 (wcar* (car/publish (:encounter-created channels)
                                     (generate-string {})))
                 nil))

(defroutes home-routes
  (ANY "/" request home)
  (ANY "/test" request home-test)
  (ANY "/add-user" request add-user)
  (ANY "/users" request get-users)
  (ANY "/in-memory-data/clear" request clear-in-memory-data)
  (GET "/encounter-data" request get-encounter-data)
  (ANY "/encounter-phase" request get-encounter-phase))

(defroutes ws-routes
  (GET "/ws" [] ws))

(defonce listener
  (car/with-new-pubsub-listener (:spec server-connection)
    {
     (:initiative-created channels) (handle-pubsub-subscribe handle-initiative-created-reponse)
     (:combatant-added channels) (handle-pubsub-subscribe handle-combatant-added)
     (:initiative-rolled channels) (handle-pubsub-subscribe handle-initiative-rolled)
     (:round-started channels) (handle-pubsub-subscribe handle-round-started)
     (:turn-started channels) (handle-pubsub-subscribe handle-turn-started)
     ;; this is here cuz combatant service doesn't exist yet
     (:add-combatant-command channels) (handle-pubsub-subscribe handle-add-combatant-request)
     }

    (car/subscribe (:initiative-created channels)
                   (:combatant-added channels)
                   (:initiative-rolled channels)
                   (:round-started channels)
                   (:turn-started channels)
                   ;; this is here cuz combatant service doesn't exist yet
                   (:add-combatant-command channels))))
