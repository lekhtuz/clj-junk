(ns user-api.frontendtype
  (:use user-api.frontend-protocol)
  (:use user-api.frontend-render)
  (:use user-api.misc)
  )

(deftype UserApiFrontEndJson
  []
  FrontEndResources

  (renderResponse
    [f request response-map data-mapper-keyword status-code-keyword]
    (generate-http-response-map request response-map (nvl status-code-keyword :ok) :json data-mapper-keyword)
    )

  FrontEndErrors

  (notFound
    [f request response-map]
    (generate-http-response-map request response-map :not-found :json :error )
    )

  (serverError
    [f request response-map]
    (generate-http-response-map request response-map :server-error :json :error )
    )

  (badRequest
    [f request response-map]
    (generate-http-response-map request response-map :bad-request :json :error )
    )

  (methodNotAllowed
      [f request response-map]
      (generate-http-response-map request response-map :method-not-allowed :json :error )
      )
  )

(deftype UserApiFrontEndHtml
  []

  FrontEndResources

  (renderResponse
    [f request response-map data-mapper-keyword status-code-keyword]
    (generate-http-response-map request response-map (nvl status-code-keyword :ok) :html data-mapper-keyword)
    )

  FrontEndErrors

  (notFound
    [f request response-map]
    (generate-http-response-map request response-map :not-found :html :error )
    )

  (serverError
    [f request response-map]
    (generate-http-response-map request response-map :server-error :html :error )
    )

  (badRequest
    [f request response-map]
    (generate-http-response-map request response-map :bad-request :html :error )
    )

  (methodNotAllowed
        [f request response-map]
        (generate-http-response-map request response-map :method-not-allowed :html :error )
        )
  )

(deftype UserApiFrontEndPlain
  []

  FrontEndResources

  (renderResponse
    [f request response-map data-mapper-keyword status-code-keyword]
    (generate-http-response-map request response-map (nvl status-code-keyword :ok) :plain data-mapper-keyword)
    )

  FrontEndErrors

  (notFound
    [f request response-map]
    (generate-http-response-map request response-map :not-found :plain :error )
    )

  (serverError
    [f request response-map]
    (generate-http-response-map request response-map :server-error :plain :error )
    )

  (badRequest
    [f request response-map]
    (generate-http-response-map request response-map :bad-request :plain :error )
    )

  (methodNotAllowed
          [f request response-map]
          (generate-http-response-map request response-map :method-not-allowed :plain :error )
          )
  )

(deftype UserApiFrontEndXml
  []
  FrontEndResources

  (renderResponse
    [f request response-map data-mapper-keyword status-code-keyword]
    (generate-http-response-map request response-map (nvl status-code-keyword :ok) :xml data-mapper-keyword)
    )

  FrontEndErrors

  (notFound
    [f request response-map]
    (generate-http-response-map request response-map :not-found :xml :error )
    )

  (serverError
    [f request response-map]
    (generate-http-response-map request response-map :server-error :xml :error )
    )

  (badRequest
    [f request response-map]
    (generate-http-response-map request response-map :bad-request :xml :error )
    )

  (methodNotAllowed
          [f request response-map]
          (generate-http-response-map request response-map :method-not-allowed :xml :error )
    )
  )