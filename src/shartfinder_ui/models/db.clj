(ns shartfinder-ui.models.db
  (:require [clojure.java.jdbc :as sql]))

(let [db-host "localhost"
      db-port 5432
      db-name "liberator_service"]
  (def db-spec {:subprotocol "postgresql"
                :subname (str "//" db-host ":" db-port "/" db-name)
                :user "jason"
                :password "stanksource"}))

(defn migrated? [table-name]
  (-> (sql/query db-spec
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='" table-name "'")])
      first :count pos?))

(defn- migrate-for [table-name create-table-ddl]
  (when (not (migrated? table-name))
    (print "Creating database structure for " table-name "...") (flush)
    (sql/db-do-commands db-spec create-table-ddl)
    (println " done")))

(defn migrate []
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
                             [:status "varchar(100)"])))

(defn get-user-by-name [name]
  (first (sql/query db-spec ["select * from users where name = ?" name])))

(defn- get-id [id-or-name]
  (if (instance? String id-or-name) (:id (get-user-by-name id-or-name)) id-or-name))

(defn get-user-by-id [id]
  (first (sql/query db-spec ["select * from users where id = ?" id])))

(defn add-user [user]
  (sql/insert! db-spec :users (dissoc user :id)))

(defn update-user [id-or-name things-to-update]
  (let [id (get-id id-or-name)]
    (sql/update! db-spec :users things-to-update ["id = ? " id])))

(defn delete-user [id-or-name]
  (let [id (get-id id-or-name)]
    (sql/delete! db-spec :users ["id = ?" id])))

(defn get-all-users []
  (sql/query db-spec ["select * from users"]))