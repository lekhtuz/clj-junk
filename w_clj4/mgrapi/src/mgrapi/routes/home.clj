(ns mgrapi.routes.home
  (:use
    [mgrapi.spring :as spring :only (account-manager search-manager)]
    [mgrapi.views.layout :as layout]
    [clojure.java.data :as data]
    [clojure.tools.logging :as log]
    [clojure.pprint]
    [compojure.core :as comp :only (GET ANY)]
    [compojure.route :as route :only (not-found)]
    [ring.util.response :as response :only [response]]
  )

  (:import
    [com.emeta.cu.business.domain Money RecoveryInfo SubscriptionInfo UserAccount]
    [com.emeta.api.search SearchCriterion]
    [com.emeta.api.objects Search]
    [javax.servlet.http HttpServletResponse]
  )
)

(defn home [request] 
  (layout/common "<h1>This is the home page</h1>")
)

(defn print-map [map] 
  (layout/common (with-out-str (pprint map)))
)

;(defmethod from-java java.util.Date [instance]
;  { :time (.getTime instance) }
;)

;(defmethod from-java com.emeta.cu.business.domain.Money [instance]
;  { :currencyCode (.getCurrencyCode instance) }
;)

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

(defn- account-retrieve-id [id]
  (try
	  (let
	    [
	     user-account (deep-bean (.getUserAccount spring/account-manager (Integer. id)))
	    ]
      (log/info "deep-bean returned. user-account =" user-account)
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

(defn- account-retrieve-login [login]
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
      (log/info "account-retrieve-login: user-account =" user-account)
      (if (= 1 (count user-accounts))
        (account-retrieve-id (int (.get user-account "USER_ID")))
        (create-error-response (HttpServletResponse/SC_NOT_FOUND) (str "Invalid user name - " login))
      )
    )
  )
)

(defn- recovery-info-create [body]
  (let
    [
     recovery-info (data/to-java RecoveryInfo body)
     recovery-info (.createRecoveryRecord spring/account-manager recovery-info)
    ]
    (log/info "recovery-info-create: recovery-info =" recovery-info)
    (print-map body)
  )
)

(comp/defroutes home-routes
  (comp/GET "/" request (home request))
  (comp/GET "/test/:id" request (print-map request))
  (comp/GET "/account/id/:id" request (account-retrieve-id ((request :params) :id)))
  (comp/POST "/recovery-info" request (recovery-info-create (request :body)))
  (comp/GET "/account/login/:login" request (account-retrieve-login ((request :params) :login)))
  (comp/ANY "/*" request
      (route/not-found (layout/common [:h1 "Page not found. 404 returned."]))
  )
)
