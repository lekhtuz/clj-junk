(ns cu.business.processor.impl.shopping-cart
  (:use clojure.pprint)

  (:import [com.emeta.cu.business.manager.impl AccountManagerImpl])
  (:import [com.emeta.cu.business.domain UserAccount])
  (:import [com.emeta.cu.business.processor.ShoppingCart])
  (:import [java.util Date])

  (:gen-class
    :name cu.business.processor.impl.ShoppingCart
    :implements [com.emeta.cu.business.processor.ShoppingCart]
  )
  
  (defn -process-cart [cart user-account credit-card additional-data]
    (print "process cart")
  )
)
