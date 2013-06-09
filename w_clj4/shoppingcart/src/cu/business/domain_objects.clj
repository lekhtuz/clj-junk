(ns cu.business.domain-objects
;  (:import [com.emeta.cu.business.processor.ShoppingCart])
)

(defrecord email-address [email-address]
)

(defrecord address [title prefix first-name middle-name last-name suffix 
                    address1 address2 address3 city state postal-code country 
                    province phone mobile-phone ok-to-update-mobile]
)

(defrecord user-account [user-name id address password email-address credit-card]
)

(defrecord cart [cartitems billing-address credit-card quote quote-calculator]
;  ShoppingCart
)