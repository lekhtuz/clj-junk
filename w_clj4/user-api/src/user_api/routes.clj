(ns user-api.routes
  (:use user-api.frontend-select)
  (:use user-api.frontend-protocol)
  (:use user-api.validation)
  (:use user-api.mode)
  (:use user-api.backend-select)
  (:use user-api.usertransmock)
  (:use user-api.misc)
  (:use clojure.tools.logging
            clj-logging-config.log4j)
  (:require [compojure.core           :as comp])
  (:require [compojure.route          :as route])
  )

(set-logger! :pattern "%d{dd MMM yyyy HH:mm:ss,SSS} %p [%t] - %m%n")

;
; setting up routes here. needs dependency on *front-end* *back-end*
;

;
; [ object, ]
;
(defn arrayify-data-in-response [ response ]
  (if (and (not (nil? (first response))) (not (vector? (first response))))
     [ [ (first response) ], (second response)]
     ; else
     response
   )
  )

(defn handle-get-user-array [email login]
  (let [backend (get-user-backend :mock)]
    (cond
      (and (not (nil? email)) (not (nil? login)))
          (arrayify-data-in-response (findUserByLoginEmail backend login email))
      (and (nil? email) (not (nil? login)))
          (arrayify-data-in-response (findUserByLogin backend login))
      (and (not (nil? email)) (nil? login))
          (findUsersByEmail backend email)
      :else
          [nil, false]
    )
  )
)

(defn handle-request-recovery-record [email userId]
  (let [backend    (get-user-backend :mock)
        ]
    (cond
        (and (not (nil? email)) (not (nil? userId)))
          [nil, false, :bad-request]
        (not (nil? email))
           (let [ response (requestUsernameRecoveryRecord backend email) ]
             (if (second response)
               (conj response :ok)
               ; else
               (conj response :server-error)
               )
           )
        (and (not (nil? userId)))
          (try
            (let [ response (requestPasswordRecoveryRecord backend {:user-id (Integer/valueOf userId)}) ]
              (if (second response)
                 (conj response :ok)
              ; else
                (conj response :server-error)
              )
            )
            (catch NumberFormatException nfe
                [nil false :bad-request]
              )
          )
        :else
          [nil false :bad-request]
    )
  )
)

(defn build-user-changes [request]
  (let [params (get request :params)
        changes (param-selection-vector-to-keyword params ["password" "login" "email"])
        ]
      changes
    )
  )

(comp/defroutes user-api-routing
  ;
  ; test url for connectivity
  ;
  (comp/GET "/ws/2013-04-17/users/test" request
      {
        :status 200
        :headers {"Content-type" "text/html"}
        :body (str "you requested:" request)
      }
  )
  ;
  ; get the service status
  ;

;; vendorAppId from request must be valid and requestId from request must not be nil
        ;;(if (not(valid-request? req))
        ; ; (badRequest *front-end* req)
          ;; else
        ;;  (renderResponse *front-end* req :user-array)
        ;;)

  (comp/GET "/ws/2013-04-17/users" request
    (let [params (get request :params)
          requestId (get params "requestId")
          email (get params "email")
          login (get params "login")
          response (handle-get-user-array email login)
          ]
      (if (second response)
        (renderResponse (select-user-api-front-end-using-accept request) request response :user-array :ok)
        ;else
        (badRequest (select-user-api-front-end-using-accept request) request response)
      )
    )
  )

  ;
  ; check global values
  ;
; (badRequest *front-end* req)
  (comp/GET "/ws/2013-04-17/users/info" request
    (if (valid-request? request)
     ; then
      {
          :status 200
          :headers {"Content-type" "text/html"}
          :body (str "backend: " *back-end* "; front-end: " *front-end* "; mode=" @*mode* "; request-id=" *request-id*)
      }
      ; else
      {
          :status 400
          :headers {"Content-type" "text/html"}
          :body (str "bad request.")
      }
    )
  )

  ;
  ; POST will create the user
  ; TODO return 201 + add header to indicate location, return the contents of the user
  ;
  (comp/POST "/ws/2013-04-17/users"
      request
        (let [params (get request :params)
              requestId (get params "requestId")
              x (info "params = " params)
              response (createUser (get-user-backend :mock) (get params "login") (get params "password") (get params "email")) ]
              (if (second response)
                ; render response if createUser returns [user, true]
                (renderResponse (select-user-api-front-end-using-accept request) request response :user :ok-created)
                ; otherwise render error
                (badRequest (select-user-api-front-end-using-accept request) request response)
              )
        )
  )

  ; get the user
  (comp/GET "/ws/2013-04-17/users/:userId"
      request
      (do
        (info "userId = " (get (get request :params) :userId))
        (try
          (let [ params  (get request :params)
             response    (findUserById (get-user-backend :mock) (Integer/valueOf (get params :userId))) ]
            (if (second response)
              (renderResponse (select-user-api-front-end-using-accept request) request response :user :ok)
              (notFound (select-user-api-front-end-using-accept request) request response)
            )
          )
          (catch NumberFormatException nfe
            (badRequest (select-user-api-front-end-using-accept request) request [nil, false])
            )
        )
      )
  )


  ;
  ; only allow changes to password and email only.
  ;
  (comp/PUT "/ws/2013-04-17/users/:userId"
    request
    (try
      (let [params    (get request :params)
          requestId (get params "requestId")
          userId    (Integer/valueOf (get params :userId))
          changes   (param-selection-vector-to-keyword params ["password" "email"])
          response  (cond (empty? changes) [nil false] :else (updateUser (get-backend-map :mock) userId changes))
          ]
        (if (second response)
          (renderResponse (select-user-api-front-end-using-accept request) request response :user :ok)
        ;else
          (methodNotAllowed (select-user-api-front-end-using-accept request) request response)
        )
      )
      (catch NumberFormatException nfe
        (badRequest (select-user-api-front-end-using-accept request) request [nil, false])
        )
    )
  )

  (comp/GET "/ws/2013-04-17/ri/:token"
    request
    (let [params (get request :params)
          requestId (get params "requestId")
          response (findRecoveryRecord (get-user-backend :mock) (get params :token))
          ]
      (if (second response)
        (renderResponse (select-user-api-front-end-using-accept request) request response :recovery-record :ok)
        ; else
        (notFound (select-user-api-front-end-using-accept request) request response)
      )
    )
  )



  (comp/POST "/ws/2013-04-17/ri"
      request
      (let [params (get request :params)
            requestId (get params "requestId")
            email  (get params "email")
            userId (get params "userId")
            response (handle-request-recovery-record email userId)
            ]
            (cond
              (= (response 2) :ok)
                (renderResponse (select-user-api-front-end-using-accept request) request response :recovery-record :ok-created)
              (= (response 2) :server-error)
                (serverError (select-user-api-front-end-using-accept request) request response)
              (= (response 2) :bad-request)
                (badRequest (select-user-api-front-end-using-accept request) request response)
            )
        )
  )

  (comp/PUT "/ws/2013-04-17/ri/:token"
      request
      (let [params (get request :params)
            requestId (get params "requestId")
            token    (get params :token)
            action   (get params "action")
            response (updateRecoveryRecord (get-user-backend :mock) token action)
            ]
        (if (second response)
          (renderResponse (select-user-api-front-end-using-accept request) request response :recovery-record :ok)
          ; else
          (methodNotAllowed (select-user-api-front-end-using-accept request) request response)
        )
      )
  )

  (comp/ANY "/*"
    request
      (notFound (select-user-api-front-end-using-accept request) request {})
  )
)
