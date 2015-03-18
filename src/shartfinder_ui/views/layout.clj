(ns shartfinder-ui.views.layout
  (:require [hiccup.page :refer :all]
            [hiccup.form :refer :all]
            [shartfinder-ui.models.db :as db]))

(defn common [& body]
  (html5
   [:head
    [:title "Shartfinder Test"]
    (include-js "https://code.jquery.com/jquery-2.1.3.js")
    (include-js "/js/home.js")
    (include-css "/css/screen.css")]
   [:body body]))

(defn roll-initiative []
  (html5 [:h1 "Roll Initiative" " (async + websockets) "]
         [:p {:id "initiative-message"}]
         [:ul {:id "initiative-rolls"}]
         [:input {:type "text" :id "user-id" :placeholder "user id"}]
         [:input {:type "text" :id "combatant-name" :placeholder "combatant name"}]
         [:input {:type "number" :id "dice-roll" :placeholder "dice roll"}]
         [:br]
         [:button {:onclick "rollInitiative()"} "Roll Initiative"]))

(defn add-user []
  (html5
   [:h1 "Users"]
   [:p {:id "error"}]
   [:ul {:id "user-list"}]
   [:input {:type "text", :id "name", :placeholder "user name"}]
   [:br]
   [:input {:type "password", :id "password", :placeholder "password"}]
   [:br]
   [:input {:type "password", :id "password-confirm", :placeholder "re-enter password"}]
   [:br]
   [:button {:onclick "addUser()"} "Add User"]))

(defn get-user [])

(defn add-combatant []
  (html5 [:h1 "Combatants"]
         [:ul {:id "combatant-list"}]
         [:input {:type "text" :id "name" :placeholder "combatant name"}]
         [:input {:type "number" :id "maxHP" :placeholder "max HP"}]
         (let [users (db/get-all-users)]
           [:select
            (html5 (select-options (map :name users)))])
         [:br]
         [:button {:onclick "addCombatant()"} "Add Combatant"]))

(defn get-combatant [])

(defn main []
  (common
   (html5 (add-user)
          (add-combatant)
          (roll-initiative))))

;; (ns shartfinder-ui.views.layout
;;   (:require [hiccup.page :refer :all]
;;             [hiccup.form :refer :all]
;;             [shartfinder-ui.models.db :as db]))

;; (defn common [& body]
;;   (html5
;;    [:head
;;     [:title "Shartfinder Test Page"]
;;     (include-js "https://code.jquery.com/jquery-2.1.3.js")
;;     (include-js "/js/home.js")
;;     (include-css "/css/screen.css")]
;;    [:body body]))

;; (defn roll-initiative []
;;   (html5 [:h1 "Roll Initiative" " (async + websockets) "]
;;          [:p {:id "initiative-message"}]
;;          [:ul {:id "initiative-rolls"}]
;;          [:input {:type "text" :id "user-id" :placeholder "user id"}]
;;          [:input {:type "text" :id "combatant-name" :placeholder "combatant name"}]
;;          [:input {:type "number" :id "dice-roll" :placeholder "dice roll"}]
;;          [:br]
;;          [:button {:onclick "rollInitiative()"} "Roll Initiative"]))

;; (defn add-user []
;;   (html5
;;    [:h1 "Users"]
;;    [:p {:id "error"}]
;;    [:ul {:id "user-list"}]
;;    [:input {:type "text", :id "name", :placeholder "user name"}]
;;    [:br]
;;    [:input {:type "password", :id "password", :placeholder "password"}]
;;    [:br]
;;    [:input {:type "password", :id "password-confirm", :placeholder "re-enter password"}]
;;    [:br]
;;    [:button {:onclick "addUser()"} "Add User"]))

;; (defn get-user [])

;; (defn add-combatant []
;;   (html5 [:h1 "Combatants"]
;;          [:ul {:id "combatant-list"}]
;;          [:input {:type "text" :id "name" :placeholder "combatant name"}]
;;          [:input {:type "number" :id "maxHP" :placeholder "max HP"}]
;;          (let [users (db/get-all-users)]
;;            [:select
;;             (html5 (select-options (map :name users)))])
;;          [:br]
;;          [:button {:onclick "addCombatant()"} "Add Combatant"]))

;; (defn get-combatant [])

;; (defn main []
;;   (common
;;    (html5 (add-user)
;;           (add-combatant)
;;           (roll-initiative))))
