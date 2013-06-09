(ns user-api.misc)

(defn nvl
  "if x is nil, return nilvalue otherwise return x"
  [x nilvalue]
  (if (nil? x) nilvalue x)
  )

(defn to-upper-with-nil
  "[x] if x is not nil, then apply String.toUpperCase(). Otherwise return nil
   [x defaultval] if x is not nil, then apply String.toUpperCase(). Otherwise return defaultval"
  ([x] (if (nil? x) nil (.toUpperCase x)))
  ([x defaultval] (if (nil? x) defaultval (.toUpperCase x)))
)

(defn param-map-to-keyword-map [params paramstring]
  (if (nil? (get params paramstring)) {} { (keyword paramstring) (get params paramstring)} )
)



(defn convert-keywords-in-map
    "converts all string keys to keyword keys"
    [thismap]
    (into {}
      (for [[k v] thismap]
        [(keyword k) v]))
  )

(defn string-key-to-keyword-key
    "converts all string keys to keyword keys"
    [thismap]
    (apply merge
         (for [z (seq thismap)]
           {(keyword (first z)) (second z)}
         )
     )
)

(defn param-selection-vector-to-keyword
    "selects the parameters listed in vector and constructs a keyword map (where parameters are converted to keywords)"
    [params params-selection-vector]
    (string-key-to-keyword-key (select-keys params params-selection-vector))
  )

(defn apply-upper-case-to-fields
  "given a vector of map keys contained in map, if a key exists in map, then apply to-upper-with-nil to the key's value if it is not null.
   if a key in the vector does not exist in the map, then ignore it."
  [map vector]
  (apply merge map
    (for [y vector]
        (if (contains? map y) {y (to-upper-with-nil (y map))} {})
    )
  )
)