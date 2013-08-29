(ns mgrapi.views.myaccount.login
  (:require
    [hiccup.page :refer [html5 include-css]]
    [hiccup.element :refer [image link-to unordered-list]]
    [hiccup.core :refer [html]]
  )
)

(def page-width-numeric 770)

(def page-width (str page-width-numeric "px"))

(defelem magazine-section [title code product subscribe_intkey gift_intkey]
  (html
	  [:b (str "Consumer Reports " title)] [:br]
	  (link-to { :class "font-size11" } (str "https://w1.buysub.com/servlet/CSGateway?cds_mag_code=" code) "Access Account")
	  " | "
	  (link-to { :class "font-size11" } (str "/ec/" product "/order.htm" (if (nil? subscribe_intkey) "" (str "?INTKEY=" subscribe_intkey))) "Subscribe")
	  " | "
	  (link-to { :class "font-size11" } (str "/ec/" product "/gift/order.htm" (if (nil? gift_intkey) "" (str "?INTKEY=" gift_intkey))) "Give a gift")
  )
)

(defn login [request]
  (html5
    [:head
      [:title "Welcome to CRO"]
      (include-css "/css/myaccount/screen.css")
    ]
    [:body
      [:table { :width page-width, :style "margin-left: 15px; margin-top: 0px" }
        [:tr
          [:td { :colspan "2" }
            [:table { :align "right" }
              [:tr
                [:td (link-to "http://custhelp.consumerreports.org/cgi-bin/consumerreports.cfg/php/enduser/home.php" (image "/img/myaccount/head_custservice.gif" "Customer Service")) ]
                [:td (link-to "main.htm" (image "/img/myaccount/head_account.gif" "My Account")) ]
                [:td (link-to "http://www.consumerreports.org/cro/our-web-sites/index.htm" (image "/img/myaccount/head_products_cro.gif")) ]
              ]
            ]
          ]
        ]
        [:tr
          [:td { :colspan "2" }
	          [:table { :width page-width, :style "cursor:pointer; border: 1px solid #B3BBC9;", :background "/img/myaccount/back_dot_cro.gif", :margin-top "0px" }
	            [:tr
	              [:td (link-to "http://www.consumerreports.org" (image "/img/myaccount/head_crtitle.gif")) ]
	            ]
	          ]
          ]
        ]
        [:tr
          [:td { :colspan "2" }
	          [:table { :width page-width }
	            [:tr
	              [:td (link-to "http://www.consumerreports.org" (image { :width (dec page-width-numeric)} "/img/myaccount/account_head.jpg")) ]
	            ]
	          ]
          ]
        ]
        [:tr { :valign "top" }
          [:td { :rowspan "2" }
            [:table { :class "login-subtable" }
              [:tr
                [:th "Manage online products" ]
              ]
              [:tr
                [:td 
                  [:b "If you are already a subscriber to any of the online products or services listed below, enter your username and password to access your account information." ]
                  [:table { :style "color: #474747;" }
                    [:tr
                      [:td { :width "50%", :style "padding: 0px; font-size: 11px; font-weight: bold;" } "- ConsumerReports.org" ]
                      [:td { :rowspan "2", :valign "top", :style "padding: 0px; font-size: 11px; font-weight: bold;" } "- ConsumerReports.org Cars Best Deals Plus" ]
                    ]
                    [:tr
                      [:td { :style "padding: 0px; font-size: 11px; font-weight: bold;" } "- " [:em "CR"] " Car Pricing Service" ]
                    ]
                  ]
                ]
              ]
            ]
          ]
          [:td { :width "40%" }
            [:table { :class "login-subtable" }
              [:tr
                [:th "Manage print products" ]
              ]
              [:tr
                [:td
                 (magazine-section "Magazine" "CNS" "cr" nil "IG106C")
                 [:p]
                 (magazine-section "Magazine" "CRH" "oh" "IW03CHMA" "IG106H")
                 [:p]
                 (magazine-section "Magazine" "CRM" "ma" nil "IG106M")
                 [:p]
                 "Consumer Reports ShopSmart" [:br]
                 "Access Account"
                 " | "
                 "Subscribe"
                 " | "
                 "Give a gift"
                ]
              ]
            ]
          ]
        ]
        [:tr { :valign "top" }
          [:td { :width "40%" }
            [:table { :class "login-subtable" }
              [:tr
                [:th "Other links" ]
              ]
              [:tr
                [:td
                  (unordered-list { :style "margin: 0px;" }
                    '(
                       "Customer service"
                       "Frequently asked questions"
                       "Privacy"
                       "Security"
                       "Reprints and permissions"
                       "Manage your FREE newsletter"
                     )
                  )
                ]
              ]
            ]
          ]
        ]
      ]
    ]
  )
)
