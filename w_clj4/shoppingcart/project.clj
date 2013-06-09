(defproject shoppingcart "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://www.consumerreports.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
;  :omit-source true
;  :aot [cu.business.processor.impl.shopping-cart cu.business.domain-objects]
;  :aot :all
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [commons-logging "1.1.1"]
                 [pwiec/comp-businesslayer "1.2.0"]
                 [pwiec/comp-common "1.2.0"]
                 [emeta-erightsweb/comp-commandapi "2.2.0"]
                 [emeta-erightsweb/comp-components "2.0.2"]
                 [emeta-erightsweb/comp-erights4x "1.4.1"]
                 [erights "4.0_10261_3"]
                ]
  :main shoppingcart.core
)
