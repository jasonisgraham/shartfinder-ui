(ns liberator-service.views.layout
  (:require [hiccup.page :refer [html5 include-css include-js]]))

(defn common [& body]
  (html5
   [:head
    [:title "Liberator Example"]
    (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js")
    (include-js "/js/home.js")
    (include-css "/css/screen.css")]
   [:body body]))

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
          [:button {:onclick "addUser()"} "Add User"])))
