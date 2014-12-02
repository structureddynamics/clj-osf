(ns clj-osf.revision.update
  "Send a Revision Update Query to a OSF Revision Update web service endpoint
   
  The Revision: Update web service endpoint is used to change the lifestage status 
  of a revision record. If you change the lifecycle stage of a unpublished revision 
  to published, then this will replace the currently published record by this newly 
  published revision. If you change the status of a currently published record to 
  something else than published, then it will unpublish the record, and this record 
  won't be accessible anymore in the 'public' dataset. This record will always be 
  available via its revisions, however if the CRUD: Read web service endpoint is 
  used with its URI, then it will return an error saying the record is not existing 
  in the dataset. However, you could re-publish this record anytime in the future 
  using this Revision: Update web service endpoint.
  
  To use the Revision: Update code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.revision.update :as revision-u])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Revision:_Update#Web_Service_Endpoint_Information)"  
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
  (revision/update
    (revision/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"        
  [uri]
  {:dataset uri})

(defn uri
  "Specifies the URI of the record for which you want the list of revisions.

  *Note:* this URI can be found using the `(clj-osf.revision.lister/lister)` function
  
  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` URI of the record for which you want its list of revisions

  ##### Usage

  ```
  (revision/update
    (revision/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\"))
  ```"
  [uri]
  {:revuri uri})

(defn is-archive
 "Specify that the record being updated has a lifecycle stage status 'archive'
  
  The usage of this function, or one of the `is-XYZ` function, is **Required**
  
  ##### Usage

  ```
  (revision/update
    (revision/is-archive))
  ```"
  []
  {:lifecycle "archive"})

(defn is-experimental
 "Specify that the record being updated has a lifecycle stage status 'experimental'
  
  The usage of this function, or one of the `is-XYZ` function, is **Required**
  
  ##### Usage

  ```
  (revision/update
    (revision/is-experimental))
  ```"
  []
  {:lifecycle "experimental"})

(defn is-harvesting
 "Specify that the record being updated has a lifecycle stage status 'experimental'
  
  The usage of this function, or one of the `is-XYZ` function, is **Required**
  
  ##### Usage

  ```
  (revision/update
    (revision/is-experimental))
  ```"
  []
  {:lifecycle "harvesting"})

(defn is-pre-release
 "Specify that the record being updated has a lifecycle stage status 'pre-release'
  
  The usage of this function, or one of the `is-XYZ` function, is **Required**
  
  ##### Usage

  ```
  (revision/update
    (revision/is-pre-release))
  ```"
  []
  {:lifecycle "pre_release"})

(defn is-published
 "Specify that the record being updated has a lifecycle stage status 'published'
  
  The usage of this function, or one of the `is-XYZ` function, is **Required**
  
  ##### Usage

  ```
  (revision/update
    (revision/is-published))
  ```"
  []
  {:lifecycle "published"})

(defn is-staging
 "Specify that the record being updated has a lifecycle stage status 'staging'
  
  The usage of this function, or one of the `is-XYZ` function, is **Required**
  
  ##### Usage

  ```
  (revision/update
    (revision/is-staging))
  ```"
  []
  {:lifecycle "staging"})

(defn is-unspecified
 "Specify that the record being updated has a lifecycle stage status 'unspecified'
  
  The usage of this function, or one of the `is-XYZ` function, is **Required**
  
  ##### Usage

  ```
  (revision/update
    (revision/is-unspecified))
  ```"
  []
  {:lifecycle "unspecified"})

(defn update
  "Revision: Update query.

  **Required**

  ##### Usage

  ```
  ;; Update the status of the `test:bob` record to \"archive\"
  (revision-u/update
    (revision-u/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (revision-u/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\")
    (revision-u/is-archive))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->mime "application/json")
                       (core/->get))
        params (merge default params)]
    (core/osf-query "/ws/revision/update/" params)))
