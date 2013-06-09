(ns user-api.frontend-render
  (:require [cheshire.core :as cheshire]
              [clojure.data.xml :as xml]
              )
  (:use user-api.mode)
  (:use user-api.misc)
  )

(defn generate-service-status-result-map
  "create a service-map for json consumption"
  [requestId serviceStatus f req params]
  {
    "result" {
               "requestId" requestId
               "subStatus" serviceStatus
               }
    }
  )

(defn generate-error-result-map
  "create an error-map for json consumption"
  [requestId code errorMessageText f req params]
  {
    "result" {
               "requestId" requestId
               "message" {
                           "code" code
                           "text" errorMessageText
                           }
               }
    }
  )

;
; service status
;

; todo make this more succinct
(defn generate-service-status-hiccup-vector-format-for-xml
  "create a result map for xml"
  [params]
  (let [requestId (params :requestId)
        serviceStatus (params :serviceStatus)]
        [:result (list [:requestId {} requestId]
                       [:subStatus {} serviceStatus]
                  )
        ]
  )
)

;
; error xml template for consumption
;

; todo make this more succinct
(defn generate-error-result-hiccup-vector-format-for-xml
  "create an error-map for xml consumption"
  [requestId code errorMessageText]
  [
    :result (list [:requestId {} requestId]
    [:message {} (list [:code {} code]
      [:text {} errorMessageText]
      )
     ]
    )
    ]
  )

(def http-status-code-map
  {
    :ok                        200
    :ok-created                201
    :ok-accepted               202
    :ok-non-authoritative-info 203
    :ok-no-content             204
    :ok-reset-content          205
    :ok-partial-content        206
    :ok-multi-status           207
    :ok-already-reported       208
    :ok-im-used                226
    :ok-low-on-storage-space   250
    ;
    ;200 OK
    ;    Standard response for successful HTTP requests. The actual response will depend on the request method used.
    ;    In a GET request, the response will contain an entity corresponding to the requested resource.
    ;    In a POST request the response will contain an entity describing or containing the result of the action.[2]
    ;
    ;201 Created
    ;        The request has been fulfilled and resulted in a new resource being created.[2]
    ;
    ;
    ;202 Accepted
    ;    The request has been accepted for processing, but the processing has not been completed. The request might or
    ;    might not eventually be acted upon, as it might be disallowed when processing actually takes place.[2]
    ;203 Non-Authoritative Information (since HTTP/1.1)
    ;    The server successfully processed the request, but is returning information that may be from another source.[2]
    ;204 No Content
    ;    The server successfully processed the request, but is not returning any content.[2]
    ;205 Reset Content
    ;    The server successfully processed the request, but is not returning any content. Unlike a 204 response, this response
    ;    requires that the requester reset the document view.[2]
    ;
    ;206 Partial Content
    ;    The server is delivering only part of the resource due to a range header sent by the client.
    ;    The range header is used by tools like wget to enable resuming of interrupted downloads, or split a
    ;        download into multiple simultaneous streams.[2]
    ;
    ;207 Multi-Status (WebDAV; RFC 4918)
    ;    The message body that follows is an XML message and can contain a number of separate response codes,
    ;    depending on how many sub-requests were made.[4]
    ;208 Already Reported (WebDAV; RFC 5842)
    ;    The members of a DAV binding have already been enumerated in a previous reply to this request,
    ;    and are not being included again.
    ;
    ;250 Low on Storage Space (RTSP; RFC 2326)
    ;    The server returns this warning after receiving a RECORD request that it may not be able to fulfill
    ;    completely due to insufficient storage space. If possible, the server should use the Range header to indicate
    ;    what time period it may still be able to record. Since other processes on the server may be consuming storage
    ;    space simultaneously, a client should take this only as an estimate.[5]
    ;
    ;226 IM Used (RFC 3229)
    ;    The server has fulfilled a GET request for the resource, and the response is a representation of the
    ;    result of one or more instance-manipulations applied to the current instance.[6]

    :not-found 404

    :method-not-allowed 405
    ;405 Method Not Allowed
    ;    A request was made of a resource using a request method not supported by that resource;[2]
    ;    for example, using GET on a form which requires data to be presented via POST, or using PUT on a read-only resource.

    :server-error 500
    :bad-request 403
    }
  )

(def mime-type-map
  {
    :xml "text/xml"
    :html "text/html"
    :json "application/json"
    :form-url-encoded "application/x-www-form-urlencoded"
    }
  )



(defn render-user-array-xml [request response status-code-keyword]
  {}
  )

(defn render-user-xml [request response status-code-keyword]
  {}
  )

(defn render-default-xml [request response status-code-keyword]
  {}
)

(defn render-error-xml [request response status-code-keyword]
  {}
  )

;
; response assumed to be a vector [object, status-flag]
;

(defn render-standard-json-response [request response status-code-keyword]
  (let [params (get request :params)
            requestId (get params "requestId")
         ]
    (if (nil? response)
      {
          :requestId requestId
          :status   (http-status-code-map status-code-keyword)
      }
      ; else
      {
          :requestId requestId
          :status   (http-status-code-map status-code-keyword)
          :response (response 0)
      }
    )
  )
)

;
; functions to generate text in format based on data-mapper-fn
;
(defn generate-json-body [request response status-code-keyword data-mapper-fn]
  (cheshire/generate-string
    (data-mapper-fn request response status-code-keyword)
    )
  )

(defn generate-xml-body [request response status-code-keyword data-mapper-fn]
  (xml/emit-str (xml/sexp-as-element (data-mapper-fn request response status-code-keyword)))
  )

(defn generate-html-body [request response status-code-keyword data-mapper-fn]
  (str (data-mapper-fn request response status-code-keyword))
  )

(defn generate-plain-body [request response status-code-keyword data-mapper-fn]
  (str (data-mapper-fn request response status-code-keyword))
  )

(defn generate-form-body [request response status-code-keyword data-mapper-fn]
  (str (data-mapper-fn request response status-code-keyword))
  )

(def data-mapper-fn-map {
                          :recovery-record render-standard-json-response
                          :user-array      render-standard-json-response
                          :user            render-standard-json-response
                          :default         render-standard-json-response
                          :error           render-standard-json-response
                          })

(def xml-data-mapper-fn-map {
                              :recovery-record render-error-xml
                              :user-array render-user-array-xml
                              :user render-user-xml
                              :default render-default-xml
                              :error render-error-xml
                              }
  )



(def body-generation-fn-map
  {
    :xml generate-xml-body
    :html generate-html-body
    :json generate-json-body
    :plain generate-plain-body
    :form-url-encoded generate-form-body
  }
)

(defn generate-http-response-body
  [request response status-code-keyword render-content-type-fn render-data-map-fn]
  "generic function which renders a HTTP response body using render-content-type-fn, render-data-map-fn
   Params:

   request:                 HTTP request
   response:                HTTP response
   status-code-keyword:     what status code was generated
   render-content-type-fn:  function which renders for given content-type
   render-data-map-fn:      function which generates the data map structure
  "
  (render-content-type-fn request response status-code-keyword render-data-map-fn)
  )

(defn generate-http-response-map
  [request response status-code-keyword content-type-keyword data-mapper-keyword]
  {
    :status (http-status-code-map status-code-keyword)
    :headers {"Content-Type" (mime-type-map content-type-keyword)}
    :body (generate-http-response-body request response status-code-keyword (body-generation-fn-map content-type-keyword) (data-mapper-keyword data-mapper-fn-map))
    }
  )




