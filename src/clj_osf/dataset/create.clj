(ns clj-osf.dataset.create
  "Send a Dataset Create Query to a OSF Dataset Create web service endpoint
  
  The Dataset: Create Web service is used to create a new dataset in a WSF 
  (Web Services Framework). When a dataset is created, it gets described and 
  registered to the WSF and accessible to the other Web services. 
  
  To use the Dataset Create code, you have to:

  ```
  ;; Use/require the namespace
  (require '[clj-osf.dataset.create :as dataset])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Dataset:_Create#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn uri
  "Set the URI of the new dataset to create

  The usage of this function is **Required**

  ##### Parameters
  
  * `[uri]` URI of the new dataset to create.
  
  ##### Usage

  ```
  (dataset/create
    (dataset/uri \"http://sandbox.opensemanticframework/datasets/test/\"))
  ```"
  [uri]
  {:uri uri})

(defn creator
  "Set the reference to the creator of this new dataset

  The usage of this function is **Optional**

  ##### Parameters
  
  * `[uri]` URI of the creator of this new dataset
  
  ##### Usage

  ```
  (dataset/create
    (dataset/creator \"http://sandbox.opensemanticframework.org/wsf/users/admin\"))
  ```"  
  [uri]
  {:creator uri})

(defn description
  "Set the description of the dataset to create

  The usage of this function is **Required**

  ##### Parameters
  
  * `[description]` description of the dataset to create
  
  ##### Usage

  ```
  (dataset/create
    (dataset/description \"Sandbox testing dataset\"))
  ```"
  [description]
  {:description description})

(defn title
  "Set the title of the dataset to create

  The usage of this function is **Required**

  ##### Parameters
  
  * `[title]` title of the dataset to create
  
  ##### Usage

  ```
  (dataset/create
    (dataset/title \"Sandbox testing dataset\"))
  ```"  
  [title]
  {:title title})

(defn target-web-services
  "Specifies which web service endpoint can have access to the data
  contained in this new dataset.

  *Note: you can get the complete list of webservice endpoint URIs registered to a OSF network
  instance by using the `clj-osf.auth.lister/lister` function or by directly using the
  utility class `clj-osf.utils/get-ws-endpoints-uris`*
  
  The usage of this function is **Required**

  ##### Parameters
  
  * `[uris]` A vector of webservice URIs that have access to the content of this dataset.
  
  ##### Usage

  ```
  (dataset/create
    (dataset/target-web-services (utils/get-ws-endpoints-uris)))
  ```

  ```
  (dataset/create
    (dataset/target-web-services [\"http://...\" \"http://...\"]))
  ```"
  [uris]
  {:include_attributes_list (core/params-list uris)})

(defn create
  "Dataset Create query.

  **Required**

  ##### Usage

  ```
  (require '[clj-osf.utils :as utils])
  (require '[clj-osf.dataset.create :as dataset])
  
  (dataset/create
    (dataset/uri \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (dataset/title \"Sandbox testing dataset\")
    (dataset/description \"Sandbox testing dataset\")
    (dataset/target-web-services (utils/get-ws-endpoints-uris))
    (dataset/creator \"http://sandbox.opensemanticframework.org/wsf/users/admin\"))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/dataset/create/" params)))
