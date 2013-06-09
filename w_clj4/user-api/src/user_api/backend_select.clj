(ns user-api.backend-select
  (:use user-api.usertransmock)
  (:import [user_api.usertransmock MockUserBackend])
  )

(def get-backend-map
  {
    :mock (MockUserBackend. (make-data-base))
  }
)

(defn get-user-backend [mode-keyword] (mode-keyword get-backend-map) )