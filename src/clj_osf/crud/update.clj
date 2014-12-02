(ns clj-osf.crud.update
  "Send a Crud Update Query to a OSF Crud Update web service endpoint
   
  The CRUD: Update Web service is used to update an existing instance record 
  indexed in a target dataset part of a WSF (Web Services Framework). 
  
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

  To use the CRUD Update code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.crud.update :as crud])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/CRUD:_Update#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn create-revision
  "Specify that we want to create a revision record for this update.
 
  This is the **default behavior**

  ##### Usage

  ```
  (crud/update
    (crud/create-revision))    
  ```"
  []
  {:revision "true"})

(defn ignore-revision
  "Specify that we do not want to create a revision record for this update.
 
  The usage of this function is **Optional**

  ##### Usage

  ```
  (crud/update
    (crud/ignore-revision))    
  ```"
  []
  {:revision "false"})

(defn dataset
  "Set the URI(s) of the dataset where the instance record is indexed.
 
  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uris]` Dataset URI where to index the RDF document

  ##### Usage

  ```
  (crud/update
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
  (crud/update
    (crud/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (crud/document
      (turtle/rdf
        (foo :bob) (turtle/a) (foaf :Person)
        (foo :bob) (iron :prefLabel) (turtle/literal \"Bob\" :lang :en)
        (foo :bob) (iron :prefLabel) (turtle/literal \"Robert\" :lang :fr)
        (foo :bob) (foaf :knows) (foo :alice) (turtle/rei 
                                                (foo :since) (turtle/literal \"2014-10-25\"
                                                              :type :xsd:dateTime))
        (foo :bob) (foaf :knows) (foo :eli) (turtle/rei 
                                              (foo :since) (turtle/literal \"2014-11-20\"
                                                            :type :xsd:dateTime))))

    (crud/is-rdf-n3))    
  ```"  
  [document]
  {:document document})

(defn is-rdf-n3
  "Specifies that the serialization format of the input document is in RDF+N3
 
  The usage of this function is **Required** (or the usage of the `is-rdf-xml` function)

  ##### Usage

  ```
  (crud/update
    (crud/is-rdf-n3))
  ```"      
  []
  {:mime "application/rdf+n3"})

(defn is-rdf-xml
  "Specifies that the serialization format of the input document is in RDF+XML
 
  The usage of this function is **Required** (or the usage of the `is-rdf-n3` function)

  ##### Usage

  ```
  (crud/update
    (crud/is-rdf-xml))
  ```"
  []
  {:mime "application/rdf+xml"})

(defn status-archive
  "Specify that we do not want to create a revision record for this update.
 
  The usage of this function is **Required** if no other status is specified

  ##### Usage

  ```
  (crud/update
    (crud/status-archive))
  ```"  
  []
  {:lifecycle "archive"})

(defn status-experimental 
  "Specify that the record(s) beind update have a lifecycle stage status 'experimental'
 
  The usage of this function is **Required** if no other status is specified

  ##### Usage

  ```
  (crud/update
    (crud/status-experimental))
  ```"  
  []
  {:lifecycle "experimental"})

(defn status-harvesting
  "Specify that the record(s) beind update have a lifecycle stage status 'harvesting'
 
  The usage of this function is **Required** if no other status is specified

  ##### Usage

  ```
  (crud/update
    (crud/status-harvesting))
  ```"  
  []
  {:lifecycle "harvesting"})

(defn status-pre-release
  "Specify that the record(s) beind update have a lifecycle stage status 'pre-release'
 
  The usage of this function is **Required** if no other status is specified

  ##### Usage

  ```
  (crud/update
    (crud/status-pre-release))
  ```"  
  []
  {:lifecycle "pre_release"})

(defn status-published
  "Specify that the record(s) beind update have a lifecycle stage status 'published'

  This means that the record will be available/visible in the dataset to the users
  that have access to it.
 
  The usage of this function is **Required** if no other status is specified

  ##### Usage

  ```
  (crud/update
    (crud/status-published))
  ```"
  []
  {:lifecycle "published"})

(defn status-staging
  "Specify that the record(s) beind update have a lifecycle stage status 'staging'
 
  The usage of this function is **Required** if no other status is specified

  ##### Usage

  ```
  (crud/update
    (crud/status-staging))
  ```"
  []
  {:lifecycle "staging"})

(defn status-unspecified
  "Specify that the record(s) beind update have a lifecycle stage status 'unspecified'
 
  The usage of this function is **Required** if no other status is specified

  ##### Usage

  ```
  (crud/update
    (crud/status-unspecified))
  ```"
  []
  {:lifecycle "unspecified"})


(defn update
  "CRUD Update query.

  **Required**

  ##### Usage

  ```
  (use '[clj-turtle.core])
  (require '[clj-osf.crud.create :as crud-c])

  (defns foo \"http://sandbox.opensemanticframework.org/datasets/test/\")
  (defns iron \"http://purl.org/ontology/iron#\")
  
  (turtle/defns iron \"http://purl.org/ontology/iron#\"
  (turtle/defns foo \"http://sandbox.opensemanticframework.org/datasets/test/\")
  (turtle/defns foaf \"http://xmlns.com/foaf/0.1/\")

  (crud/update
    (crud/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (crud/document
      (turtle/rdf
        (foo :bob) (turtle/a) (foaf :Person)
        (foo :bob) (iron :prefLabel) (turtle/literal \"Bob\" :lang :en)
        (foo :bob) (iron :prefLabel) (turtle/literal \"Robert\" :lang :fr)
        (foo :bob) (foaf :knows) (foo :alice) (turtle/rei 
                                                (foo :since) (turtle/literal \"2014-10-25\"
                                                              :type :xsd:dateTime))
        (foo :bob) (foaf :knows) (foo :eli) (turtle/rei 
                                              (foo :since) (turtle/literal \"2014-11-20\"
                                                            :type :xsd:dateTime))))

    (crud/is-rdf-n3)
    (crud/status-published))  
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/crud/update/" params)))
