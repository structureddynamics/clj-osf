(ns clj-osf.revision.read
  "Send a Revision Read Query to a OSF Revision Read web service endpoint
   
  The Revision: Read web service endpoint is used to read a specific revision of a record. 
  This endpoint will return all the triples, including reification triples, of a specific 
  revision record. This web service endpoint can be used to get all the triples, which 
  includes the triples that defines the revision itself. But it can also be used to re-create 
  the original state of the record when it got revisioned. This \"original\" state simple 
  remove the revision specific triples and change the URI to its original one (and not 
  the revision URI). 
   
  To use the Revision: Read code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.revision.read :as revision])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Revision:_Read#Web_Service_Endpoint_Information)"  
  (:refer-clojure :exclude [read])  
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
  (revision/read
    (revision/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"        
  [uri]
  {:dataset uri})

(defn uri
  "Specifies the URI of the revision URI record to read

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` URI of the revision record to read

  ##### Usage

  ```
  (revision/read
    (revision/uri \"http://sandbox.opensemanticframework.org/datasets/test/revisions/1417180339.0395\"))
  ```"        
  [uri]
  {:revuri uri})

(defn get-record
  "Specifies that you want the endpoint to return the record of that revision, without all the meta information 
  about the revision. The URI of the record that will be returned will be different the one specified in this. 
  parameter. The URI that will be used is the one of the actual record, so the one specified by the 
  `wsf:revisionUri` property if the mode revision is used

  The usage of this function is the **default behavior**

  ##### Usage

  ```
  (revision/read
    (revision/get-record))
  ```"        
  []
  {:mode "record"})

(defn get-revision
  "Specifies that you want the endpoint to return the full revision record, with all the information specific 
  to the revision (status, revision time, performed, etc). The URI of the record that will be returned will 
  be the same as the one used for this parameter

  The usage of this function is **Optional**
  
  ##### Usage

  ```
  (revision/read
    (revision/get-revision)
  ```"        
  []
  {:mode "revision"})

(defn read
  "Revision: Read query.

  **Required**

  ##### Usage

  ```
  (revision-r/read
    (revision-r/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (revision-r/uri \"http://sandbox.opensemanticframework.org/datasets/test/revisions/1417180339.0395\")
    (revision-r/get-revision))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (get-record)
                       (core/->mime "application/json")
                       (core/->get))
        params (merge default params)]
    (core/osf-query "/ws/revision/read/" params)))
