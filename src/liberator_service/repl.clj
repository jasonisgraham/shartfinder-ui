;; (ns liberator-service.repl
;;   (:require [org.httpkit.server :as server]
;;             [ring.middleware.reload :as reload]
;;             [ring.middleware.defaults :as defaults])
;;   (:use liberator-service.handler
;;         [ring.middleware file-info file]))

;; (defonce server (atom nil))
;; (def is-dev-env? false)

;; (defn get-handler []
;;   ;; #'app expands to (var app) so that when we reload our code,
;;   ;; the server is forced to re-resolve the symbol in the var
;;   ;; rather than having its own copy. When the root binding
;;   ;; changes, the server picks it up without having to restart.
;;   ;; (if is-dev-env?
;;   ;;   (-> #'app
;;   ;;       (defaults/wrap-defaults (-> defaults/site-defaults (assoc-in [:security :anti-forgery] false)))
;;   ;;       (reload/wrap-reload))

;;     (-> #'app
;;         ;; Makes static assets in $PROJECT_DIR/resources/public/ available.
;;         (wrap-file "resources")
;;         ;; Content-Type, Content-Length, and Last Modified headers for files in body
;;         (wrap-file-info)))

;; (defn start-server
;;   "used for starting the server in development mode from REPL"
;;   [& [port]]
;;   (let [port (if port (Integer/parseInt port) 8080)]
;;     (reset! server
;;             (server/run-server (get-handler)
;;                    {:port port
;;                     :init init
;;                     :destroy destroy
;;                     :join true}))
;;     (println (str "You can view the site at http://localhost:" port))))

;; (defn stop-server []
;;   (when-not (nil? @server)
;;     (@server :timeout 100)
;;     (reset! server nil)))

(ns liberator-service.repl
  (:use liberator-service.handler
        ring.server.standalone
        [ring.middleware file-info file]))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'app
    ; Makes static assets in $PROJECT_DIR/resources/public/ available.
    (wrap-file "resources")
    ; Content-Type, Content-Length, and Last Modified headers for files in body
    (wrap-file-info)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 8080)]
    (reset! server
            (serve (get-handler)
                   {:port port
                    :init init
                    :auto-reload? true
                    :destroy destroy
                    :join true}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))
