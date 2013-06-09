(ns startingclojure.app
  (:use (compojure handler
                   [core :only (GET POST defroutes)]))
  (:require
    compojure.route
    [net.cgrand.enlive-html :as en]
    [ring.util.response :as response]
    [ring.adapter.jetty :as jetty]))

; define handler (needed for ring)
;(defn app
;  [request]
;  {:status 200
;   :body (str "<h1>" (with-out-str (print request)) "</h1>")})

(defonce counter (atom 10000))

(defonce urls (atom {}))

(defn shorten
  [url]
  (let [id (swap! counter inc)
        id (java.lang.Long/toString id 36)]
    (swap! urls assoc id url)))


(en/deftemplate homepage
  (en/xml-resource "homepage.html")
  [request]
  [:#listing :tr] (en/clone-for [[id url] @urls]
                                [:span] (en/content id)
                                [:a] (comp
                                       (fn [e] (update-in e [:content] conj " *click me*"))
                                       (en/content url)
                                       (en/set-attr :href (str \/ id)
                                                    ))))

; compojure handlers
(defn redirect
  [id]
  (response/redirect (@urls id)))

; compojure routes. asterisk means that this is low level implementation
(defroutes app*
  (compojure.route/resources "/")
  (GET "/" request (homepage request))
  (POST "/shorten" request 
        (let [id (shorten (-> request :params :url))]
          (response/redirect "/")))
  (GET "/:id" [id] (redirect id)))

; we added middleware that will do parameter processing for us
(def app (compojure.handler/site app*))

;(defn run
;  []
;  (def server (jetty/run-jetty #'app {:port 8080 :join? false})))
