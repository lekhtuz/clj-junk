(ns mgrapi.routes.home
  (:use
    [mgrapi.spring :as spring :only (account-manager search-manager)]
    [mgrapi.views.layout :as layout]
    [clojure.java.data]
    [compojure.core :as comp :only (GET ANY)]
    [compojure.route :as route :only (not-found)]
    [ring.util.response :as response :only [response]]
  )

  (:import
    [com.emeta.cu.business.domain SubscriptionInfo UserAccount]
    [com.emeta.api.search SearchCriterion]
    [com.emeta.api.objects Search]
    [javax.servlet.http HttpServletResponse]
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

(defn- deep-bean [bean]
  (if (instance? UserAccount bean) (.setSubscriptions bean nil))  ; remove this line after array conversion is sorted out
  (from-java bean)
)

(defn- account-retrieve-id [id request]
  (try
	  (let
	    [
	     user-account (deep-bean (.getUserAccount spring/account-manager (Integer. id)))
	    ]
	    (response/response user-account)
    )
    (catch NullPointerException e (-> 
                                    (response/response {:error (str "Account " id " does not exist")})
                                    (response/status (. HttpServletResponse SC_NOT_FOUND))
                                  )
    )
    (catch NumberFormatException e (-> 
                                     (response/response {:error (str "Invalid account id - " id)}) 
                                     (response/status (. HttpServletResponse SC_BAD_REQUEST))
                                   )
    )
  )
)

(defn- account-retrieve-login [login request]
  (try
	  (let
	    [
       sc (SearchCriterion. "userName" (. SearchCriterion EQUAL) login)
       search (Search. "UserSearch" (into-array SearchCriterion [ sc ]))
	    ]
      (.setPageSize search 1)
      (let
        [
         user-accounts (.queryForUser spring/search-manager search)
         user-account (first user-accounts)
        ]
        (if (= 1 (count user-accounts))
          (account-retrieve-id (int (get (first user-accounts) "USER_ID")) request)
;          (response/response {:user-account user-account :keys (keys user-account) :key (first(keys user-account)) :f (:USER_ID user-account)} )
          (response/response {:error "Invalid user name"})
        )
      )
    )
    (catch NullPointerException e (layout/common [:h2 (str "Account " login " does not exist.")]))
  )
)

(comp/defroutes home-routes
  (comp/GET "/" [] (home))
  (comp/GET "/account/id/:id" [id request] (account-retrieve-id id request))
  (comp/GET "/account/login/:login" [login request] (account-retrieve-login login request))
  (comp/ANY "/*" request
      (route/not-found (layout/common [:h1 "Page not found. 404 returned."]))
  )
)
