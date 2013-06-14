(ns mgrapi.handler
  (:use 
    [compojure.core :as comp]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [mgrapi.spring :as spring]
    [ring.middleware.json :as json]
    [ring.middleware.resource :as resource]
  )

  (:require [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [mgrapi.routes.home :refer [home-routes]]
  )
)

(def context-files
  [
   "applicationContext.xml"
   "context-erights.xml"
   "context-transformers.xml"
  ]
)

(defn init []
  (println "mgrapi is starting")
  (spring/&init context-files)
)

(defn destroy []
  (println "mgrapi is shutting down"))
  
(comp/defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app 
  (handler/site (routes home-routes app-routes))
)

(def war-handler 
  (-> app    
    (wrap-resource "public") 
    (wrap-base-url)
    (wrap-file-info)
    (json/wrap-json-response)
    (json/wrap-json-body)
  )
)
