(ns clj-osf.dataset.update
  "Send a Dataset Update Query to a OSF Dataset Update web service endpoint
   
  The Dataset: Update Web service is used to update the description of an 
  existing dataset in a WSF (Web Services Framework).
  
  To use the Dataset Update code, you have to:

  ```
  ;; Use/require the namespace
  (require '[clj-osf.dataset.update :as dataset])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Dataset:_Update#Web_Service_Endpoint_Information)"
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn uri
  "Set the URI of the new dataset to update

  The usage of this function is **Required**

  ##### Parameters
  
  * `[uri]` URI of the new dataset to update.
  
  ##### Usage

  ```
  (dataset/update
    (dataset/uri \"http://sandbox.opensemanticframework/datasets/test/\"))
  ```"
  [uri]
  {:uri uri})

(defn title
  "Set the title of the dataset to update

  The usage of this function is **Required**

  ##### Parameters
  
  * `[title]` title of the dataset to update
  
  ##### Usage

  ```
  (dataset/update
    (dataset/title \"Sandbox testing updating dataset\"))
  ```"  
  [title]
  {:title title})

(defn modified
  "Specifies the date of the modification of the dataset

  The usage of this function is **Optional**

  ##### Parameters
  
  * `[date]` Date of the modification of the dataset
  
  ##### Usage

  ```
  (dataset/update
    (dataset/modified \"27/11/2014\"))
  ```"  
  [date]
  {:modified date})

(defn description
  "Set the description of the dataset to update

  The usage of this function is **Required**

  ##### Parameters
  
  * `[description]` description of the dataset to update
  
  ##### Usage

  ```
  (dataset/update
    (dataset/description \"Sandbox testing dataset update\"))
  ```"
  [description]
  {:description description})

(defn contributors
  "Redefine the contributors to this dataset

  **Usage Note:** If this function is not called, the reference to all contributors will be
                  removed from the descrioption of this dataset

  ##### Parameters
  
  * `[uris]` Vector of contributors URIs
  
  ##### Usage

  ```
  (dataset/update
    (dataset/contributors [\"http://sandbox.opensemanticframework.org/datasets/users/a/\"
                           \"http://sandbox.opensemanticframework.org/datasets/users/b/\"]))
  ```"
  [uris]
  {:contributors (core/params-list uris)})

(defn update
  "Dataset Update query.

  **Required**

  ##### Usage

  ```
  (require '[clj-osf.utils :as utils])
  (require '[clj-osf.dataset.create :as dataset])
  
  (dataset/update
    (dataset/uri \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (dataset/title \"Sandbox testing dataset - update\")
    (dataset/description \"Sandbox testing dataset - update\")
    (dataset/contributors [\"http://sandbox.opensemanticframework.org/datasets/users/a/\"
                           \"http://sandbox.opensemanticframework.org/datasets/users/b/\"]))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/dataset/update/" params)))
