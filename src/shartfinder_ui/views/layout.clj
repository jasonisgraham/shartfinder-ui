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

(defn render-users []
  (html5 [:div {:id "users-div"}
          [:h1 "Users"]
          [:p {:id "error"}]
          [:ul {:id "user-list"}]
          [:input {:type "text", :id "name", :placeholder "user name"}]
          [:br]
          [:input {:type "password", :id "password", :placeholder "password"}]
          [:br]
          [:input {:type "password", :id "password-confirm", :placeholder "re-enter password"}]
          [:br]
          [:button {:id "add-user-button" :onclick "addUser()"} "Add User"]
          [:button {:id "reset-users-button" :disabled true :onclick "resetUsers()"} "Reset Users"]]))

(defn render-combatants [users]
  (html5 [:div {:id "combatants-div"}
          [:h1 "Combatants"]
          [:ul {:id "combatant-list"}]
          [:input {:type "text" :id "combatants_combatant-name" :placeholder "combatant name"}]
          [:input {:type "number" :id "combatants_max-hp" :placeholder "max HP"}]
          [:select {:id "combatants_user"}
           (html5 (select-options (map :name users)))]
          [:br]
          [:button {:id "add-combatant-button" :onclick "addCombatant()"} "Add Combatant"]
          [:button {:id "start-encounter-button" :onclick "startEncounter()"} "Start Encounter"]]))

(defn render-roll-initiative [combatants]
  (html5 [:div {:id "roll-initiative-div", :style "display: none"}
          [:h1 "Roll Initiative"]
          [:p {:id "initiative-message"}]
          [:div {:id "combatant-wait-list-div"}
           [:h3 "Waiting on these guys"]
           [:ul {:id "combatant-wait-list-ul"}]]
          [:ul {:id "initiative-rolls"}]
          [:input {:type "text" :id "initiative_user" :placeholder "user id"}]
          [:select {:id "initiative_combatant-name"}
           (html5 (select-options (map :name combatants)))]
          [:input {:type "number" :id "dice-roll" :placeholder "dice roll"}]
          [:br]
          [:button {:onclick "rollInitiative()"} "Roll Initiative"]]))

(defn render-round []
  (html5 [:div {:id "round-div" :style "display: none"}
          [:h1 "Round"]
          [:ul {:id "round-order"}]]))

(defn main [{combatants :combatants, users :users}]
  (common
   (html5 (render-users )
          (println "users: " users)
          (println "combatants: " combatants)
          (render-combatants @users)
          (render-roll-initiative @combatants)
          (render-round))))
