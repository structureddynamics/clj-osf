(ns clj-osf.auth.registrar.group
  "Send a Auth Registrar Group Query to a OSF Auth Registrar Group web service endpoint
   
  The Auth Registrar: Group Web service is used to register a new Group to the OSF
  instance. This group will be used to give access to the group's users to certain
  datasets.
   
  To use the Auth: Registrar Group code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.auth.registrar.group access :as auth-rg])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Auth_Registrar:_Group#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn application
  "Specifies the Application ID where the group should be created

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[id]` ID of the application

  ##### Usage

  ```
  (auth/registrar-group
    (auth/application \"administer\"))
  ```"      
  [id]
  {:app_id id})

(defn create-group
  "Specify that we want to create a new group

  The usage of this function is **Required** (if `(delete-group)` is not used)
  
  ##### Usage

  ```
  (auth/registrar-group
    (auth/create-group))
  ```"      
  []
  {:action "create"})

(defn delete-group
  "Specify that we want to delete a new group

  The usage of this function is **Required** (if `(create-group)` is not used)
  
  ##### Usage

  ```
  (auth/registrar-group
    (auth/delete-group))
  ```"      
  []
  {:action "delete"})

(defn group
  "Specifies the group URI

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` URI of the target group

  ##### Usage

  ```
  (auth/registrar-group
    (auth/group \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"))
  ```"      
  [uri]
  {:group_uri uri})

(defn registrar-group
  "Auth: Registrar Group query.

  **Required**

  ##### Usage

  ```
  (require '[clj-osf.auth.registrar.group :as auth-rg])

  (auth-rg/registrar-group
    (auth-rg/group \"http://sandbox.opensemanticframework.org/wsf/groups/test\")
    (auth-rg/application \"administer\")
    (auth-rg/create-group))

  (auth-rg/registrar-group
    (auth-rg/group \"http://sandbox.opensemanticframework.org/wsf/groups/test\")
    (auth-rg/application \"administer\")
    (auth-rg/delete-group))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->get)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/auth/registrar/group/" params)))
