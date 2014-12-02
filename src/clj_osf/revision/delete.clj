(ns clj-osf.revision.delete
  "Send a 
   
  To use the Revision: Delete code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.revision.delete :as revision-d])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Revision:_Delete#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn dataset
  "Specifies the dataset URI where the record is indexed.

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` The dataset URI where the record is indexed. This is the URI of the dataset, and not
            the URI of the revisions dataset

  ##### Usage

  ```
  (revision/delete
    (revision/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"        
  [uri]
  {:dataset uri})

(defn uri
  "Specifies the URI of the record for which you want the list of revisions.

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` URI of the revision record to delete

  ##### Usage

  ```
  (revision/delete
    (revision/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\"))
  ```"        
  [uri]
  {:revuri uri})

(defn delete
  "Revision: Lister query.

  **Required**

  ##### Usage

  ```
  (revision/delete
    (revision/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (revision/uri \"http://sandbox.opensemanticframework.org/datasets/test/revisions/1417180339.0395\"))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->mime "application/json")
                       (core/->get))
        params (merge default params)]
    (core/osf-query "/ws/revision/delete/" params)))
