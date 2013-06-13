(ns mgrapi.core
  (:use [compojure.core :only (defroutes GET)]
        [compojure.handler]
        [ring.adapter.jetty :as ring]
        [ring.util.response :as response]
        [clojure.pprint]
        [mgrapi.spring]
  )
  (:require [compojure.route :as route])
)

(&init [ "applicationContext.xml" ])

; Compojure handler for the home page /
(defn homepage [request]
  (pr-str "homepage request=" request)
)

; Compojure handler for account retrieval page /account/:id
(defn account [id]
  (pr-str "id=" id)
)

; compojure routes. asterisk means that this is low level implementation
(defroutes mgrapi-routes*
  (route/resources "/")
  (GET "/" request (homepage request))
  (GET "/account/:id" [id] (account id))
)

; we added middleware that will do parameter processing for us
(def mgrapi-routes (compojure.handler/site mgrapi-routes*))

(defn -main []
  (run-jetty mgrapi-routes* {:port 8080 :join? false})
)
