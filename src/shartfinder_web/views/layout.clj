(ns shartfinder-web.views.layout
  (:require [hiccup.page :refer [html5 include-css include-js]]))

(defn common [& body]
  (html5
   [:head
    [:title "Liberator Example"]
    (include-js "https://code.jquery.com/jquery-2.1.3.js")
    (include-js "/js/home.js")
    (include-css "/css/screen.css")]
   [:body body]))

(defn roll-initiative []
  (html5 [:h1 "Roll Initiative"]
         [:p {:id "initiative-message"}]
         [:ul {:id "initiative-rolls"}]
         [:input {:type "text" :id "user-id" :placeholder "user id"}]
         [:input {:type "text" :id "combatant-name" :placeholder "combatant name"}]
         [:input {:type "number" :id "dice-roll" :placeholder "dice roll"}]
         [:br]
         [:button {:onclick "rollInitiative()"} "Roll Initiative"]))

(defn main []
  (common
   (html5 [:h1 "Current Users"]
          [:p {:id "error"}]
          [:ul {:id "user-list"}]
          [:input {:type "text", :id "name", :placeholder "user name"}]
          [:br]
          [:input {:type "password", :id "password", :placeholder "password"}]
          [:br]
          [:input {:type "password", :id "password-confirm", :placeholder "re-enter password"}]
          [:br]
          [:button {:onclick "addUser()"} "Add User"]
          [:br] [:br]
          (roll-initiative))))
