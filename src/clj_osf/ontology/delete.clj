(ns clj-osf.ontology.delete
  "Send an Ontology Delete service is used to delete an OWL ontology existing in the 
  OSF instance, or an entity in one of the ontology. An entity can be anything 
  of: a class, an object property, a datatype property, an annotation property or a 
  named individual.
   
  This service is a web service wrapper over the OWLAPI ontology library. It wraps all 
  the needed functionalities related to delete an ontology or an entity in an ontology. 
  Most of the related API has been implemented. So we can say that web service (with the 
  other related services) turns the OWLAPI into a web service API.
   
  To use the Ontology: Delete code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.ontology.delete :as ontology-d])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Ontology_Delete#Web_Service_Endpoint_Information)"    
  (:require [clj-osf.core :as core]
            [clojure.string :as string]
            [cemerick.url :as url]))

(defn ontology
  "URI of the ontology; the URI of an Ontology dataset is the URL used to import that 
  ontology in the system. The URL can be the URI of the ontology if it was resolvable 
  on the Web, or the URL where the OWL file, containing the ontology's description, 
  can be resolved by the server (on the web, on the file system, etc) via a URL.

  *Note:* you can get the list of all loaded ontologies by using the
          `(clj-osf.ontology.read/get-loaded-ontologies)`

  The usage of this function is **Required**
  
  ##### Usage

  ```
  (ontology/delete
    (ontology/ontology \"http://purl.org/ontology/bibo/\")
  ```"  
  [uri]
  {:ontology uri})

(defn delete-ontology
  "Delete the ontology from the system
  
  ##### Usage

  ```
  (ontology/delete
    (ontology/delete-ontology)
  ```"    
  []
  {:function "deleteOntology"})

(defn delete-property
  "Delete a property from an ontology on the system
  
  ##### Parameters
  
  * `[uri]` URI of the property to delete in the ontology

  ##### Usage

  ```
  (ontology/delete
    (ontology/delete-property \"http://purl.org/ontology/bibo/author\"))
  ```"       
  [uri]
  {:function "deleteProperty"
   :parameters (str "uri=" (url/url-encode uri))})

(defn delete-class
  "Delete a class from an ontology on the system
  
  ##### Parameters
  
  * `[uri]` URI of the class to delete in the ontology

  ##### Usage

  ```
  (ontology/delete
    (ontology/delete-class \"http://purl.org/ontology/bibo/Issue\"))
  ```"       
  [uri]
  {:function "deleteClass"
   :parameters (str "uri=" (url/url-encode uri))})

(defn delete-named-individual
  "Delete a named individual from an ontology on the system
  
  ##### Parameters
  
  * `[uri]` URI of the named individual to delete in the ontology

  ##### Usage

  ```
  (ontology/delete
    (ontology/delete-named-individual \"http://purl.org/ontology/bibo/status/unpublished\"))
  ```"       
  [uri]
  {:function "deleteNamedIndividual"
   :parameters (str "uri=" (url/url-encode uri))})

(defn delete
  "Ontology: Delete query.

  **Required**

  ##### Usage

  ```
  ;; Delete a property
  (ontology-d/delete
    (ontology-d/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-d/delete-property \"http://purl.org/ontology/bibo/annotates\"))

  ;; Delete the ontology
  (ontology-d/delete
    (ontology-d/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-d/delete-ontology))
  ```"  
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/ontology/delete/" params)))
