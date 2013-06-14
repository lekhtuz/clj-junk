(ns mgrapi.routes.home
  (:use
    [mgrapi.spring]
    [clojure.java.data]
    [compojure.core]
    [compojure.route]
    [mgrapi.views.layout :as layout]
  )

  (:import
    [com.emeta.cu.business.domain SubscriptionInfo UserAccount]
    [com.emeta.api.search SearchCriterion]
    [com.emeta.api.objects Search]
  )
)

(defn home [] 
  (layout/common [:h1 "home dir!!!!!!!"])
)

;(defmethod from-java com.emeta.cu.business.domain.SubscriptionInfo [instance]
  ; your custom logic for turing this instance into a clojure data structure
;  { :productContext "CRO", :apsReportLocation "reportlocation" }
;)
;(prefer-method from-java com.emeta.cu.business.domain.SubscriptionInfo Object)

(defn deep-bean [bean]
  (if (instance? UserAccount bean) (.setSubscriptions bean nil))  ; remove this line after array conversion is sorted out
  (from-java bean)
)

(defn account-retrieve-id [id request]
  (try
	  (let
	    [
	     user-account (deep-bean (.getUserAccount account-manager (Integer. id)))
	    ]
	    {:body user-account }
    )
    (catch NullPointerException e {:body {:error (str "Account " id " does not exist")}})
    (catch NumberFormatException e {:body {:error (str "Invalid account id - " id)}})
   )
)

(defn account-retrieve-login [login request]
  (try
	  (let
	    [
       sc (SearchCriterion. "userName" (. SearchCriterion EQUAL) login)
       search (Search. "UserSearch" (into-array SearchCriterion [ sc ]))
	    ]
      (.setPageSize search 1)
      (let
        [
         user-accounts (.queryForUser search-manager search)
         user-account (first user-accounts)
        ]
        (if (= 1 (count user-accounts))
          (account-retrieve-id (int (get (first user-accounts) "USER_ID")) request)
;          {:body {:user-account user-account :keys (keys user-account) :key (first(keys user-account)) :f (:USER_ID user-account)} }
          {:body {:error "Invalid user name"} }
        )
      )
    )
    (catch NullPointerException e (layout/common [:h2 (str "Account " login " does not exist.")]))
  )
)

(defroutes home-routes
  (GET "/" [] (home))
  (GET "/account/id/:id" [id request] (account-retrieve-id id request))
  (GET "/account/login/:login" [login request] (account-retrieve-login login request))
  (ANY "/*" request
      (not-found (layout/common [:h1 "Page not found. AAAAAAAAA."]))
  )
)
