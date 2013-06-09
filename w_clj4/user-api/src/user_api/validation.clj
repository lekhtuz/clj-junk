(ns user-api.validation
  (:require [clojure.string :as str])
)
; this is to verify that the incoming request has an requestId

(defn valid-request?
   "returns true if the HTTP request has a non nil requestId"
   [ req ]
   (let [params (get req :params)
         requestId (get params "requestId")
         ]
      (not (str/blank? requestId))
   )
)
