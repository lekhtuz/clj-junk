(ns mgrapi.routes.home
  (:use [mgrapi.spring :as spring])
  (:require [compojure.core :refer :all]
            [mgrapi.views.layout :as layout])
)

(defn home [] 
  (layout/common [:h1 "home dir!!!!!!!"]))

(defn account [id]
  (let
    [
     account-manager (spring/get-bean "accountManager")
     user-account (bean (.getUserAccount account-manager id))
    ]
    (layout/common [:h2 (str "id=" id "<br>" user-account)])
  )
)

(defroutes home-routes
  (GET "/" [] (home))
  (GET "/account/:id" [id] (account id))
)
