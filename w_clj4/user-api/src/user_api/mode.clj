(ns user-api.mode
  (:use user-api.config)
)
  ;
  ; todo use mode to get configuration map as needed.
  ;
  (def ^:dynamic *mode* (ref "prod"))
  (def ^:dynamic *config-data* (ref {}))
  (def ^:dynamic *db* (ref {}))

  (def ^:dynamic *request-id* nil)
  (def ^:dynamic *back-end* nil)
  (def ^:dynamic *front-end* nil)

  (defn get-app-id
     "get the id based on mode"
     []
      (:appId @*config-data*)
    )

  (defn get-db-credential-spec
      "returns a dbspec"
      []
      (:appId @*db*)
    )

  (defn get-jetty-options
      "return a map of web server options based on *mode*"
      []
      (@*config-data* :jetty-options)
    )