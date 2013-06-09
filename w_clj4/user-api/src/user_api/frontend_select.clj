(ns user-api.frontend-select
  (:use user-api.frontendtype)
  (:import [user_api.frontendtype UserApiFrontEndXml UserApiFrontEndJson UserApiFrontEndHtml UserApiFrontEndPlain])
  )

(def front-end-map {
      :xml  (UserApiFrontEndXml.)
      :json (UserApiFrontEndJson.)
      :html (UserApiFrontEndHtml.)
      :plain (UserApiFrontEndPlain.)
    }
  )

(defn get-accept-header-from-request
  [request]
    ((:headers request) "accept")
  )

(defn get-media-type-by-accept-header-value
   "selects the keyword to use based on the accept header contained in the request
    returns :json | :xml | :plain | :html
    by default or the absence of accept header, it will return :json"
    [accept]
      (cond
        (nil? accept)
            :json
        (= accept "text/xml")
            :xml
        (= accept "application/json")
            :json
        (= accept "text/html")
            :html
        (= accept "text/plain")
            :plain
        (.contains accept "text/xml")
            :xml
        (.contains accept "application/json")
            :json
        :else
            :json
        )
   )

(defn select-user-api-front-end-using-accept
  [request]
  "given an http request, use the accept header to select which front end object to use."
  ((get-media-type-by-accept-header-value (get-accept-header-from-request request)) front-end-map)
)

