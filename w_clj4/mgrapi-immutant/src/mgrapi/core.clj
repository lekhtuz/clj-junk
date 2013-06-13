(ns mgrapi.core
  (:use clojure.pprint)
  (:use mgrapi.spring)
)

(&init [ "applicationContext.xml" ])

; Ring handler for the home page /
(defn home [request]
  {
    :status 200
    :headers {"Content-Type" "text/html"}
    :body (pr-str "home request=" request)
  }
)

; Ring handler for account retrieval page /account/:id
(defn account [request]
  (let [
        params (get request :params)
        id (get params :id)
       ]
    
  {
    :status 200
    :headers {"Content-Type" "text/html"}
    :body (pr-str "params=" params " id=" id " account request=" request)
  }
)
)
