(ns shartfinder-ui.models.db
  (:require [clojure.java.jdbc :as sql]
            [carica.core :refer [config]]))

(def db-spec (config :database))

(defn migrated?
  ([]
   (or (migrated? "users") (migrated? "combatants")))
  ([table-name]
   (-> (sql/query db-spec
                  [(str "select count(*) from information_schema.tables "
                        "where table_name='" table-name "'")])
       first :count pos?)))

(defn drop-table [table-name]
  (println "dropping table " table-name)
  (sql/db-do-commands db-spec (str "drop table " table-name)))

(defn- migrate-for [table-name create-table-ddl]
  (when (not (migrated? table-name))
    (print "Creating database structure for " table-name "...") (flush)
    (sql/db-do-commands db-spec create-table-ddl)
    (println " done")))

(defn migrate
  ([truncate-all?] (when truncate-all?
                     (do
                       (drop-table "users")
                       (drop-table "combatants")))
   (migrate))
  ([]
   (migrate-for "users" (sql/create-table-ddl
                         :users
                         [:id  "serial PRIMARY KEY"]
                         [:name "varchar(32)"]
                         [:pass "varchar(100)"]))
   (migrate-for "combatants" (sql/create-table-ddl
                              :combatants
                              [:id "serial PRIMARY KEY"]
                              [:name "varchar(32)"]
                              [:max_hp :integer]
                              [:current_hp :integer]
                              [:race "varchar(20)"]
                              [:class "varchar(20)"]
                              [:status "varchar(100)"]))))

(defn get-user-by-name [name]
  (first (sql/query db-spec ["select * from users where name = ?" name])))

(defn- get-id [id-or-name]
  (if (instance? String id-or-name) (:id (get-user-by-name id-or-name)) id-or-name))

(defn get-user-by-id [id]
  (first (sql/query db-spec ["select * from users where id = ?" id])))

(defn add-user [user]
  (println "adding user " user)
  (sql/insert! db-spec :users (dissoc user :id)))

(defn add-combatant [combatant]
  (println "adding combatant: " combatant)
  (sql/insert! db-spec :combatants (dissoc combatant :id)))

(defn update-user [id-or-name things-to-update]
  (let [id (get-id id-or-name)]
    (sql/update! db-spec :users things-to-update ["id = ? " id])))

(defn delete-user [id-or-name]
  (let [id (get-id id-or-name)]
    (sql/delete! db-spec :users ["id = ?" id])))

(defn get-all-users []
  (sql/query db-spec ["select * from users"]))

(defn get-all-combatants []
  (sql/query db-spec ["select * from combatants"]))
