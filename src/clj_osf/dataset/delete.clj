(ns clj-osf.dataset.delete
  "Send a Dataset Delete Query to a OSF Dataset Delete web service endpoint
   
  The Dataset: Delete Web service is used to delete an existing dataset in a 
  WSF (Web Services Framework). When a dataset gets deleted, all of the 
  information archived in it is deleted as well. There is no way to recover 
  any data once this query is issued. 

  To use the Dataset Delete code, you have to:

  ```
  ;; Use/require the namespace
  (require '[clj-osf.dataset.delete :as dataset])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                               :domain \"sandbox.opensemanticframework.org\"
                               :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                               :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Dataset:_Delete#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn uri
  "Set the URI of the new dataset to delete

  The usage of this function is **Required**

  ##### Parameters
  
  * `[uri]` URI of the new dataset to delete.
  
  ##### Usage

  ```
  (dataset/delete
    (dataset/uri \"http://sandbox.opensemanticframework/datasets/test/\"))
  ```"
  
  [uri]
  {:uri uri})

(defn delete
  "Dataset Delete query.

  **Required**

  ##### Usage

  ```
  (require '[clj-osf.utils :as utils])
  (require '[clj-osf.dataset.create :as dataset])
  
  (dataset/delete
    (dataset/uri \"http://sandbox.opensemanticframework/datasets/test/\"))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->get)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/dataset/delete/" params)))
