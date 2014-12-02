(ns clj-osf.revision.lister
  "Send a Revision Lister Query to a OSF Revision Lister web service endpoint
   
  The Revision: Lister web service endpoint is used to list all the revisions 
  existing for a record. All the revision records have a unix timestamp in 
  microseconds. This timestamp is defined as a double. All the revisions 
  records can be sorted using this timestamp. If a user want to see what 
  was the description of a record at a specific time, then he will use the 
  Revision: Read web service endpoint to get all the triple of that record, 
  for that revision.
   
  To use the Revision: Lister code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.revision.lister :as revision-l])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Revision:_Lister#Web_Service_Endpoint_Information)"  
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
  (revision/lister
    (revision/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"        
  [uri]
  {:dataset uri})

(defn long-results
  "Specifies that we want the long description of the results record. Returned record is described using
  their date stamp (for ordering purposes) their URI, the performer of the revision and their
  lifecycle stage status.

  The usage of this function is the **Default behavior**
  
  ##### Usage

  ```
  (revision/lister
    (revision/long-results)
  ```"
  []
  {:mode "long"})

(defn short-results
  "Specifies that we want the short description of the results record. Returned record is described using
  their date stamp (for ordering purposes) and their URI.

  The usage of this function is the **Default behavior**
  
  ##### Usage

  ```
  (revision/lister
    (revision/short-results)
  ```"
  []
  {:mode "short"})

(defn uri
  "Specifies the URI of the record for which you want the list of revisions.

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` URI of the record for which you want its list of revisions

  ##### Usage

  ```
  (revision/lister
    (revision/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\"))
  ```"        
  [uri]
  {:uri uri})

(defn lister
  "Revision: Lister query.

  **Required**

  ##### Usage

  ```
  ;; Get all the revisions of the test:bob record
  (revision/lister
    (revision/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (revision/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\")
    (revision/short-results))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->mime "application/json")
                       (core/->get))
        params (merge default params)]
    (core/osf-query "/ws/revision/lister/" params)))
