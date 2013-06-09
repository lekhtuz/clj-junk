(ns immutant-demo.core
  (:use clojure.pprint)
 )

(defn ring-handler [request]
  {:status 200
    :headers {"Content-Type" "text/html"}
    :body (pprint (get (. System getProperties) "java.class.path"))
  }
)
  
(defn another-ring-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Pssst! Over here!"})
