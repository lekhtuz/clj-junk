(ns mgrapi.routes.home
  (:use
    [mgrapi.spring]
    [clojure.java.data]
    [compojure.core]
    [compojure.route]
    [mgrapi.views.layout :as layout]
  )

  (:import [com.emeta.cu.business.domain SubscriptionInfo UserAccount])
)

(defn home [] 
  (layout/common [:h1 "home dir!!!!!!!"])
)

;(defmethod from-java com.emeta.cu.business.domain.SubscriptionInfo [instance]
  ; your custom logic for turing this instance into a clojure data structure
;  { :productContext "CRO", :apsReportLocation "reportlocation" }
;)
;(prefer-method from-java com.emeta.cu.business.domain.SubscriptionInfo Object)

(defn deep-bean [ua]
  (let [ua-map (from-java ua)]
    ua-map
  )
)

(defn account-retrieve-id [id]
  (try
	  (let
	    [
	     user-account (deep-bean (.getUserAccount account-manager (Integer. id)))
	    ]
;      (println user-account)
	    {:body user-account }
    )
    (catch NullPointerException e {:body {:error "Account does not exist"}})
   )
)

(defn account-retrieve-login [login]
  (try
	  (let
	    [
	     user-account (bean (.getUserAccount account-manager (Integer. login)))
	    ]
	    (layout/common [:h2 (str "login=" login "<br>" user-account)])
	  )
    (catch NullPointerException e (layout/common [:h2 (str "Account " login " does not exist.")]))
   )
)

(defroutes home-routes
  (GET "/" [] (home))
  (GET "/account/id/:id" [id] (account-retrieve-id id))
  (GET "/account/login/:login" [login] (account-retrieve-login login))
  (ANY "/*" request
      (not-found (layout/common [:h1 "Page not found. AAAAAAAAA."]))
  )
)
