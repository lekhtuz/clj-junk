(ns user-api.frontend-protocol)

(defprotocol FrontEndErrors
  "methods that transform data into error responses"
  (notFound [f request response-map ] "")
  (serverError [f request response-map ] "")
  (badRequest [f request response-map ] "")
  (methodNotAllowed [f request response-map] "")
  )

(defprotocol FrontEndResources
  "methods on the front end to return the same data in diff formats"
  (renderResponse [f request response-map data-mapper-keyword status-code-keyword] "")
  )
