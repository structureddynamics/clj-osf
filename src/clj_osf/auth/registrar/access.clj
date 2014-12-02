(ns clj-osf.auth.registrar.access
  "Send a Auth Registrar Access Query to a OSF Auth Registrar Access web service endpoint
   
  The Auth Registrar: Access Web service is used to register (create, update and delete) 
  an access for a given IP address, to a specific dataset and all the registered Web 
  services endpoints registered to the WSF (Web Services Framework) with given CRUD 
  (Create, Read, Update and Delete) permissions in the WSF. 
   
  This Web service is intended to be used by content management systems, developers or 
  administrators to manage access to WSF (Web Service Framework) resources (users, 
  datasets, Web services endpoints).
   
  This web service endpoint is used to create what we refer to as an access permissions 
  record. This record describe the CRUD permissions, for a certain IP address, to use a 
  set of web service endpoints, to query a target dataset.
   
  To use the Auth: Registrar Access  code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.auth.registrar.access :as auth-ra])
  (require '[clj-osf.utils :as utils])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Auth_Registrar:_Access#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn crud-permissions
  "Define a CRUD permissions record to be used as input to other Auth Registrar: Access functions

  ##### Parameters
  
  * `[create]` Specify if you want create permissions for this CRUD record
  * `[read]` Specify if you want read permissions for this CRUD record
  * `[update]` Specify if you want update permissions for this CRUD record
  * `[delete]` Specify if you want delete permissions for this CRUD record

  ##### Usage

  ```
  (auth/registrar-access
    (auth/create \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"
                 \"http://sandbox.opensemanticframework.org/datasets/test/\"
                 (auth/crud-permissions true true true true)
                 (utils/get-ws-endpoints-uris)))
  ```"      
  [create read update delete]
  [create read update delete])

(defn create
  "Create a new access permissions record

  ##### Parameters
  
  * `[group-uri]` Target Group URI related to the acces record being created
  * `[dataset-uri]` Specifies which dataset URI is targeted by the access record.
  * `[permissions]` A CRUD permission object instance that define the permissions granted 
                    for the target IP, target Dataset and target Web Service Endpoints of 
                    this access permission record. See `(crud-permissions)`.
  * `[web-services-uris]` Specifies which web service endpoint URI are targetted by this access record.
                          Only the web service endpoints URIs that will be defined in this access record
                          will be able to access/use data for the user and dataset defined in this access
                          record. Note: you can get the complete list of webservice endpoint URIs 
                          registered to a OSF network instance by using the
                          `(clj-osf.utils.get-ws-endpoints-uris)` function.

  ##### Usage

  ```
  (auth/registrar-access
    (auth/create \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"
                 \"http://sandbox.opensemanticframework.org/datasets/test/\"
                 (auth/crud-permissions true true true true)
                 (utils/get-ws-endpoints-uris)))
  ```"      
  [group-uri dataset-uri permissions web-services-uris]
  {:group group-uri
   :dataset dataset-uri
   :crud (str (if (nth permissions 0) "True" "False") ";"
              (if (nth permissions 1) "True" "False") ";"
              (if (nth permissions 2) "True" "False") ";"
              (if (nth permissions 3) "True" "False"))
   :ws_uris (core/params-list web-services-uris)
   :action "create"})

(defn update
  "Update an existing access permissions record

  ##### Parameters
  
  * `[access-uri]` Access record URI to modify
  * `[group-uri]` Target Group URI related to the acces record being created
  * `[dataset-uri]` Specifies which dataset URI is targeted by the access record.
  * `[permissions]` A CRUD permission object instance that define the permissions granted 
                    for the target IP, target Dataset and target Web Service Endpoints of 
                    this access permission record. See `(crud-permissions)`.
  * `[web-services-uris]` Specifies which web service endpoint URI are targetted by this access record.
                          Only the web service endpoints URIs that will be defined in this access record
                          will be able to access/use data for the user and dataset defined in this access
                          record. Note: you can get the complete list of webservice endpoint URIs 
                          registered to a OSF network instance by using the
                          `(clj-osf.utils.get-ws-endpoints-uris)` function.

  ##### Usage

  ```
  (auth/registrar-access
    (auth/update \"http://sandbox.opensemanticframework.org/wsf/access/a986129592045faaa4380e47c340953e\"
                 \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"
                 \"http://sandbox.opensemanticframework.org/datasets/test/\"
                 (auth/crud-permissions true true true true)
                 (utils/get-ws-endpoints-uris)))
  ```"      
  [access-uri group-uri dataset-uri permissions web-services-uris]
  {:target_access_uri access-uri
   :group group-uri
   :dataset dataset-uri
   :crud (str (if (nth permissions 0) "True" "False") ";"
              (if (nth permissions 1) "True" "False") ";"
              (if (nth permissions 2) "True" "False") ";"
              (if (nth permissions 3) "True" "False"))
   :ws_uris (core/params-list web-services-uris)
   :action "update"})

(defn delete-all-dataset-accesses
  "Delete all accesses permissions records for a specific dataset

  ##### Parameters
  
  * `[uri]` Dataset URI for which we delete all the access record defined for it

  ##### Usage

  ```
  (auth/registrar-access
    (auth/delete-all-dataset-accesses \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"      
  [uri]
  {:dataset uri
   :action "delete_all"})

(defn delete-all-group-accesses
  "Delete all accesses permissions records for a specific group

  ##### Parameters
  
  * `[uri]` Group URI for which we delete all the access record defined for it

  ##### Usage

  ```
  (auth/registrar-access
    (auth/delete-all-group-accesses \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"))
  ```"      
  [uri]
  {:group uri
   :action "delete_all"})

(defn delete-specific-access
 "Delete a specific access record

  ##### Parameters
  
  * `[uri]` URI of the access record to delete from the system
  
  ##### Usage
  
  ```
  (auth/registrar-access
    (auth/delete-specific-access \"http://sandbox.opensemanticframework.org/wsf/access/a986129592045faaa4380e47c340953\"))
  ```"
 [uri]
  {:target_access_uri uri
   :action "delete_specific"})

(defn delete-target-access
 "Delete a target access permissions record for a specific IP address and a specific dataset 

  ##### Parameters
  
  * `[group]` Target Group URI related to the acces record being created
  * `[dataset]` Dataset URI defined for the access record to delete
  
  ##### Usage
  
  ```
  (auth/registrar-access
    (auth/delete-target-access \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"
                               \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"
  [group dataset]
  {:group group
   :dataset dataset
   :action "delete_target"})

(defn registrar-access
  "Auth: Registrar Access query.

  **Required**

  ##### Usage

  ```
  (require '[clj-osf.auth.registrar.access :as auth-ra])

  (auth-ra/registrar-access
    (auth-ra/create \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"
                    \"http://sandbox.opensemanticframework.org/datasets/test/\"
    (auth-ra/crud-permissions true true true true)
    (utils/get-ws-endpoints-uris)))

  (auth-ra/registrar-access
    (auth-ra/update \"http://sandbox.opensemanticframework.org/wsf/access/a986129592045faaa4380e47c340953e\"
                    \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"
                    \"http://sandbox.opensemanticframework.org/datasets/test/\"
    (auth-ra/crud-permissions false false false false)
    (utils/get-ws-endpoints-uris)))
  
  (auth-ra/registrar-access
    (auth-ra/delete-all-dataset-accesses \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/auth/registrar/access/" params)))
