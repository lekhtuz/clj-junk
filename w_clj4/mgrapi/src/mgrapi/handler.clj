(ns mgrapi.handler
  (:use 
    [clojure.tools.logging :as log]
    [clojure.pprint]
    [compojure.core :as comp]
    [compojure.handler :as handler]
    [hiccup.middleware :refer [wrap-base-url]]
    [mgrapi.spring :as spring]
    [mgrapi.routes.home :as home]
    [mgrapi.routes.myaccount :as myaccount]
    [ring.middleware.json :as json]
    [ring.middleware.resource :as resource]
    [ring.middleware.file-info :as file-info]
    [ring.middleware.params :as params]
    [ring.util.response :as response]
  )
  (:import
    [javax.servlet.http HttpServletResponse]
  )
)

; 
(def context-files
  [
   "applicationContext.xml"
   "context-erights.xml"
   "context-transformers.xml"
  ]
)

(defn init []
  (log/info "mgrapi is starting")
  (spring/&init context-files)
)

(defn destroy []
  (log/info "mgrapi is shutting down")
)
  
(comp/defroutes app-routes
  (comp/ANY "/*" request (create-error-response (HttpServletResponse/SC_NOT_FOUND) "Page not found. 404 returned."))
)

(def app 
  (handler/site (routes myaccount/myaccount-routes home/home-routes app-routes))
)

(defn- json-response? [response]
  (log/info "json-response?: ----------- started. response =" response)
  (if-let [type ((:headers response) "Content-Type")]
    (not (empty? (re-find #"^application/(vnd.+)?json" type)))
  )
)

(defn- request-id-valid? [^String request-id]
  (not (nil? request-id))
)

; Process the body of the response. It is guaranteed to be a map with either correct response, or "error"
; This function returns the new body consisting with one the following at the top level:
; 1. "response" with the original body as the value if "error" is not present
; 2. original body if "error" was present
(defn- process-json-response-body [body]
  (log/info "process-json-response-body: ----------- started. body =" (with-out-str (pprint body)))
  (if (:error body) body { :response body }
  )
)

; These patterns must match route definitions
(def protected-routes
  '( #"/account/id/[\\d]+", #"/account/login/[^/\\s]+", #"/user/exists", #"/user/duplicatesub", #"/user/validateuserinfo" )
)

; Check against the list of routes which require request id. Since part of the route may be a variable,
; it does not have to be exact match.
; /account/id/12345 will match /account/id
(defn- protected-route? [uri]
  (log/info "protected-route?: ----------- checking uri " uri)
  (some #(re-matches % uri) protected-routes)
)

; This middleware has to be above wrap-json-response
(defn wrap-pwiec
  [handler]
  (fn [request]
    (log/info "wrap-pwiec: ----------- started")
    (let [request-id ((:query-params request) "requestId")]
	    (if (or (not (protected-route? (:uri request))) (request-id-valid? request-id))
		    (let 
		      [
		        start-time (System/nanoTime)
		        response (handler request)
		        end-time (System/nanoTime)
		        duration (/ (double (- end-time start-time)) 1000000) ; in milliseconds
	          response-body (:body response)
		      ]
          (log/info "wrap-pwiec: ----------- (json-response? response)" (json-response? response))
          (log/info "wrap-pwiec: ----------- (map? response-body)" (map? response-body))
          (log/info "wrap-pwiec: ----------- condition to call process-json-response-body (should be true)" (and (json-response? response) (map? response-body)))
		      (if (and (json-response? response) (map? response-body))
	          (assoc response :body (conj 
                                    (assoc (process-json-response-body response-body)
                                      :duration duration
                                      :status (:status response)
                                    )
                                    (if (nil? request-id) { } { :requestId request-id })
                                  )
            )
		        response
		      )
		    )
	      (home/create-error-response (HttpServletResponse/SC_BAD_REQUEST) "Request Id is missing or not valid")
	    )
    )
  )
)

(defn wrap-log-request-response
  [handler description]
  (fn [request]
    (log/info "wrap-log-request-response: --" description "-- request =" (with-out-str (pprint request)))
    (let 
      [
        start-time (System/nanoTime)
        response (handler request)
        end-time (System/nanoTime)
        duration (/ (double (- end-time start-time)) 1000000) ; in milliseconds
      ]
      (log/info "wrap-log-request-response: --" description "-- duration=" duration "ms, response =" (with-out-str (pprint response)))
      response
    )
  )
)

(def war-handler 
  (-> app
;    (wrap-log-request-response "before wrap-params")
    (wrap-pwiec) ; This middleware has to be above wrap-json-response
    (wrap-log-request-response "between wrap-params and wrap-pwiec")
    (params/wrap-params)
;    (wrap-log-request-response "after wrap-pwiec")
    (wrap-base-url)
    (file-info/wrap-file-info)
    (json/wrap-json-response)
    (json/wrap-json-body)
    (wrap-resource "public") ; return static resource, do not call down the chain if it exists.
  )
)
