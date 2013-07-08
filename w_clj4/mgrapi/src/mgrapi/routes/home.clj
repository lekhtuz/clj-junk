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

; This method is necessary to circumvent bad handling of java.sql.Date
(defmethod from-java java.util.Date [instance]
  { :time (.getTime instance) }
)

; This method is necessary to circumvent bad handling of java.sql.Date
(defmethod to-java [java.util.Date clojure.lang.APersistentMap] [clazz props]
  (.setTime clazz (props :time))
)

(defn- doctor-subscription-info [sub]
  (log/info "doctor-subscription-info called. sub =" sub)
  (doto sub
;    (.setStartDate nil)
;    (.setExpirationDate nil)
;    (.setRenewalDate nil)
;    (.setTerminatedDate nil)
;    (.setCreditCard nil)
;    (.setCurrentLicense nil)
;    (.setCurrentCharge nil)
;    (.setMyAccountLicenseFormatter nil)
    (.setParent nil)
;    (.setChildSubscriptions nil)
;    (.setGiftCertificate nil)
;    (.setAmountPaid nil)
;    (.setRenewalPrice nil)
  )
  (log/info "doctor-subscription-info ended. ----------------------------------------------------------")
)

(defn- doctor-user-account [obj]
  (log/info "doctor-user-account called. obj =" obj)
  (.setLicenseActiveProductSubscriptionMap obj nil)
  (doseq
    [
      sub (flatten 
            (concat
              (.getSubscriptions obj) 
              (reduce into [] (vals (.getActiveProductSubscriptionMap obj)))
;              (reduce into [] (vals (.getLicenseActiveProductSubscriptionMap obj)))
            )
          )
    ]
    (doctor-subscription-info sub)
  )
  (log/info "doctor-user-account ended. ---------------------------------------------------------------")
)

(defn- doctor-bean [obj]
  (log/info "doctor-bean called. obj =" obj)

  (if (instance? UserAccount obj)
    (doctor-user-account obj)
  )
  (log/info "doctor-bean ended. -----------------------------------------------------------------------")
)

(defn- deep-bean [bean]
  (log/info "deep-bean called. bean =" bean)
  (doctor-bean bean)
  (data/from-java bean)
)

(defn- create-error-response [status-code message]
  (-> (response/response {:error message})
      (response/status status-code)
  )
)

(defn- account-retrieve-id-internal "input: numeric id, output: UserAccount object as a map" [id]
  (log/info "account-retrieve-id-internal started. id =" id)
	(deep-bean (.getUserAccount spring/account-manager (Integer. id)))
)

(defn- account-retrieve-id "input: numeric id, output: http response" [id]
  (log/info "account-retrieve-id started. id = " id)
  (try
	  (let
	    [
	     user-account (account-retrieve-id-internal id)
	    ]
      (log/info "account-retrieve-id: account-retrieve-id-internal returned. user-account = " user-account)
      (log/info "account-retrieve-id: account-retrieve-id-internal returned. subscriptions = " (user-account :subscriptions))
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

(defn- account-retrieve-userid-by-login "input: login, output: user-id" [login]
  (log/info "account-retrieve-userid-by-login started. login = " login)
	(let
	  [
     sc (SearchCriterion. "userName" (SearchCriterion/EQUAL) login)
     search (doto (Search. "UserSearch" (into-array SearchCriterion [ sc ])) (.setPageSize 1))
     user-accounts (.queryForUser spring/search-manager search)
     user-account (first user-accounts)
    ]
    (log/info "account-retrieve-userid-by-login: user-account = " user-account)
    (if (= 1 (count user-accounts))
      (int (.get user-account "USER_ID"))
    )
  )
)

(defn- account-retrieve-login "input: login, output: http response" [login]
  (log/info "account-retrieve-login started. login = " login)
	(let
	  [
      user-id (account-retrieve-userid-by-login login)
    ]
    (if (nil? user-id)
      (create-error-response (HttpServletResponse/SC_NOT_FOUND) (str "Invalid user name - " login))
      (account-retrieve-id user-id)
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
      ; This function is not finished, because I switched to duplicate account service. Code below will throw NPE, because recovery-info is null.
      (if
        (not (nil? user-name))
        (.setUserId recovery-info (account-retrieve-userid-by-login user-name))
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
