(ns mgrapi.routes.home
  (:use
    [mgrapi.spring :as spring :only (account-manager search-manager)]
    [mgrapi.views.layout :as layout]
    [clojure.java.data :as data]
    [clojure.tools.logging :as log]
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

(defn- deep-bean [bean]
  (log/info "deep-bean called. bean=" bean)
  (if (instance? UserAccount bean) (.setSubscriptions bean nil))  ; remove this line after array conversion is sorted out
  (data/from-java bean)
)

(defn- create-error-response [status-code message]
  (-> (response/response {:error message})
      (response/status status-code)
  )
)

(defn- account-retrieve-id [id request]
  (try
	  (let
	    [
	     user-account (deep-bean (.getUserAccount spring/account-manager (Integer. id)))
	    ]
      (log/info "deep-bean returned. user-account=" user-account)
      (if (nil? user-account)
        (create-error-response (HttpServletResponse/SC_NOT_FOUND) (str "Account " id " does not exist"))
        (response/response user-account)
      )
    )
    (catch NumberFormatException e
      (log/info "NumberFormatException: account id is not a valid number")
      (create-error-response (HttpServletResponse/SC_BAD_REQUEST) (str "Invalid account id - " id))
    )
  )
)

(defn- account-retrieve-login [login request]
  (try
	  (let
	    [
       sc (SearchCriterion. "userName" (SearchCriterion/EQUAL) login)
       search (Search. "UserSearch" (into-array SearchCriterion [ sc ]))
	    ]
      (.setPageSize search 1)
      (let
        [
         user-accounts (.queryForUser spring/search-manager search)
         user-account (first user-accounts)
        ]
        (if (= 1 (count user-accounts))
          (account-retrieve-id (int ((first user-accounts) :USER_ID)) request)
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
