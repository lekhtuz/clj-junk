(ns mgrapi.handler
  (:use 
    [clojure.tools.logging :as log]
    [clojure.pprint]
    [compojure.core :as comp]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [hiccup.middleware :refer [wrap-base-url]]
    [mgrapi.spring :as spring]
    [mgrapi.routes.home :as home :only [home-routes create-error-response]]
    [ring.middleware.json :as json]
    [ring.middleware.resource :as resource]
    [ring.middleware.file-info :as file-info]
    [ring.middleware.params :as params]
    [ring.util.response :as response :only [response content-type]]
  )
  (:import
    [javax.servlet.http HttpServletResponse]
  )
)

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
  (log/info "mgrapi is shutting down"))
  
(comp/defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app 
  (handler/site (routes home/home-routes app-routes))
)

(defn- json-response? [response]
  (log/info "json-response?: ----------- started")
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
; 2. "error" if it was present

(defn- process-json-response-body [body]
  (log/info "process-json-response-body: ----------- started. body =" (with-out-str (pprint body)))
  (if (nil? (:error body))
    {:response body}
    {:error (:error body)}
  )
)

; This middleware has to be above wrap-json-response
(defn wrap-pwiec
  [handler]
  (fn [request]
    (log/info "wrap-pwiec: ----------- started")
    (let [request-id ((:query-params request) "requestId")]
	    (if (request-id-valid? request-id)
		    (let 
		      [
		        start-time (System/nanoTime)
		        response (handler request)
		        end-time (System/nanoTime)
		        duration (/ (double (- end-time start-time)) 1000000) ; in milliseconds
	          response-body (:body response)
		      ]
          (log/info "wrap-pwiec: ----------- condition to call process-json-response-body (should be true)" (and (json-response? response) (map? response-body)))
		      (if (and (json-response? response) (map? response-body))
	          (assoc response :body (assoc (process-json-response-body response-body)
                                     :duration duration
                                     :requestId request-id
                                     :status (:status response)
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
