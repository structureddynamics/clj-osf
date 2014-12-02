(ns clj-osf.crud.delete
  "Send a CRUD Delete Query to a OSF Crud Delete web service endpoint
   
  The CRUD: Delete Web service is used to delete an existing instance record indexed 
  in some target dataset of a WSF. When the instance record gets deleted, all of the 
  information archived in the dataset is deleted as well.

  To use the CRUD Delete code, you have to:

  ```
  ;; Use/require the namespace
  (require '[clj-osf.crud.delete :as crud])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                               :domain \"sandbox.opensemanticframework.org\"
                               :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                               :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/CRUD:_Delete#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn dataset
  "Set the URI(s) of the dataset where the instance record is indexed.
 
  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uris]` Dataset URI where to index the RDF document

  ##### Usage

  ```
  (crud/delete
    (crud/dataset \"http://sandbox.opensemanticframework.org/datasets/test/bob\"))
  ```"    
  [uri]
  {:dataset uri})

(defn hard
  "Specify that this query will delete the published record and all its revisions.
 
  The usage of this function is **Required**
  ##### Usage

  ```
  (crud/delete
    (crud/hard))
  ```"  
  []
  {:mode "hard"})

(defn soft
  "Specify that this query will only delete the published record and not any of its
  possible revision.
 
  The usage of this function is **Required**
  ##### Usage

  ```
  (crud/delete
    (crud/soft))
  ```"    []
  {:mode "soft"})

(defn uri
  "Set the URI(s) of the dataset where the instance record is indexed.
 
  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uris]` Dataset URI where to index the RDF document

  ##### Usage

  ```
  (crud/delete
    (crud/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\"))
  ```"
  [uri]
  {:uri uri})

(defn delete
  "CRUD Delete query.

  **Required**

  ##### Usage

  ```
  (use '[clj-turtle.core])
  (require '[clj-osf.crud.delete :as crud-d])

  ;; Delete an existing record, but keeping the revisions of that record
  ;; This action is like unpublishing the record. It could be recreated
  ;; from its revisions.
  (crud-d/delete
   (crud-d/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
   (crud-d/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\")
   (crud-d/soft))

  ;; Delete an existing record, but deleting all its revisions as well
  (crud-d/delete
   (crud-d/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
   (crud-d/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\")
   (crud-d/hard))  
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (soft)
                       (core/->get)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/crud/delete/" params)))
