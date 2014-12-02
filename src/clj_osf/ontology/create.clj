(ns clj-osf.ontology.create
  "Send a Ontology Create service is used to create/import a new OWL ontology into the 
  OSF instance.
  
  This service is a web service wrapper over the OWLAPI ontology library. It wraps 
  all the needed functionalities related to ontology creation/import. Most of the 
  relatd API has been implemented. So we can say that web service (with the other 
  related services) turns the OWLAPI into a web service API. 
  
  This Web service is intended to be used by content management systems, developers 
  or administrators to create ontologies that are hosted on a OSF instance, 
  and that are used to describe the named entities in the system.
  
  This endpoint, along with the other related endpoints: Ontology Read, Ontology 
  Update and Ontology Delete; can be seen as the brain of your OSF instance. 
   
  To use the Ontology: Create code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.ontology.create :as ontology-c])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Ontology_Create#Web_Service_Endpoint_Information)"    
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn uri
  "Specifies the URI of the ontology.
   
  *Note:* you can get the list of all loaded ontologies by using the
          `(osf-clj.ontology.read/get-loaded-ontologies)` function

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[uri]` Specifies the URI of the ontology; the URI is the URL used to import that 
            ontology in the system. The URL can be the URI of the ontology if it was 
            resolvable on the Web, or the URL where the OWL file, containing the 
            ontology's description, that can be resolved on the Web by this endpoint. 
            This URL can refers to a file accessible on the web, on the file system, 
            etc. The endpoint will get the ontology's description from that URL.

  ##### Usage

  ```
  (ontology/create
    (ontology/uri \"http://purl.org/ontology/bibo/\"))
  ```"     
  [uri]
  {:uri uri})

(defn enable-reasoner
  "Enable the reasoner for indexing the ontology into OSF (the triple 
  store and the full text engine) 
   
  This is the default behavior of this service.

  ##### Usage

  ```
  (ontology/create
    (ontology/enable-reasoner)
  ```"
  []
  {:reasoner "True"})

(defn disable-reasoner
  "Disable the reasoner for for indexing the ontology into OSF (the triple 
  store and the full text engine)

  ##### Usage

  ```
  (ontology/create
    (ontology/disable-reasoner)
  ```"
  []
  {:reasoner "False"})

(defn enable-advanced-indexation
  "Enable advanced indexation of the ontology. This means that the ontology's description 
  (so all the classes, properties and named individuals) will be indexed in the other 
  data management system in OSF. This means that all the information in these 
  ontologies will be accessible via the other endpoints such as the Search and the SPARQL 
  web service endpoints. Enabling this option may render the creation process slower 
  depending on the size of the created ontology.

  ##### Usage

  ```
  (ontology/create
    (ontology/enable-advanced-indexation)
  ```"  
  []
  {:advancedIndexation "True"})

(defn disable-advanced-indexation
  "Disable advanced indexation of the ontology. This means that the ontologies will be queriable 
  via the Ontology Read, Ontology Update and Ontology Delete web service endpoints only
   
  This is the default behavior of this service.

  ##### Usage

  ```
  (ontology/create
    (ontology/disable-advanced-indexation)
  ```"
  []
  {:advancedIndexation "False"})

(defn create
  "Ontology: Create query.

  **Required**

  ##### Usage

  ```
  ;; Create/import an ontology from a dereferencable URI on the Web
  (ontology-c/create
    (ontology-c/enable-advanced-indexation)
    (ontology-c/enable-reasoner)
      (ontology-c/uri \"http://purl.org/ontology/bibo/\"))
  ```"  
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/ontology/create/" params)))
