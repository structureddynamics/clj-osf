(ns clj-osf.auth.registrar.user
  "Send a Auth Registrar User Query to a OSF Auth Registrar User web service endpoint
   
  The Auth Registrar: User Web service is used to register a user to an existing Group.
  This means that the user will have access to all the datasets accessible to that
  group of users.
   
  To use the Auth: Registrar User code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.auth.registrar.user access :as auth-ru])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Auth_Registrar:_User#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn group
  "Specifies the group URI where to register/unregister the user

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` URI of the group where to register/unregister the user

  ##### Usage

  ```
  (auth/registrar-user
    (auth/group \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"))
  ```"
  [uri]
  {:group_uri uri})

(defn user
  "Specifies the user URI to add to the target group

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` URI of the target user

  ##### Usage

  ```
  (auth/registrar-user
    (auth/user \"http://sandbox.opensemanticframework.org/wsf/users/admin\"))
  ```"
  [uri]
  {:user_uri uri})

(defn join-group
  "Specify that we want the user to join that group

  The usage of this function is **Required**

  ##### Usage

  ```
  (auth/registrar-user
    (auth/join-group))
  ```"
  []
  {:action "join"})

(defn leave-group
  "Specify that we want the user to leave that group

  The usage of this function is **Required**

  ##### Usage

  ```
  (auth/registrar-user
    (auth/leave-group))
  ```"
  []
  {:action "leave"})

(defn registrar-user
  "Auth: Registrar User query.

  **Required**

  ##### Usage

  ```
  (require '[clj-osf.auth.registrar.user :as auth-ru])

  ;; Add a user to a group
  (auth-ru/registrar-user
    (auth-ru/group \"http://sandbox.opensemanticframework.org/wsf/groups/test\")
    (auth-ru/user \"http://sandbox.opensemanticframework.org/wsf/users/admin\")
    (auth-ru/join-group))

  ;; Remove a user from a group
  (auth-ru/registrar-user
    (auth-ru/group \"http://sandbox.opensemanticframework.org/wsf/groups/test\")
    (auth-ru/user \"http://sandbox.opensemanticframework.org/wsf/users/admin\")
    (auth-ru/leave-group))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->get)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/auth/registrar/user/" params)))
