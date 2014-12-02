(ns clj-osf.revision.diff
  "Send a Revision Diff Query to a OSF Revision Diff web service endpoint
   
  The Revision: Diff web service endpoint is used to compare two revisions of the 
  same record. A ChangeSet which contains all the added and removed triples 
  between the two revisions is returned.
   
  To use the Revision: Diff code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.revision.diff :as revision-diff])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Revision:_Diff#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn dataset
  "Specifies the dataset URI where the record is indexed.

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` The dataset URI where the record is indexed. This is the URI of the dataset, and not
            the URI of the revisions dataset.

  ##### Usage

  ```
  (revision/diff
    (revision/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"        
  [uri]
  {:dataset uri})

(defn left-revision
  "Specifies the first revision URI to compare.

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` First revision URI to compare.

  ##### Usage

  ```
  (revision/diff
    (revision/left-revision \"http://sandbox.opensemanticframework.org/datasets/test/revisions/1417180339.0395\"))
  ```"        
  [uri]
  {:lrevuri uri})

(defn right-revision
  "Specifies the second revision URI to compare.

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` Second revision URI to compare.

  ##### Usage

  ```
  (revision/diff
    (revision/left-revision \"http://sandbox.opensemanticframework.org/datasets/test/revisions/14171485932.0395\"))
  ```"        
  [uri]
  {:rrevuri uri})

(defn diff
  "Revision: Diff query.

  **Required**

  ##### Usage

  ```
  ;; Compare two revisions and return the differences between the two
  (revision-diff/diff
    (revision-diff/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (revision-diff/left-revision \"http://sandbox.opensemanticframework.org/datasets/test/revisions/1417180339.0395\")
    (revision-diff/right-revision \"http://sandbox.opensemanticframework.org/datasets/test/revisions/1417186032.4905\"))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->mime "application/json")
                       (core/->get))
        params (merge default params)]
    (core/osf-query "/ws/revision/diff/" params)))

