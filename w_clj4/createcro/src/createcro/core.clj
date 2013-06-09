(ns createcro.core
  (:use clojure.pprint)
  (:import [com.emeta.cu.business.manager.impl AccountManagerImpl])
  (:import [com.emeta.cu.business.domain UserAccount])
  (:import [java.util Date])
)

(def accountManager (AccountManagerImpl.))

(def ua (UserAccount.))
(.setCdsId ua 5)
(.setUserName ua "aaa")
(.setLastLogin ua (Date.))

(defn -main
  []
  (println "Starting...")
  (pprint ua)
  (pprint accountManager)
)