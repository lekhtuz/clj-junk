(defproject mgrapi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [
                 ; Clojure jars
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/java.data "0.1.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [ring/ring-json "0.2.0"]
                 [ring-server "0.2.8"]

                 ; Various third-party jars
                 [commons-beanutils "1.8.3"]
                 [commons-primitives "1.0"]
                 [commons-lang "2.6"] ; latest version is 3.1, but pwiec uses 2.6
                 [commons-logging "1.0.4"] ; latest version is 1.1.1, but erights uses 1.0.4
                 [net.sf.ehcache/ehcache-core "2.6.5"]
                 [org.springframework/spring-context "3.2.3.RELEASE"]
                 [org.slf4j/slf4j-api "1.7.2"]

                 ; Erights/pwiec jars
                 [pwiec/comp-businesslayer "1.2.0"]
                 [pwiec/comp-common "1.2.0"]
                 [pwiec/tools-loader "1.1.1"]
                 [emeta-erightsweb/comp-commandapi "2.2.0"]
                 [emeta-erightsweb/comp-components "2.0.2"]
                 [emeta-erightsweb/comp-utils "1.4.2"]
                 [emeta-erightsweb/comp-erights4x "1.4.1"]
                 [erights "4.0_10261_3"]
                ]
  :plugins [
            [lein-ring "0.8.5"]
           ]
  
  :java-opts ["-Xdebug" "-Xnoagent" "-Djava.compiler=NONE" "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8092" "-Djava.rmi.server.logCalls=false"]
  
  :ring {
         :handler mgrapi.handler/war-handler
         :init mgrapi.handler/init
         :destroy mgrapi.handler/destroy
        }
  
  :profiles {
             :production {
                          :ring {
                                 :open-browser? false
                                 :stacktraces? false
                                 :auto-reload? false
                                }
                         }
             :dev {
                   :dependencies [
                        [ring-mock "0.1.5"]
                        [ring/ring-devel "1.1.8"]
                        ]
                   :ring {
                          :open-browser? false
                          :stacktraces? true
                          :auto-reload? true
                         }
                  }
            }
)
