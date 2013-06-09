(defproject user-api "0.2.0"
  :description "user-api"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [com.oracle/ojdbc14 "10.2.0.2"]
                 [c3p0/c3p0 "0.9.0.2"]
                 [ring "1.1.6"]
                 [compojure "1.1.3"]
                 [cheshire "4.0.3"]
                 [clj-time "0.4.4"]
                 [org.clojure/data.xml "0.0.6"]
                 [org.clojure/tools.cli "0.2.2"]
                 [clj-logging-config "1.9.10"]
                 [org.clojure/tools.logging "0.2.3"]
                 [leinjacker "0.4.1"]
  ]
  :plugins [
     [codox/codox.leiningen "0.6.4"] 
  ]
  :repositories {"local" "file:///Users/tingde/.m2/repository"}
  :main user-api.core
)
