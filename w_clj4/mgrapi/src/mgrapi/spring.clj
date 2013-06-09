(ns mgrapi.spring
  (:import [org.springframework.context.support ClassPathXmlApplicationContext])
)

; The following came from http://stackoverflow.com/questions/16930363/how-to-use-spring-beans-in-a-clojure-application

(declare ^:dynamic *spring-context*)

(defn load-context 
  "Load a Spring Framwork Application context based on the locations"
  ([parent locations]
     (doto (new ClassPathXmlApplicationContext (into-array String locations) parent)
       .refresh))
  ([locations]
     (new ClassPathXmlApplicationContext (into-array locations))))

(defn &init
  "Define spring funcs and return the Spring Application Context."
  [locs]
  (let [ctx (load-context locs)]
    (def ^:dynamic *spring-context* ctx)
    ctx))

(defn get-bean 
  [context name] (. context getBean name))

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

(defn &beans [&ctx] (create-bean-map (&ctx)))

(defn && "Get a bean.  Accepts a string, symbol or keyword"
  ([name]
     (if-let [f (get (&beans) (keyword name))] (f))))

(defmacro with-spring
  [[& beans] & body]
    `(let [badones# (filter  (fn [b#] (not (&& b#))) ~(reduce conj [] (map keyword beans)))]
       (if (seq badones#) (throw (IllegalArgumentException. (str "Undefined beans:" (apply str badones#)))))
       (let ~(reduce conj []
             (mapcat
              (fn [b]
                (vector
                 b
                 (list (list (keyword b) '(&beans)))))
              beans))
         ~@body)))