(ns user-api.usertransmock
  (:use clojure.tools.logging
        clj-logging-config.log4j
        user-api.misc)
  (:import [java.util UUID Date])
  )

(set-logger! :pattern "%d{dd MMM yyyy HH:mm:ss,SSS} %p [%t] - %m%n")

(defrecord User [user-id login password email])
(defrecord Session [session-token session-data user-id])
(defrecord Client [client-token user-id user-agent client-ip])
(defrecord RecoveryRecord [recovery-token temp-password type user-id email-address create-date modified-date confirmed-date])
(defrecord EmailConfirmation [email-token user-id email create-date])
(defrecord License [user-id url product expiration renewing?])
(defrecord Entitlement [user-id issue-id])
(defrecord Profile [user-id avatar gender birthdate])

(defn make-data-base
  []
  (let [d {
    :last-id (ref 0)
    :users-by-login (ref {})
    :users-by-email (ref {})
    :users-by-id (ref {})
    :users-by-client (ref {})
    :sessions-by-token (ref {})
    :sessions-by-user (ref {})
    :clients-by-token (ref {})
    :recrecs-by-token (ref {})
    ;:recrecs-by-user (ref {})
    :confirmations-by-token (ref {})
    :licenses-by-user (ref {})
    :entitlements-by-user (ref {})
    } ]
    d
  )
)

(defn send-matching-names-email
  [email names]
)

(defn send-reset-link-email
  [email reset-link]
)

(defn generate-temp-password
  [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes)))))
)

(defn generate-uuid
  []
  (let [uuid (.toString (UUID/randomUUID))
        token (.replaceAll uuid "-" "")]
    token)
)

(defn generate-recovery-token
  []
  (generate-uuid)
)

(defn generate-session-token
  []
  (generate-uuid)
)

(defn generate-confirmation-token
  []
  (generate-uuid)
)

(defn generate-client-token
  []
  (generate-uuid)
)

(defn make-license
  [user-id url product renewing]
  (License. user-id url product "2015-01-01" renewing)

;  {
;    "u" url
;    "p" product
;    "x" "2015-01-01"
;    "r" renewing
;  }
)

(defn make-cro-license
  [user-id]
  (make-license user-id "/cro/*" "CRO" true)
)

(defn make-aps-license
  [user-id]
  (make-license user-id "/aps/new/2009/chevrolet/silverado-1500-lt-4x4-ext-cab-mwb.htm" "APS" false)
)

(defn authorize-client
  [saved-ua saved-ip user-agent client-ip]
  (and (= saved-ua user-agent) (= saved-ip client-ip))
)

(defn authorize-password
  [password saved-password]
  (= password saved-password)
)

(defprotocol IdentifierW
  ""
  (createUser [d login password email] "create a new id and return new User")
  (updateUser [d old-user new-user] "update password or email of the
id, return new user")
)

(defprotocol IdentifierR
  ""
  (findUserById [d user-id] "look up id, return user")
  (findUserByLogin [d login] "look up login, return user")
)

(defprotocol IdentityConfirmerW
  ""
  (initiateEmailConfirmation [d user] "return email-confirmation record")
  (completeEmailConfirmation [d email-confirmation] "updates the account, returns nil, ok, or fails with error, false")
  (requestUsernameRecoveryRecord [d email] "return new or existing email-token, recovery-password for forgot username")
  (requestPasswordRecoveryRecord [d user-id] "return new or existing email-token, recovery-password for forgot password")
  (updateRecoveryRecord [d token action] "")
)

(defprotocol IdentityConfirmerR
  ""
  (findUsersByEmail [d email] "returns vector of [user-id user] pairs matching the email address, ok")
  (findUserByLoginEmail [d login email] "returns user, ok, or nil false")
  (findRecoveryRecord [d recovery-token] "returns the recovery record, ok or nil false")
  (findUserByEmailConfirmation [d email-token] "returns the user that matches the email token")
)

(defprotocol AuthenticatorW
  ""
  (loginUser [d login password] "returns session, ok")
  (loginClient [d client-token user-agent client-ip] "returns session, ok")
  (renewSessionToken [d session-token] "returns session-token, ok")
  (updateSessionData [d session-token session-data] "writes session-data to the session")
  (releaseSession [d session] "returns nil, ok")
  (requestClientToken [d session-token user-agent client-ip] "returns client, ok")
  (releaseClientToken [d client] "returns nil, ok" )
)

(defprotocol AuthenticatorR
  ""
  (findUserBySession [d session-token] "returns user for session, ok")
)

(defprotocol AuthorizerW
  ""
  (grantLicense [d license] "grant a license to a user, return the list of licenses, ok")
  (revokeLicense [d license] "remove a license from a user, return the list of licenses, ok")
  (grantEntitlement [d entitlement] "grant an entitlement to a user, return the list of entitlements, ok")
  (revokeEntitlement [d entitlement] "revoke an entitlement from a user, return the list of entitlements, ok")
)

(defprotocol AuthorizerR
  ""
  (verifyEntitlement [d user-id entitlement] "confirm that right now a given user has a given entitlement")
  (listEntitlements [d user-id] "returns list of entitlements, ok")
  (listLicenses [d user-id] "returns list of licenses, ok")
)


(deftype MockUserBackend
  [r]

  IdentifierW

  (createUser
    [d login password email]
    (let [ubl       (:users-by-login r)
          ube       (:users-by-email r)
          ubi       (:users-by-id r)
          last-id   (:last-id r)]
      (dosync
        (let [upperlogin (to-upper-with-nil login)
              upperemail (to-upper-with-nil email)
              user-with-login (@ubl upperlogin)
              users-with-email (@ube upperemail)]
          (if (not (empty? user-with-login))
          ; look up login in map, if found fail
            [nil false]
            (let [new-user (User. @last-id upperlogin password upperemail)]
              (alter ubl assoc upperlogin new-user)
              (info (str "before ube = " @ube))
              (alter ube assoc upperemail (set (conj users-with-email new-user)))
              (info (str "after ube = " @ube))
              (alter ubi assoc @last-id new-user)
              (info (str "last-id = " @last-id "; ubl = " @ubl))
              (alter last-id inc)
              (info "new last-id = " @last-id)
              [new-user true])))))
  )

  (updateUser
    [d user-id changes]
    (let [
          ubi (:users-by-id r)
          old-user (@ubi user-id)
          new-user (merge old-user (apply-upper-case-to-fields changes [:email]))
         ]
      (info "updateUser: changes = " changes)
      (info "updateUser: new user = " new-user)
      (let [old-user-id (:user-id old-user)
          old-login   (:login old-user)
          new-user-id (:user-id new-user)
          new-login   (:login new-user)]
          ; throw error if login changes
          (if (or
                  (not= old-user-id new-user-id)
                  (not= old-login new-login)
              )
            [nil false]
            (let [ubl         (:users-by-login r)
                  ube         (:users-by-email r)
                  ubi         (:users-by-id r)
                  old-email   (:email old-user)
                  new-email   (:email new-user)
              ]
              (info "updateUser: before ubl = " @ubl)
              (info "updateUser: before ube = " @ube)
              (info "updateUser: before ubi = " @ubi)
              (dosync
                ; remove old user
                (alter ube assoc old-email (disj (@ube old-email) old-user))
                ; add the new user
                (alter ube assoc new-email (set (conj (@ube new-email) new-user)))
                (alter ubl assoc old-login new-user)
                (alter ubi assoc old-user-id new-user)
              )
              (info "updateUser: after ubl = " @ubl)
              (info "updateUser: after ube = " @ube)
              (info "updateUser: after ubi = " @ubi)

              [new-user true]
            )
          )
        )

    )
  )

  IdentifierR

  (findUserById
    [d user-id]
    (do
    (info "user-id = " user-id "; r = " (str r))
    (let [users-by-id (:users-by-id r)
          user        (@users-by-id user-id)]
      (if (empty? user)
        [nil false]
        [user true]))
    )
  )

  (findUserByLogin
    [d login]
    (let [users-by-login  (:users-by-login r)
          user            (@users-by-login (to-upper-with-nil login)) ]
      (if (empty? user)
        [nil false]
        [user true]))
  )

  IdentityConfirmerW

  (initiateEmailConfirmation
    [d user]
    ; create the email confirmation record  for the user-id
    (let [user-id       (:user-id user)
          email         (:email user)
          token         (generate-confirmation-token)
          confirmation  (EmailConfirmation. token user-id email (Date.))
          cbt           (:confirmations-by-token r)]
      ; save it, replacing any existing
      (dosync
        (alter cbt assoc token confirmation))
      ; return it, true
      [confirmation true])
  )

  (completeEmailConfirmation
    [d email-confirmation]
    ; look up the confirmation to confirm the token exists, confirm it is equal
    ; if it exists, then remove it, return nil, true
    ; if not return confirmation, false
    (let  [cbt  (:confirmations-by-token r)
           token (:email-token email-confirmation)]
      (dosync
        (let [existing (@cbt token)]
          (if (empty? existing)
            [email-confirmation false]
            (do
              (alter cbt dissoc token)
              [nil true])))))
  )

  (requestUsernameRecoveryRecord
      [d email]
      ; create a new recovery record
      (let [
            token    (generate-recovery-token)
            password (generate-temp-password 12)
            recrec   (RecoveryRecord. token password "forgot-username" nil (to-upper-with-nil email) (Date.) (Date.) nil)
            rbt      (:recrecs-by-token r) ]
        ; save it, replace any existing
        (dosync
          (alter rbt assoc token recrec)
         )
        ; return it, true
        [recrec true])
    )

  (requestPasswordRecoveryRecord
    [d user]
    ; create a new recovery record
    (let [user-id  (:user-id user)
          token    (generate-recovery-token)
          password (generate-temp-password 12)
          recrec   (RecoveryRecord. token password "forgot-password" user-id nil (Date.) (Date.) nil)
          rbt      (:recrecs-by-token r)]
      ; save it, replace any existing
      (dosync
        (alter rbt assoc token recrec)
        )
      ; return it, true
      [recrec true])
  )

  (updateRecoveryRecord
    [d token action]
    ; look up the recovery to confirm it exists, confirm it is equal
    ; if so, remove it, return nil, true
    ; if not return recovery, false
    (let [rbt      (:recrecs-by-token r)
          saved-rec-rec (@rbt token)]
      (dosync
          (if (empty? saved-rec-rec)
              [nil false]
              (cond
                 (and (= "confirm" action) (nil? (:confirmed-date saved-rec-rec)))
                    (let [updated-rec-rec (assoc saved-rec-rec :confirmed-date (Date.))]

                       (info "updated-rec-rec = " updated-rec-rec "confirmation date = " (:confirmed-date updated-rec-rec))
                       (alter rbt dissoc token)
                       (alter rbt assoc token updated-rec-rec)
                       [updated-rec-rec true]
                    )
                 :else
                    [nil false]
              )
          )
      )
    )
  )

  IdentityConfirmerR

  (findUsersByEmail
    [d email]
    ; lookup the set of users by email
    (let [ube     (:users-by-email r)
          users   (@ube (to-upper-with-nil email))]
      (if (empty? users)
        [nil false]
        [users true]))
  )

  (findUserByLoginEmail
    [d login email]
    ; lookup by login, confirm email matches
    (let [ubl     (:users-by-login r)
          user    (@ubl (to-upper-with-nil login))]
      (if (empty? user)
        [nil false]
        (if (not= (:email user) (to-upper-with-nil email))
          [nil false]
          [user true])))
  )

  (findRecoveryRecord
    [d recovery-token]
    ; lookup by recovery token
    (let [rbt (:recrecs-by-token r)
          rr  (@rbt recovery-token)]
      (if (empty? rr)
        [nil false]
        [rr true]))
  )

  (findUserByEmailConfirmation
    [d email-token]
    ; lookup by email-token
    (let [cbt           (:confirmations-by-token r)
          confirmation  (@cbt email-token)]
      (if (empty? confirmation)
        [nil false]
        ; get the user using the user-id
        (let [ubi         (:users-by-id r)
              user-id     (:user-id confirmation)
              user        (@ubi user-id)]
          [user true])))
  )

  AuthenticatorW

  (loginUser
    [d login password]
    (let [ubl (:users-by-login r)]
      (dosync
        ; lookup user by login
        (let [user (@ubl login)]
          ; if doesn't exist, nil false
          (if (empty? user)
            [nil false]
            ; compare passwords
            (let [saved-password  (:password user)
                  user-id         (:user-id user)]
              (if (authorize-password password saved-password)
                (let [sessions-by-token   (:sessions-by-token r)
                      sessions-by-user    (:sessions-by-user r)
                      session-token       (generate-session-token)
                      session             (Session. session-token nil user-id)]
                  (alter sessions-by-token assoc session-token session)
                  (alter sessions-by-user assoc user-id session)
                  [session true])
                [nil false]))))))
  )

  (loginClient
    [d client-token user-agent client-ip]
    ; lookup client token
    (let [cbt (:clients-by-token r)]
      (dosync
        (let [client (@cbt client-token)]
          (if (empty? client)
            [nil false]
            (let [saved-ua (:user-agent client)
                  saved-ip (:client-ip client)]
              ; confirm user agent and client ip match
              (if (authorize-client saved-ua saved-ip user-agent client-ip)
                ; if so, lookup the user for this client
                ; create new session for this user, add it to the things, and return it
                (let [ubc                 (:users-by-client r)
                      user                (@ubc client-token)
                      user-id             (:user-id user)
                      sessions-by-token   (:sessions-by-token r)
                      sessions-by-user    (:sessions-by-user r)
                      session-token       (generate-session-token)
                      session             (Session. session-token nil user-id)]
                  (alter sessions-by-token assoc session-token session)
                  (alter sessions-by-user assoc user-id session)
                  [session true])
                [nil false]))))))
  )

  (releaseSession
    [d session]
    ; lookup the session
    (let [sbt             (:sessions-by-token r)
          session-token   (:session-token session)]
      (dosync
        (let [saved-session   (@sbt session-token)]
          (if (empty? saved-session)
            ; if not return session false
            [session nil]
            ; if found, remove it, and return nil true
            (do
              (alter sbt dissoc session-token)
              [nil true])))))
  )

  (requestClientToken
    [d session-token user-agent client-ip]
    ; confirm the session token is valid
    (let [sbt  (:sessions-by-token r)
          ubi  (:users-by-id r)
          cbt  (:clients-by-token r)]
      (dosync
        (let [saved-session (@sbt session-token)]
          (if (empty? saved-session)
            [nil false]
            ; generate a new client token
            (let [user-id         (:user-id saved-session)
                  user            (@ubi user-id)
                  client-token    (generate-client-token)
                  client          (Client. client-token user-id
user-agent client-ip)]
              ; save it,replacing any other client token
              (alter cbt assoc client-token client)
              ; return the client object, ok
              [client true])))))
  )

  (releaseClientToken
    [d client]
    ; lookup the client token
    (let [cbt           (:clients-by-token r)
          client-token  (:client-token client)]
      (dosync
        (let [saved-client (@cbt client-token)]
          (if (empty? saved-client)
            ; if not, return client and false
            [client false]
            (do
              ; if found, remove it, return nil true
              (alter cbt dissoc client-token)
              [nil true])))))

  )

  AuthenticatorR

  (findUserBySession
    [d session-token]
    ; confirm session token is valid
    (let [sbt  (:sessions-by-token r)
          session (@sbt session-token)]
      (if (empty? session)
        [nil false]
        ; if so, get the user-id, get the user
        (let [ubi (:users-by-id r)
              user-id (:user-id session)
              user (@ubi user-id)]
          (if (empty? user)
            [nil false]
            ; return it, true
            [user true]))))
  )

  AuthorizerW

  (grantLicense
    [d license]
    (let [ubi       (:users-by-id r)
          user-id   (:user-id license)]
      (dosync
        ; confirm valid user
        (let [user (@ubi user-id)]
          (if (empty? user)
            [nil false]
            ; lookup licenses by user
            (let [lbu           (:licenses-by-user r)
                  licenses      (@lbu user-id)
                  ; add the license, even if there are other ones like it
                  new-licenses  (if (empty? licenses) [license] (conj
licenses license)) ]
              ; save with the user
              (alter lbu assoc user-id new-licenses)
              ; return all licenses, true
              [new-licenses true])))))
  )

  (revokeLicense
    [d license]
    ; lookup the license by user-id
    (let [lbu (:licenses-by-user r)
          user-id (:user-id license)]
      (dosync
        (let [saved-licenses  (@lbu user-id)
              ; filter out matching licenses, return remaining licenses, true
              new-licenses    (remove (fn [i] (= license i)) saved-licenses)]
          (alter lbu assoc user-id new-licenses)
          ; other license, false
          [new-licenses true])))
  )

  (grantEntitlement
    [d entitlement]
    ; confirm valid user
    (let [ebu (:entitlements-by-user r)
          ubi (:users-by-id r)
          user-id (:user-id entitlement)]
      (dosync
        ; look up entitlements by user
        (let [saved-entitlements (@ebu user-id)
              ; add this entitlement to the set, so no dupes
              new-entitlements (if (empty? saved-entitlements) (set
entitlement) (conj saved-entitlements entitlement))]
          (alter ebu assoc user-id new-entitlements)
          ; return the set, true
          [new-entitlements true])))
  )

  (revokeEntitlement
    [d entitlement]
    ; look up the entitlements
    (let [ebu (:entitlements-by-user r)
          ubi (:users-by-id r)
          user-id (:user-id entitlement)]
      (dosync
        (let [saved-entitlements (@ebu user-id)
              ; remove this on from the set, return the set, true
              new-entitlements (if (empty? saved-entitlements)
saved-entitlements (disj saved-entitlements entitlement))]
          (alter ebu assoc user-id new-entitlements)
          [new-entitlements true])))
  )

  AuthorizerR

  (listEntitlements
    [d user-id]
    ; confirm valid user
    ; lookup entitlements, return them
    (let [ebu (:entitlements-by-user r)
          ubi (:users-by-id r)
          user (@ubi user-id)
          saved-entitlements (@ebu user-id)]
      (if (nil? user)
        [nil false]
        (if (nil? saved-entitlements)
          [[] true]
          [saved-entitlements true])))
  )

  (listLicenses
    [d user-id]
    (let [lbu             (:licenses-by-user r)
          ubi             (:users-by-id r)
          user            (@ubi user-id)
          saved-licenses  (@lbu user-id)]
      (if (nil? user)
        [nil false]
        (if (nil? saved-licenses)
          [[] true]
          [saved-licenses true])))
  )
)