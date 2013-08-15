(ns mgrapi.spring
  (:import [org.springframework.context.support ClassPathXmlApplicationContext])
)

; The following came from http://stackoverflow.com/questions/16930363/how-to-use-spring-beans-in-a-clojure-application

(declare ^:dynamic *spring-context*)
(declare ^:dynamic *bean-map*)

(declare ^:dynamic account-manager)
(declare ^:dynamic search-manager)
(declare ^:dynamic user-search-manager)

(defn load-context 
  "Load a Spring Framwork Application context based on the locations"
  ([parent locations]
     (doto (new ClassPathXmlApplicationContext (into-array String locations) parent)
       .refresh))
  ([locations]
     (new ClassPathXmlApplicationContext (into-array locations))
  )
)

(defn get-bean
  "Retrieve a bean with provided name"
  [name]
  (if
    (nil? *spring-context*)
    nil
    (.getBean *spring-context* name)
  )
)

(defn get-erights-cce []
  (get-bean "erightsCommandExecutionEnvironment")
)

(defmacro typed-bean [ctx key]
  (let [rtnval (gensym "rtnval") cls (gensym "cls") ]
    `(fn []
       (let [bean# (get-bean ~ctx ~key)
         ~cls (.getType ~ctx ~key)]
     (let [~(with-meta rtnval {:tag cls}) bean#] ~rtnval)))))

(defn create-bean-map
  "Create a map of bean names (as keywords) to functions. Calling the function will return the bean with the given name. ctx - The Spring Application Context"
   ([ctx]
     (let [names (seq (org.springframework.beans.factory.BeanFactoryUtils/beanNamesIncludingAncestors ctx))]
       (apply hash-map (mapcat (fn [f]
                    [(keyword f) (typed-bean ctx f)]) names)))))
(defn &init
  "Define spring funcs and return the Spring Application Context."
  [locs]
  (let [ctx (load-context locs)]
    (def ^:dynamic *spring-context* ctx)
    (def ^:dynamic *bean-map* (create-bean-map ctx))
    (def ^:dynamic account-manager (get-bean "accountManager"))
    (def ^:dynamic search-manager (get-bean "searchManager"))
    (def ^:dynamic user-search-manager (get-bean "userSearchManager"))
    ctx))
