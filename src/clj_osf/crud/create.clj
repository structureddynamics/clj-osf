(ns clj-osf.crud.create
  "Send a Create Query to a OSF Crud Create web service endpoint
  
  The CRUD: Create Web service is used to create a new instance record 
  in a target dataset registered to a WSF (Web Services Framework). 
  When a new instance record is created, it becomes accessible to the 
  users that have access to this dataset. 
  
  Usage notes: This Web service is intended to be used by any user that wants to update 
  the description of an instance record. The update of a record is performed by two atomic 
  actions: delete and create. All records described within the input RDF document will get 
  updated on the system. For each of them, the Crud Update web service will remove all the 
  triples we have defined for them in the target dataset, and then will re-insert the new 
  ones.
  
  This is the main difference between an Update of a given instance record, and the Creation 
  (using Crud Create) of an already existing record: the update web service guaranties that 
  only the triples of the updated version of an instance record will be in the system. 
  Creating an already existing instance record, will overwrite existing triples, and will 
  add new ones. But the ones from the old version of the instance record that are not in 
  the new version won't be delete in the triple store instance, but will be deleted in 
  the Solr instance because Solr documents can't be updated (they can only be replaced).
  
  It also update possible reification statements.
  
  Warning: if your RDF document contains blank nodes and that you try to update them using 
  the CRUD: Update web service endpoint, this will results in the creation of a new set of 
  resources with new blank nodes URIS. This means that resources specified as blank nodes 
  can't be updated using this web service endpoint. The best practice is not using blank 
  nodes.

  To use the CRUD Create code, you have to:

  ```
  ;; Use/require the namespace
  (require '[clj-osf.crud.create :as crud])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                               :domain \"sandbox.opensemanticframework.org\"
                               :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                               :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/CRUD:_Create#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]
            [cemerick.url :as url]))

(defn dataset
  "Set the URI(s) of the dataset where the instance record is indexed.
 
  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uris]` Dataset URI where to index the RDF document

  ##### Usage

  ```
  (crud/create
    (crud/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"  
  [uri]
  {:dataset uri})

(defn document
  "Set the RDF document where instance record(s) are described. The size of this document 
  is limited to 8MB on the default system (may be lower or higher on different systems).
 
  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[document]` RDF+XML or RDF+N3 documents to import into the system

  ##### Usage

  ```
  (require '[clj-turtle.core :as turtle])
  (require '[clj-osf.crud.create :as crud-c])

  ;; Using clj-turtle
  (crud-c/create
    (crud-c/document
      (crud-c/document
        (turtle/rdf
          (foo :bob) (turtle/a) (foaf :Person)
          (foo :bob) (iron :prefLabel) (turtle/literal \"Bob\" :lang :en)
          (foo :bob) (iron :prefLabel) (turtle/literal \"Robert\" :lang :fr)
                                                (foo :since)
                                                  (turtle/literal \"2014-10-25\"
                                                   :type :xsd:dateTime))))
    (crud-c/is-rdf-n3))
  ```"  
  [document]
  {:document (clojure.string/trim document)})

(defn is-rdf-n3
  "Specifies that the serialization format of the input document is in RDF+N3
 
  The usage of this function is **Required** (or the usage of the `is-rdf-xml` function)

  ##### Usage

  ```
  (crud/create
    (crud/is-rdf-n3))
  ```"    
  []
  {:mime "application/rdf+n3"})

(defn is-rdf-xml
  "Specifies that the serialization format of the input document is in RDF+XML
 
  The usage of this function is **Required** (or the usage of the `is-rdf-n3` function)

  ##### Usage

  ```
  (crud/create
    (crud/is-rdf-xml))
  ```"
  []
  {:mime "application/rdf+xml"})

(defn full-indexation-mode
  "Specifies that you want the document to be indexed everywhere in the system
 
  This is the **default behavior**

  ##### Usage

  ```
  (crud/create
    (crud/full-indexation-mode))
  ```"
  []
  {:mode "full"})

(defn search-indexation-mode
  "Specifies that you want the document to be indexed in the search engine only
 
  The usage of this function is **Optional**

  ##### Usage

  ```
  (crud/create
    (crud/search-indexation-mode))
  ```"
  []
  {:mode "searchindex"})

(defn triplestore-indexation-mode
  "Specifies that you want the document to be indexed in the triple store only
 
  The usage of this function is **Optional**

  ##### Usage

  ```
  (crud/create
    (crud/triplestore-indexation-mode))
  ```"
  []
  {:mode "triplestore"})

(defn create
  "CRUD Create query.

  **Required**

  ##### Usage

  ```
  (use '[clj-turtle.core])
  (require '[clj-osf.crud.create :as crud-c])

  (defns foo \"http://sandbox.opensemanticframework.org/datasets/test/\")
  (defns iron \"http://purl.org/ontology/iron#\")
  
  (crud-c/create
    (crud-c/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (crud-c/document
      (turtle/rdf
        (foo :bob) (turtle/a) (foaf :Person)
        (foo :bob) (iron :prefLabel) (turtle/literal \"Bob\" :lang :en)
        (foo :bob) (iron :prefLabel) (turtle/literal \"Robert\" :lang :fr)
        (foo :bob) (foaf :knows) (foo :alice) (turtle/rei 
                                                (foo :since)
                                                  (turtle/literal \"2014-10-25\"
                                                   :type :xsd:dateTime))))
     (crud-c/full-indexation-mode)
     (crud-c/is-rdf-n3))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (full-indexation-mode)
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/crud/create/" params)))
