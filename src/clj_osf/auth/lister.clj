(ns clj-osf.auth.lister
  "Send a Auth Lister Query to a OSF Auth Lister web service endpoint
   
  The Auth: Lister Web service is used to list all of the datasets accessible to a given user, 
  list all of the datasets accessible to a given user with all of its CRUD permissions, to 
  list all of the Web services registered to the WSF (Web Services Framework) and to list all 
  of the CRUD permissions, for all users, for a given dataset created on a WSF.
   
  This Web service is used to list all the things that are registered / authenticated in a 
  Web Service Framework network.

  To use the Auth: Lister code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.auth.lister :as auth])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Auth:_Lister#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn dataset-groups-accesses
  "Specifies that this query will return all the group access records in the OSF network instance.
  This information will only be returned if the requester has permissions on the core OSF registry dataset.
  
  ##### Parameters
  
  * `[uri]` the URI of the target dataset for which you want the access records for all its users

  ##### Usage

  ```
  (auth/lister
    (auth/dataset-group-accesses \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"    
  [uri]
  {:mode "access_dataset"
   :dataset uri})

(defn datasets-uris
  "Specifies that this query will return all the datasets URI currently existing, and accessible by the user,
  in the OSF network instance.
  
  ##### Usage

  ```
  (auth/lister
    (auth/datasets-uris)
  ```"
  []
  {:mode "datasets"})

(defn group-accesses
  "Specifies that this query will return all the datasets URI currently existing, and accessible by the group,
  in the OSF network instance, along with their CRUD permissions.
  
  ##### Parameters
  
  * `[uri]` the URI of the group for which you want all the accessible datasets
  
  ##### Usage

  ```
  (auth/lister
    (auth/group-accesses \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"))
  ```"
  [uri]
  {:mode "access_group"
   :group uri})

(defn groups
  "Specifies that this query will return all the groups currently defined in the OSF instance.
  
  ##### Usage

  ```
  (auth/lister
    (auth/groups))
  ```"  
  []
  {:mode "groups"})

(defn group-users
  "Specifies that this query will return all the users registered to a group
  
  ##### Parameters
  
  * `[uri]` the URI of the group for which you want all its users
  
  ##### Usage

  ```
  (auth/lister
    (auth/group-users \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"))
  ```"  
  [uri]
  {:mode "group_users"
   :group uri})


(defn user-groups
  "Specifies that this query will return all the groups where the user is registered to

  Note that the user is the one that make the request to the OSF instance.  
  
  ##### Usage

  ```
  (auth/lister
    (auth/user-groups))
  ```"  
  []
  {:mode "user_groups"})

(defn user-accesses
  "Specifies that this query will return all the datasets URI currently existing, and accessible by the user,
  in the OSF network instance, along with their CRUD permissions.
  
  ##### Usage

  ```
  (auth/lister
    (auth/user-accesses))
  ```"
  []
  {:mode "access_user"})

(defn registered-web-service-endpoints-uri
  "Specifies that this query will return all the web service endpoints URI currently registered
  to this OSF network instance.
  
  ##### Usage

  ```
  (auth/lister
    (auth/registered-web-service-endpoints-uri))
  ```"
  []
  {:mode "ws"})

(defn include-all-web-service-uris
  "Specifies if you want to get all the WebService URIs along with all the access records.
  Depending on the usecase, this list can be quite large and the returned resultset
  can be huge.
  
  ##### Usage

  ```
  (auth/lister
    (auth/include-all-web-service-uris))
  ```"
  []
  {:target_webservice "all"})

(defn include-no-web-service-uris
  "Specifies if you do not want to include any web service URIs for the access records.
  
  ##### Usage

  ```
  (auth/lister
    (auth/include-no-web-service-uris))
  ```"
  []
  {:target_webservice "none"})

(defn include-target-web-service-uri
  "Specifies which target web service you want to include in the resultset
  
  ##### Parameters
  
  * `[uri]` URI of the web service endpoint to include
  
  ##### Usage

  ```
  (auth/lister
    (auth/include-target-web-service-uri \"http://sandbox.opensemanticframework.org/wsf/ws/auth/lister/\"))
  ```"    
  [uri]
  {:target_webservice uri})


(defn lister
  "Auth: Lister query.

  **Required**

  ##### Usage

  ```
  (require '[clj-osf.auth.lister :as auth-l])

  ;; Get all groups accesses for a given dataset
  (auth-l/lister
    (auth-l/dataset-groups-accesses \"http://sandbox.opensemanticframework.org/datasets/test/\"))

  ;; Get all the datasets accessible to a group of users
  (auth-l/lister
    (auth-l/group-accesses \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"))

  ;; Get all the groups of the OSF instance
  (auth-l/lister
    (auth-l/groups))

  ;; Get all the users of a target group
  (auth-l/lister
    (auth-l/group-users \"http://sandbox.opensemanticframework.org/wsf/groups/administrators\"))

  ;; Get all the groups of a target user
  (auth-l/lister
    (auth-l/user-groups))

  ;; Get all the accesses available for that user
  (auth-l/lister
    (auth-l/user-accesses))

  ;; Specifying the target web service endpoint to include in the resultset
  (auth-l/lister
    (auth-l/user-accesses)
    (auth-l/include-target-web-service-uri \"http://sandbox.opensemanticframework.org/wsf/ws/auth/lister/\"))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->get)
                       (core/->mime "application/json")
                       (datasets-uris)
                       (include-all-web-service-uris))
        params (merge default params)]
    (core/osf-query "/ws/auth/lister/" params)))
