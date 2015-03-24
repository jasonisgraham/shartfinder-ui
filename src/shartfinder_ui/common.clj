(ns shartfinder-ui.common
  (:require [carica.core :refer [config]]
            [cheshire.core :refer [generate-string parse-string]]))

(def server-connection {:pool {}
                        :spec (config :redis :spec)})

(def service-urls (config :service-urls))

(def channels {:encounter-created "encounter-created"

               :combatant-added "combatant-added"
               :add-combatant-command "add-combatant-command"

               :initiative-rolled "initiative-rolled"
               :roll-initiative-command "roll-initiative-command"
               :initiative-created "initiative-created"

               :error "error"})

(defmacro wcar* [& body]
  `(car/wcar server-connection ~@body))

(defmacro handle-pubsub-subscribe [handle-event-fn]
  `(fn f1 [[type# match# content-json# :as payload#]]
     (when (instance? String content-json#)
       (let [content# (parse-string content-json# true)]
         (println "payload: " payload#)
         (~handle-event-fn content#)))))
