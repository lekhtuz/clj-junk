(ns user-api.core
  (:gen-class)
  (:use user-api.mode
        user-api.frontend-select
        user-api.backend-select
        user-api.routes
        user-api.config
  )
  (:require [user-api.config          :as config])
  (:require [ring.middleware.params   :as params])
  (:require [ring.adapter.jetty       :as jetty])

  (:import [java.util UUID Date]
           [java.io FileNotFoundException]
           )
)

;
; ring wrapper methods
;
(defn wrap-request-id
  "generate a uuid on every request, make it available to the log4j
mdc, and add it to the request object"
  [handler]
  (fn [req]
    (binding [*request-id* (.toString (UUID/randomUUID))]
      (handler req))))

(
)

(defn wrap-front-end
  "generate a front end response object depending on the accept header
or other hints"
  [handler]
  (fn [req]
    (let [front-end (select-user-api-front-end-using-accept req)]
        (binding [*front-end* front-end]
          (handler req))
    )
  )
)

(defn wrap-back-end
  "set the back-end needed to process the request"
  [handler]
  (fn [req]
    (binding [ *back-end* (get-user-backend req) ]
      (handler req)
    )
  )
)

;
;  here to setup the launching and wrapping of calls
;
; (wrap-request-id (params/wrap-params #'api-version-2012-11-01)
;
(def public-app
  (params/wrap-params (wrap-request-id (wrap-back-end (wrap-front-end #'user-api-routing))))
)

;
; make any initializations here. perhaps select which backend to use
;

(defn -init
  [args]
  (process-cli args)
)

(defn setup-globals
  [config-map]
  "setting global vars"
  (do
    (dosync (alter *mode* str (config-map :mode)))
    (dosync (alter *config-data* conj config-map))
    ;(dosync (alter *db* conj (:dbspec config-map)))
  )
)

(defn launch-jetty
   [config-map which-app]
   (jetty/run-jetty which-app (:jetty-options config-map))
  )

(defn bootstrap-user-api
  [cli-vector]
  "sets up configuration data and starts up the server"
  (let [param-map (first cli-vector)]
    (if (:usage param-map)
      ; print usage
      (println (last cli-vector))
      ; else
      (do
        (try
          (let [config-map (config/setup-config-map cli-vector)]
            (setup-globals config-map)
            (launch-jetty config-map public-app)
          )
          (catch FileNotFoundException fe (println (str "\n\nERROR: unable to setup config map (missing properties file?)\n\n" (last cli-vector))))
          (catch Exception e
            (do
              (.printStackTrace e)
              (println (str "\n\nERROR: unable to start server\n\n" (last cli-vector)))
            )
          )
        )
      )
    )
  )
)

(defn -main
  [& args]

    (let [cli-vector (-init (vec args))]
      (bootstrap-user-api cli-vector)
    )

  ;       (binding [ *db-spec* ]); initialize back end service and launch jetty
  ;(binding [ *back-end* (AolGathrBackendService.) ]
  ;(jetty/run-jetty public-app (get-jetty-options))
  ;)
)

