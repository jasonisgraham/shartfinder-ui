(ns shartfinder-ui.routes.home
  (:require [compojure.core :refer [defroutes ANY GET]]
            [shartfinder-ui.views.layout :as layout]
            [liberator.core :refer [defresource resource request-method-in]]
            [cheshire.core :refer [generate-string parse-string]]
            [org.httpkit.server :as server]
            [shartfinder-ui.models.db :as db]
            [taoensso.carmine :as car :refer (wcar)]
            [clj-http.client :as client]
            [clojure.java.io :refer [file]]
            [noir.io :as io]))

(def server-connection {:pool {}
                        :spec {:host "pub-redis-18240.us-east-1-3.1.ec2.garantiadata.com"
                               :port 18240
                               :password "abc123"}})

(def ^:private encounter-id 69)
(def ^:private service-urls {:combatant "https://secure-beach-3319.herokuapp.com/"
                             :initiative ""})

(def ^:private channels {:encounter-created "encounter-created"
                         :initiative-rolled "roll-initiative"
                         :initiative-created "initiative-created"
                         :combatant-add-request "combatant-add-request"
                         :initiative-rolled-success "roll-initiative-success"
                         :error "error"})

(defmacro wcar* [& body]
  `(car/wcar server-connection ~@body))

;; (def users (atom (db/get-all-users)))
(def users (atom {}))
(def clients (atom {}))
(def combatants (atom #{}))

(defn- handle-roll-initiative-ws [context]
  (let [payload {:diceRoll (get-in context ["data" "diceRoll"])
                 :combatantName (get-in context ["data" "combatantName"])
                 :user (get-in context ["data" "user"])}]
    (println "payload: " payload)

    (wcar* (car/publish (:initiative-rolled channels)
                        (generate-string payload)))))

(defn- handle-roll-initiative-on-success [initiative-payload]
  (println "initiative-payload: " initiative-payload)
  (doseq [client @clients]
    (server/send! (key client)
                  (generate-string {:event-name "roll-initiative"
                                    :payload initiative-payload})
                  false)))

(defn- handle-add-combatant-ws [context]
  "FIXME awful naming!!"
  (let [payload {:maxHP (get-in context ["data" "maxHP"])
                 :combatantName (get-in context ["data" "combatantName"])
                 :user (get-in context ["data" "user"])}]

    (when-not (clojure.string/blank? (:combatantName payload))
      (println "add-combatant-payload: " (generate-string payload))
      ;; TODO this should be handled by someone. for now, the UI will just subscribe to its own message
      (wcar* (car/publish (:combatant-add-request channels)
                          (generate-string payload))))))

(defn- handle-add-combatant-on-success [combatant-payload]
  "FIXME awful naming!!"
  (println "combatant-payload: " combatant-payload)
  (swap! combatants conj combatant-payload)
  (println "combatants: " combatants)
  (doseq [client @clients]
    (server/send! (key client)
                  (generate-string {:event-name "add-combatant"
                                    :payload combatant-payload})
                  false)))

(defn- handle-start-encounter [_]
  (println "handling start encounter")
  (println "combatants:" @combatants)
  (let [payload {:encounterId encounter-id
                 :combatants @combatants}]
    (println "start-encounter payload: " payload)
    (wcar* (car/publish (:encounter-created channels)
                        (generate-string payload))))

  (doseq [client @clients]
    (server/send! (key client)
                  (generate-string {:event-name "start-encounter"
                                    :payload {:combatants @combatants}})
                  false)))

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
                               resource (context "resource")]
                           (cond
                             (= "roll-initiative" resource) (handle-roll-initiative-ws context)
                             (= "add-combatant" resource) (handle-add-combatant-ws context)
                             (= "start-encounter" resource) (handle-start-encounter context)
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
                         ;; (db/add-user new-user)
                         (swap! users conj new-user)
                         (doseq [client @clients]
                           (server/send! (key client)
                                         (generate-string {:event-name "add-user"
                                                           :payload (map :name @users)})
                                         false))))

  :handle-created (fn [_] (generate-string (map :name @users)))

  :malformed? (fn [context] (let [params (get-in context [:request :form-params])]
                              (or
                               (empty? (get params "user"))
                               (not= (get params "password") (get params "password_confirm")))))

  :handle-malformed "user name and password must be filled in and password must match"
  :available-media-types ["application/json"])

(defresource home
  :available-media-types ["text/html"]

  :handle-ok (fn [{{{ resource :resource} :route-params } :request}]
               (do (reset! users (db/get-all-users))
                   (reset! combatants #{}))
               (clojure.java.io/input-stream (io/get-resource "/home.html"))))

(defroutes home-routes
  (ANY "/" request home)
  (ANY "/add-user" request add-user)
  (ANY "/users" request get-users))

(defroutes ws-routes
  (GET "/ws" [] ws))

(defonce listener
  (car/with-new-pubsub-listener (:spec server-connection)
    {(:initiative-created channels) (fn f1 [[type match  content-json :as payload]]
                                      (when (instance? String  content-json)
                                        (let [content (parse-string content-json true)]
                                          (println "content: " content))))
     ;; TODO this is subscribing to unaltered message published by itself
     (:combatant-add-request channels) (fn f1 [[type match  content-json :as payload]]
                                         (when (instance? String  content-json)
                                           (let [content (parse-string content-json true)]
                                             (handle-add-combatant-on-success content))))

     (:initiative-rolled-success channels) (fn f1 [[type match  content-json :as payload]]
                                             (println "roll initiative on success")
                                             (when (instance? String  content-json)
                                               (let [content (parse-string content-json true)]
                                                 (handle-roll-initiative-on-success content))))}
    (car/subscribe (:initiative-created channels)
                   (:combatant-add-request channels)
                   (:initiative-rolled-success channels))))
