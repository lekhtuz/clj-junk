(ns mgrapi.core
  (:use clojure.pprint)
  (:use mgrapi.spring)
)

(&init [ "applicationContext.xml" ])

(defn home [request]
  {
    :status 200
    :headers {"Content-Type" "text/html"}
    :body "Hello"
  }
)
