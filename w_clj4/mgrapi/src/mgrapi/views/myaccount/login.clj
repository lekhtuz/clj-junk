(ns mgrapi.views.myaccount.login
  (:require
;    [hiccup.core :refer [html]]
    [hiccup.element :refer [image link-to unordered-list]]
    [hiccup.page :refer [html5 include-css]]
    [hiccup.util :refer [url]]
    [hiccup.form :refer [form-to label text-field password-field submit-button]]
  )
)

; http://stackoverflow.com/questions/12679754/idiomatic-way-of-rendering-style-info-using-clojure-hiccup
(defn style [& info]
  {
    :style (.trim (apply str (map #(let [[kwd val] %] (str (name kwd) ": " val "; ")) (apply hash-map info))))
  }
)

(def page-width-numeric 770)

(def page-width (str page-width-numeric "px"))

(defn- magazine-section
  (
    [title code subscribe_url gift_url]
    (list
	    [:b (str "Consumer Reports " title)] [:br]
      [:span { :class "font-size11" }
    	  (link-to (url "https://w1.buysub.com/servlet/CSGateway" { :cds_mag_code code }) "Access Account")
	      " | "
	      (link-to subscribe_url "Subscribe")
        " | "
	      (link-to gift_url "Give a gift")
      ]
    )
  )
  (
    [title code product subscribe_intkey gift_intkey]
    (magazine-section
      title
      code
      (url "/ec/" product "/order.htm" (if (nil? subscribe_intkey) {} { :INTKEY subscribe_intkey }))
      (url "/ec/" product "/gift/order.htm" (if (nil? gift_intkey) {} { :INTKEY gift_intkey }))
    )
  )
)

(defn login [request]
  (html5
    [:head
      [:title "Welcome to CRO"]
      (include-css "/css/myaccount/screen.css")
    ]
    [:body
      [:table (style :margin-left "15px" :margin-top "0px" :width page-width)
        [:tr
          [:td { :colspan 2 }
            [:table { :align "right" }
              [:tr
                [:td (link-to "http://custhelp.consumerreports.org/cgi-bin/consumerreports.cfg/php/enduser/home.php" (image "/img/myaccount/head_custservice.gif" "Customer Service")) ]
                [:td (link-to "/ec/myaccount/main.htm" (image "/img/myaccount/head_account.gif" "My Account")) ]
                [:td (link-to "http://www.consumerreports.org/cro/our-web-sites/index.htm" (image "/img/myaccount/head_products_cro.gif")) ]
              ]
            ]
          ]
        ]
        [:tr
          [:td { :colspan 2 }
	          [:table (style :cursor "pointer" :border "1px solid #B3BBC9" :background "url(/img/myaccount/back_dot_cro.gif)" :margin-top "20px" :width page-width)
	            [:tr
	              [:td (link-to "http://www.consumerreports.org" (image "/img/myaccount/head_crtitle.gif")) ]
	            ]
	          ]
          ]
        ]
        [:tr
          [:td { :colspan 2 }
	          [:table { :width page-width }
	            [:tr
	              [:td (link-to "http://www.consumerreports.org" (image { :width (dec page-width-numeric)} "/img/myaccount/account_head.jpg")) ]
	            ]
	          ]
          ]
        ]
        [:tr { :valign "top" }
          [:td { :rowspan 2 }
            [:table { :class "login-subtable" }
              [:tr
                [:th "Manage online products" ]
              ]
              [:tr
                [:td 
                  [:b "If you are already a subscriber to any of the online products or services listed below, enter your username and password to access your account information." ]
                  [:table (style :color "#474747")
                    [:tr
                      [:td { :width "50%", :style "padding: 0px; font-size: 11px; font-weight: bold;" } "- ConsumerReports.org" ]
                      [:td { :rowspan 2, :valign "top", :style "padding: 0px; font-size: 11px; font-weight: bold;" } "- ConsumerReports.org Cars Best Deals Plus" ]
                    ]
                    [:tr
                      [:td { :style "padding: 0px; font-size: 11px; font-weight: bold;" } "- " [:em "CR"] " Car Pricing Service" ]
                    ]
                  ]
                  (form-to [:put ""]
                    [:table { :id "login-table" }
                      [:tr
                        [:td (label (style :font-weight "bold") "userName" "Username:") ]
                        [:td (text-field "userName") ]
                        [:td { :rowspan 2, :valign "bottom" } (submit-button "Log In")]
                      ]
                      [:tr
                        [:td (label (style :font-weight "bold") "password" "Password:") ]
                        [:td (password-field "password") ]
                      ]
                      [:tr
                        [:td { :colspan 3 }
                          [:span { :class "font-size10" }
                            (link-to "/ec/myaccount/forgot_username.htm" "Forgot your username?")
                            [:br]
                            (link-to "/ec/myaccount/forgot_password.htm" "Forgot your password?")
                            [:br]
                            (link-to "http://custhelp.consumerreports.org/cgi-bin/consumerreports.cfg/php/enduser/std_adp.php?p_faqid=34" "Need help loggin in?")
                          ]
                        ]
                      ]
                    ]
                  )
                ]
              ]
              [:tr
                [:td
	                [:strong
	                  [:span (style :color "#CC0000") "Not a subscriber? "]
	                  "Learn more about our online products and services."
	                ]
                  [:p]
                  [:strong "ConsumerReports.org" ] [:br]
                  [:span { :class "font-size10" }
                    (link-to "/cro/features-tools/index.htm" "Learn More")
                    " | "
                    (link-to "/ec/cro/order.htm?INTKEY=I925LT0" "Subscribe")
                    " | "
                    (link-to "/ec/cro/gift/order.htm?INTKEY=GY3700A" "Give a gift subscription")
                  ]
                  [:p]
                  [:strong "ConsumerReports.org Cars Best Deals Plus" ] [:br]
                  [:span { :class "font-size10" }
                    (link-to "/ec/carp/order.htm?INTKEY=I990C" "Subscribe")
                  ]
                  [:p]
                  [:strong "ConsumerReports.org Cars Pricing Service" ] [:br]
                  [:span { :class "font-size10" }
                    (link-to "/ec/aps/order.htm?INTKEY=WA37M00" "Subscribe")
                  ]
                ]
              ]
            ]
          ]
          [:td { :width "40%" }
            [:table { :class "login-subtable" :width "95%" }
              [:tr
                [:th "Manage print products" ]
              ]
              [:tr
                [:td
                  (magazine-section "Magazine" "CNS" "cr" nil "IG106C")
                  [:p]
                  (magazine-section "on Health" "CRH" "oh" "IW03CHMA" "IG106H")
                  [:p]
                  (magazine-section "Money Adviser" "CRM" "ma" nil "IG106M")
                  [:p]
                  (magazine-section "ShopSmart" "SHM"
                    (url "https://w1.buysub.com/servlet/OrdersGateway" { :cds_mag_code "SHM", :cds_page_id 42071, :cds_response_key "" })
                    (url "https://w1.buysub.com/pubs/C8/SHM/lp45.jsp" { :cds_mag_code "SHM", :cds_page_id 43404 })
                  )
                ]
              ]
            ]
          ]
        ]
        [:tr { :valign "top" }
          [:td { :width "40%" }
            [:table { :class "login-subtable" :width "95%" }
              [:tr
                [:th "Other links" ]
              ]
              [:tr
                [:td
                  (unordered-list (style :padding-left "16px" :margin-top "0px" :margin-bottom "0px" )
                    (list
                      (link-to "http://custhelp.consumerreports.org/cgi-bin/consumerreports.cfg/php/enduser/home.php" "Customer service")
                      (link-to "http://custhelp.consumerreports.org/cgi-bin/consumerreports.cfg/php/enduser/home.php" "Frequently asked questions")
                      (link-to "http://www.consumerreports.org/cro/customer-service/privacy.htm" "Privacy")
                      (link-to "http://www.consumerreports.org/cro/customer-service/security.htm" "Security")
                      (link-to "http://www.consumerreports.org/cro/customer-service/reprints/reprints-and-permissions.htm" "Reprints and permissions")
                      (link-to "http://www.consumerreports.org/cro/customer-service/email-service/e-mail-newsletters/index.htm?email=" "Manage your FREE newsletter")
                    )
                  )
                ]
              ]
            ]
          ]
        ]
        [:tr
          [:td { :colspan "2", :class "font-size10" }
            "Copyright &copy; 2006-2013 " 
            (link-to "http://www.consumerreports.org" "Consumer Reports")
            ". No reproduction, in whole or in part, without written "
            (link-to "http://www.consumerreports.org/cro/about-us/no-commerical-use-policy/permission-requests/index.htm" "permission")
            "."
          ]
        ]
      ]
    ]
  )
)
