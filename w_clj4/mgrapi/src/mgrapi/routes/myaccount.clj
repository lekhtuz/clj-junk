(ns mgrapi.routes.myaccount
  (:use
    [mgrapi.views.myaccount.login :as login]
    [clojure.tools.logging :as log]
    [clojure.pprint]
    [compojure.core :as comp]
    [ring.util.response :as response]
  )

  (:import
    [com.emeta.erweb.components UncheckedException]
    [com.emeta.api.search SearchCriterion]
    [com.emeta.api.objects Search]
    [com.emeta.cu.business.domain RecoveryInfo SubscriptionInfo UserAccount UserSearchRequest]
    [javax.servlet.http HttpServletResponse]
  )
)

(comp/defroutes myaccount-routes-internal
  (comp/GET "/login.htm" [] login)
)

(comp/defroutes myaccount-routes
  (comp/context "/ec/myaccount" [] myaccount-routes-internal)
)