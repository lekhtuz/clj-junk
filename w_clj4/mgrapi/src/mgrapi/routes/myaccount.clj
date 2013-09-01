(ns mgrapi.routes.myaccount
  (:use
    [mgrapi.views.myaccount.login]
;    [clojure.pprint]
    [compojure.core]
  )
)

(defroutes myaccount-routes-internal
  (GET "/login.htm" [] login)
  (POST "/login.htm" request (do-login request))
  (GET "/main.htm" [] home)
)

(defroutes myaccount-routes
  (context "/ec/myaccount" [] myaccount-routes-internal)
)