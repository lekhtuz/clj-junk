(ns mgrapi.routes.home
  (:use
    [mgrapi.spring :as spring :only (get-erights-cce account-manager search-manager user-search-manager)]
    [mgrapi.views.layout :as layout]
    [clojure.java.data :as data]
    [clojure.tools.logging :as log]
    [clojure.pprint]
    [compojure.core :as comp :only (GET ANY context)]
    [compojure.route :as route :only (not-found)]
    [ring.util.response :as response :only [response]]
  )

  (:import
    [com.emeta.erweb.components UncheckedException]
    [com.emeta.api.search SearchCriterion]
    [com.emeta.api.objects Search]
    [com.emeta.cu.business.domain Money RecoveryInfo SubscriptionInfo UserAccount UserSearchRequest]
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
  (.setTime clazz (:time props))
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

(defn create-error-response [status-code message]
  (-> (response/response {:error message})
      (response/status status-code)
  )
)

(defn create-json-response [response-map]
  (-> (response/response response-map)
      (response/content-type "application/json")
  )
)

; There are 3 layers - UncheckedException, which wraps RuntimeException, which wraps TheRealException
; This function is only called with UncheckedException, but I will check for it just in case.
(defn- create-erights-error-response [^UncheckedException e]
  (log/warn "create-erights-error-response: exception thrown. e =" e (newline) (.printStackTrace e))
  (if (and (instance? UncheckedException e) (instance? RuntimeException (.getCause e)))
    (create-erights-error-response (.getCause (.getCause e)))
    ; Consider changing status code below to something more appropriate, as this is not a user error condition.
    (create-error-response (HttpServletResponse/SC_BAD_REQUEST) (str "Exception: " e))
  )
)

(defn- account-retrieve-id-internal "input: numeric id, output: UserAccount object as a map" [id]
  (log/info "account-retrieve-id-internal started. id =" id)
	(deep-bean (.getUserAccount spring/account-manager (Integer. id)))
)

(defn- account-retrieve-id "input: numeric id, output: http response" [id]
  (log/info "account-retrieve-id started. id =" id)
  (try
	  (let
	    [
	     user-account (account-retrieve-id-internal id)
	    ]
      (log/info "account-retrieve-id: account-retrieve-id-internal returned. user-account = " user-account)
      (if (nil? user-account)
        (create-error-response (HttpServletResponse/SC_NOT_FOUND) (str "Account " id " does not exist"))
        (create-json-response user-account)
      )
    )
    (catch NumberFormatException e
      (log/info "NumberFormatException: account id is not a valid number")
      (create-error-response (HttpServletResponse/SC_BAD_REQUEST) (str "Invalid account id - " id))
    )
    (catch UncheckedException e (create-erights-error-response e))
  )
)

(defn- account-retrieve-userid-by-login "input: login, output: user-id" [^String login]
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

(defn- account-retrieve-login "input: login, output: http response" [^String login]
  (log/info "account-retrieve-login started. login = " login)
  (try
		(let
		  [
	      user-id (account-retrieve-userid-by-login login)
	    ]
	    (if (nil? user-id)
	      (create-error-response (HttpServletResponse/SC_NOT_FOUND) (str "Invalid user name - " login))
	      (account-retrieve-id user-id)
	    )
	  )
    (catch UncheckedException e (create-erights-error-response e))
  )
)

; This handler is not finished. do not call!!!
(defn- recovery-info-create [body]
  (log/info "recovery-info-create started. body = " body)
  (try
	  (let
	    [
       email (:email body)
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
        (create-json-response (deep-bean recovery-info))
      )
	  )
    (catch IllegalArgumentException e
      (create-error-response (HttpServletResponse/SC_BAD_REQUEST) "Unable to construct a RecoveryInfo object using provided json data. Possible reasons are property name and/or type mismatch.")
    )
  )
)

(def bad-emails
  '("nobody@nowhere.com", "t-martje@consumer.org")
)

; if email is in bad-emails list, return nil, otherwise return email
(defn- check-email-against-blacklist [^String email]
  (if (not (some #{email} bad-emails)) email)
)

(defn- check-user-exists [username]
  (log/info "check-user-exists started. username =" username)
  (try
    (create-json-response { :userNameExists (.validateUserInfo user-search-manager username) })
    (catch UncheckedException e (create-erights-error-response e))
  )
)

(defn- check-duplicate-subscription [first-name last-name username email product-context]
  (log/info "check-duplicate-subscription started. first-name =" first-name ", last_name =" last-name ", username =" username ", email =" email ", product-context =" product-context)
  (try
	  (let
	    [
	     userSearchRequest (doto (UserSearchRequest.)
	                         (.setFirstName first-name)
	                         (.setLastName last-name)
	                         (.setEmail (check-email-against-blacklist email))
	                         (.setProductContext product-context)
	                       )
	     return-object (into {} (.validateUserInfo user-search-manager userSearchRequest))
	    ]
	    (log/info "check-duplicate-subscription: return-object =" return-object)
	    (if (empty? return-object)
        ; Consider changing status code below to something more appropriate, as this is not a user error condition.
	      (create-error-response (HttpServletResponse/SC_BAD_REQUEST) "Unable to check for a duplicate subscription. Command did not return a valid response.")
	      (create-json-response {
	                              :username-exists (< 0 (count (return-object "accountExistsList"))),
	                              :active-subscriptions-exists (< 0 (count (return-object "accountSubscriptionList"))),
	                              :search-user-active (< 0 (count (return-object "searchUserActive")))
	                            }
	      )
	    )
	  )
    (catch UncheckedException e (create-erights-error-response e))
  )
)

(comp/defroutes account-routes
  (comp/GET ["/id/:id", :id "[0-9]+"] [id] (account-retrieve-id id))
  (comp/GET "/login/:login" [login] (account-retrieve-login login))
)

(comp/defroutes home-routes
  (comp/GET "/" request (home request))
  (comp/context "/account" [] account-routes)
  (comp/GET "/test/:id" request (print-map request))
  (comp/GET "/user/exists" [userName] (check-user-exists userName))
  (comp/GET "/checkduplicateaccount" [firstName lastName username email productContext] (check-duplicate-subscription firstName lastName username email productContext))
  (comp/POST "/recovery-info" request (recovery-info-create ( :body request))); this handler is not finished. do not call!!!
  (comp/ANY "/*" request
      (create-error-response (HttpServletResponse/SC_NOT_FOUND) "Page not found. 404 returned.")
  )
)
