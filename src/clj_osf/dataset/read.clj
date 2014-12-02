(ns clj-osf.dataset.read
  "Send a Dataset Read Query to a OSF Dataset Read web service endpoint
   
  The Dataset: Read Web service is used to get information (title, 
  description, creator, contributor(s), creation date and last modification 
  date) for a dataset belonging to the WSF (Web Services Framework).
  
  To use the Dataset Read code, you have to:

  ```
  ;; Use/require the namespace
  (require '[clj-osf.dataset.read :as dataset])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Dataset:_Read#Web_Service_Endpoint_Information)"
  (:refer-clojure :exclude [read])
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn uri
  "Set the URI of the dataset for which you want its description

  The usage of this function is **Required**

  ##### Parameters
  
  * `[uri]` URI of the creator of dataset to read
  
  ##### Usage

  ```
  (dataset/read
    (dataset/uri \"http://sandbox.opensemanticframework/datasets/test/\"))
  ```"  
  [uri]
  {:uri uri})

(defn all
  "Read all the accessible datasets

  The usage of this function is **Optional** and replaces `(uri)`

  ##### Usage

  ```
  (dataset/read
    (dataset/all))
  ```"
  []
  {:uri "all"})

(defn read
  "Dataset Read query.

  **Required**

  ##### Usage

  ```
  (require '[clj-osf.utils :as utils])
  (require '[clj-osf.dataset.create :as dataset])
  
  (dataset/read
    (dataset/uri \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->get)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/dataset/read/" params)))
