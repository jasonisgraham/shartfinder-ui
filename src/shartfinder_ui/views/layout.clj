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
         [:input {:type "text" :id "initiative_user" :placeholder "user id"}]
         [:input {:type "text" :id "combatant-name" :placeholder "combatant name"}]
         [:input {:type "number" :id "dice-roll" :placeholder "dice roll"}]
         [:br]
         [:button {:onclick "rollInitiative()"} "Roll Initiative"]))

(defn users []
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
   [:button {:onclick "addUser()"} "Add User"]
   [:button {:onclick "resetUsers()"} "Reset Users"]))

(defn combatants []
  (html5 [:h1 "Combatants"]
         [:ul {:id "combatant-list"}]
         [:input {:type "text" :id "combatants_combatant-name" :placeholder "combatant name"}]
         [:input {:type "number" :id "combatants_max-hp" :placeholder "max HP"}]
         (let [users (db/get-all-users)]
           [:select {:id "combatants_user"}
            (html5 (select-options (map :name users)))])
         [:br]
         [:button {:onclick "addCombatant()"} "Add Combatant"]))

(defn main []
  (common
   (html5 (users)
          (combatants)
          (roll-initiative))))
