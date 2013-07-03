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
  (log/info "home page called")
  (response/redirect "index.html")
)

(defn print-map [map]
  (layout/common (str "<code>" (with-out-str (pprint map)) "</code>"))
)

(defmethod from-java java.sql.Date [instance]
  { :time (.getTime instance) }
)

(defmethod to-java [java.sql.Date clojure.lang.APersistentMap] [clazz props]
  (.setTime clazz (props :time))
)

(defmethod from-java java.util.Date [instance]
  { :time (.getTime instance) }
)

(defmethod to-java [java.util.Date clojure.lang.APersistentMap] [clazz props]
  (.setTime clazz (props :time))
)

;(defmethod from-java com.emeta.cu.business.domain.Money [instance]
;  { :currencyCode (.getCurrencyCode instance) }
;)

(defn- doctor-subscription-info [sub]
  (doto sub
    (.setStartDate nil)
    (.setExpirationDate nil)
    (.setRenewalDate nil)
    (.setTerminatedDate nil)
    (.setParent nil)
    (.setCreditCard nil)
    (.setCurrentLicense nil)
    (.setCurrentCharge nil)
    (.setMyAccountLicenseFormatter nil)
    (.setChildSubscriptions nil)
    (.setGiftCertificate nil)
    (.setAmountPaid nil)
    (.setRenewalPrice nil)
  )
)

(defn- doctor-bean [obj]
  (log/info "doctor-bean called. obj = " obj)
  (if (instance? UserAccount obj)
    (doseq [
            sub (flatten (concat
                  (.getSubscriptions obj) 
                  (vals (.getActiveProductSubscriptionMap obj))
                  (vals (.getLicenseActiveProductSubscriptionMap obj))))
           ] 
      (doctor-subscription-info sub)
    )
  )
;  (log/info "doctor-bean ended. obj = " obj)
)

(defn- deep-bean [bean]
  (log/info "deep-bean called. bean=" bean)
  (doctor-bean bean)
  (data/from-java bean)
)

(defn- create-error-response [status-code message]
  (-> (response/response {:error message})
      (response/status status-code)
  )
)

(defn- account-retrieve-id-internal "input: numeric id, output: UserAccount object as a map" [id]
  (log/info "account-retrieve-id-internal started. id = " id)
	(deep-bean (.getUserAccount spring/account-manager (Integer. id)))
)

(defn- account-retrieve-id "input: numeric id, output: http response" [id]
  (log/info "account-retrieve-id started. id = " id)
  (try
	  (let
	    [
	     user-account (account-retrieve-id-internal id)
	    ]
      (log/info "account-retrieve-id-internal returned. user-account = " user-account)
      (log/info "account-retrieve-id-internal returned. subscriptions = " (user-account :subscriptions))
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

(defn- account-retrieve-login-internal "input: login, output: UserAccount object as a map" [login]
  (log/info "account-retrieve-login-internal started. login = " login)
	(let
	  [
     sc (SearchCriterion. "userName" (SearchCriterion/EQUAL) login)
     search (doto (Search. "UserSearch" (into-array SearchCriterion [ sc ])) (.setPageSize 1))
     user-accounts (.queryForUser spring/search-manager search)
     user-account (first user-accounts)
    ]
    (log/info "account-retrieve-login-internal: user-account = " user-account)
    (if (= 1 (count user-accounts))
      (account-retrieve-id (int (.get user-account "USER_ID")))
    )
  )
)

(defn- account-retrieve-login "input: login, output: http response" [login]
  (log/info "account-retrieve-login started. login = " login)
	(let
	  [
      user-account (account-retrieve-login-internal login)
    ]
    (if (nil? user-account)
      (account-retrieve-id (int (.get user-account "USER_ID")))
      (create-error-response (HttpServletResponse/SC_NOT_FOUND) (str "Invalid user name - " login))
    )
  )
)

(defn- recovery-info-create [body]
  (log/info "recovery-info-create started. body = " body)
  (try
	  (let
	    [
       email (body :email)
       user-name (body :userName)
       recovery-info (doto (RecoveryInfo.)
                       (.setEmail email)
                       (.setUserName user-name)
                     )
	    ]
	    (log/info "recovery-info-create: email = " email "userName = " user-name)
      (if-let 
        [
          user-account (account-retrieve-login-internal user-name)
        ]
        (not (nil? user-name))
        (.setUserId recovery-info 1)
      )
     
     (if (nil? recovery-info)
        (create-error-response (HttpServletResponse/SC_BAD_REQUEST) "Unable to save a RecoveryInfo object. Possible reason is invalid userId.")
        (response/response (deep-bean recovery-info))
      )
	  )
    (catch IllegalArgumentException e
      (create-error-response (HttpServletResponse/SC_BAD_REQUEST) "Unable to construct a RecoveryInfo object using provided json data. Possible reasons are property name and/or type mismatch.")
    )
  )
)

(defn- recovery-info-create [body]
  (log/info "recovery-info-create started. body = " body)
  (try
	  (let
	    [
	     recovery-info (data/to-java RecoveryInfo body)
	     recovery-info (.createRecoveryRecord spring/account-manager recovery-info)
	    ]
	    (log/info "recovery-info-create: recovery-info =" recovery-info)
      (if (nil? recovery-info)
        (create-error-response (HttpServletResponse/SC_BAD_REQUEST) "Unable to save a RecoveryInfo object. Possible reason is invalid userId.")
        (response/response (deep-bean recovery-info))
      )
	  )
    (catch IllegalArgumentException e
      (create-error-response (HttpServletResponse/SC_BAD_REQUEST) "Unable to construct a RecoveryInfo object using provided json data. Possible reasons are property name and/or type mismatch.")
    )
  )
)

(comp/defroutes home-routes
  (comp/GET "/" request (home request))
  (comp/GET "/test/:id" request (print-map request))
  (comp/GET "/account/id/:id" request (account-retrieve-id ((request :params) :id)))
  (comp/GET "/account/login/:login" request (account-retrieve-login ((request :params) :login)))
  (comp/POST "/recovery-info" request (recovery-info-create (request :body)))
  (comp/ANY "/*" request
      (create-error-response (HttpServletResponse/SC_NOT_FOUND) "Page not found. 404 returned.")
  )
)
