(ns user-api.config
  (:require [clojure.tools.cli          :as tools-cli])
  )

(defn convert-keywords-in-map [thismap]
    (into {}
      (for [[k v] thismap]
        [(keyword k) v]))
  )

(defn load-props [filename]
    "reads file from [filename] and loads into property map"
    (let [io (java.io.FileInputStream. filename)
        prop (java.util.Properties.)]
    (.load prop io)
    (into {} prop)))

(defn load-clj-config [filename]
    "reads clj configuration file"
    (with-open [r (clojure.java.io/reader filename)]
      (read (java.io.PushbackReader. r))))

(defn load-config-file [filename]
    "auto detect a config file and "
    (cond
        (.endsWith filename ".properties")
            (load-props filename)
        (.endsWith filename ".clj")
            (load-clj-config filename)
        :else
            (load-props filename)
    )
)

(defn process-cli [args]
        (tools-cli/cli args
           ["-p" "--port" "Listen on this port" :default 8084 :parse-fn #(Integer. %)]
           ["-m" "--mode" "which mode to use" :default "prod"]
           ;["-d" "--db-prop-file" "which db configuration file to use" :default "user-api-dbspec.properties"]
           ["-u" "--usage" "this usage information" :flag true]
        )
  )

(def crAppIdMap {
              :dev       "dev123"
              :qa        "qa456"
              :staging   "staging6789"
              :prod      "prod01234"
    }
  )

(defn setup-config-map [cli-vector]
  (let [
         arg-data          (first cli-vector)
         other-args-vector (second cli-vector)
         usage             (last cli-vector)
         mode              (arg-data :mode)
         ;dbspec-prop-file  (arg-data :db-prop-file)
         port              (arg-data :port)
       ]
       (merge {:jetty-options {:port port}} {:appId (crAppIdMap (keyword mode))})
       ;{:dbspec (convert-keywords-in-map (load-config-file dbspec-prop-file))})
  )
)
