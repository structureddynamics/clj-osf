(ns clj-osf.ontology.update
  "Send an Ontology Update service is used to update an OWL ontology existing in the 
  OSF instance.
   
  This service is a web service wrapper over the OWLAPI ontology library. It wraps 
  all the needed functionalities related to updating an ontology. Most of the 
  related API has been implemented. So we can say that web service (with the other 
  related services) turns the OWLAPI into a web service API.
   
  There is an important semantic distinction to do: this endpoint is about UPDATING 
  an ontology. This means that we may be updating ontologies resources, or creating 
  new ones. The logic is that by creating new resources (such as classes, properties 
  and named individuals) we are updating the ontology.
   
  This is what this web service endpoint is about. To update or create an existing 
  resource, the requester only has to send the RDF description of that resource to 
  update or create. If the resource is existing, it will get updated, if it is not, 
  it will get added.
   
  To use the Ontology: Update code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.ontology.update :as ontology-u])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Ontology_Update#Web_Service_Endpoint_Information)"    
  (:require [clj-osf.core :as core]
            [clojure.string :as string]
            [cemerick.url :as url]))

(defn create-or-update-entity
  "Create or update an entity in the ontology. An entity can be anything of a class, 
  an object property, a datatype property, an annotation property or a named individual.
  
  Right now, the creation and the modification of an entity is simplified to writing the 
  RDF triples describing the entity to create or update. The advantage of this method is 
  that a system that interacts with the endpoints doesn't have to send multiple different 
  queries to change multiple aspects of an entity. It only has to generate the code offline, 
  and send it once to the Ontology Update web service endpoint, with the complete RDF 
  description of the entity to update, or create, in the instance.
  
  If an entity is being modified by some system, usually the workflow process is:
  * The system send a query to the Ontology Read web service endpoint to get the 
    current complete description of the entity to update
  * The system modify the RDF description of that entity, the way it has to (by 
    adding, removing or modifying triples)
  * Once the modifications are done offline, the system send the resulting RDF 
    document to the Ontology Update web service endpoint
  
  ##### Parameters
  
  * `[document]` A version of the RDF document describing the entity (class, property or 
                 named individual) to create or update in the ontology.
  * `[enable-advanced-indexation]` *(optional)* Enable advanced indexation of the ontology.
                This means that the ontology's description (so all the classes, properties
                and named individuals) will be indexed in the other data management system
                in OSF. This means that all the information in these ontologies will be
                accessible via the other endpoints such as the Search and the SPARQL web
                service endpoints. Enabling this option may render the creation process slower 
                depending on the size of the created ontology.

  ##### Usage

  ```
  ;; Create a new class
  (turtle/defns iron \"http://purl.org/ontology/iron#\")
  (turtle/defns bibo \"http://purl.org/ontology/bibo/\")
  (turtle/defns owl \"http://www.w3.org/2002/07/owl#\")

  (ontology/update
    (ontology/ontology \"http://purl.org/ontology/bibo/\")
    (ontology/create-or-update-entity
      (turtle/rdf
        (bibo :new-property) (turtle/a) (owl :ObjectProperty)
        (bibo :new-property) (iron :prefLabel) (turtle/literal \"Some New Property\")))) 
  ```"   
  [document & {:keys [enable-advanced-indexation]
               :or {enable-advanced-indexation true}}]  
  {:function "createOrUpdateentity"
   :parameters (str "document=" (url/url-encode document)
                    ";advancedIndexation=" (if enable-advanced-indexation
                                             "True"
                                             "False"))})
(defn update-entity-uri
  "Update (refactor) the URI of an existing entity. The entity can be a 
  class, an object property, a datatype property, an annotation property 
  or a named individual. 
  
  ##### Parameters
  
  * `[old-uri]` This is the current URI of the entity to update. This URI will be replaced 
                by the newuri. After this query, that current URI won't be available anymore.
  * `[new-uri]` This is the new URI to define for the entity. This URI is replacing the 
                olduri. After this query, this is the URI that will be referring to this entity.
  * `[enable-advanced-indexation]` *(optional)* Enable advanced indexation of the ontology.
                This means that the ontology's description (so all the classes, properties
                and named individuals) will be indexed in the other data management system
                in OSF. This means that all the information in these ontologies will be
                accessible via the other endpoints such as the Search and the SPARQL web
                service endpoints. Enabling this option may render the creation process slower 
                depending on the size of the created ontology.

  ##### Usage

  ```
  ;; Update the URI of an existing entity
  (ontology/update
    (ontology/ontology \"http://purl.org/ontology/bibo/\")
    (ontology/update-entity-uri
    \"http://purl.org/ontology/bibo/new-property\" \"http://purl.org/ontology/bibo/update-uri\"))  
  ```"
  [old-uri new-uri & {:keys [enable-advanced-indexation]
                      :or {enable-advanced-indexation true}}]  
  {:function "updateEntityUri"
   :parameters (str "olduri=" (url/url-encode old-uri)
                    ";newuri=" (url/url-encode new-uri)
                    ";advancedIndexation=" (if enable-advanced-indexation
                                             "True"
                                             "False"))})

(defn disable-reasoner
  "Disable the reasoner for for indexing the ontology into OSF (the triple 
  store and the full text engine)
  
  ##### Usage

  ```
  (ontology/update
    (ontology/disable-reasoner)
  ```"     
  []
  {:reasoner "False"})

(defn enable-reasoner
  "Enable the reasoner for indexing the ontology into OSF (the triple 
  store and the full text engine) 
   
  This is the default behavior of this service.
   
  *Note:* This only has an effect if the advanced indexation is enabled
  
  ##### Usage

  ```
  (ontology/update
    (ontology/enable-reasoner)
  ```"     
  []
  {:reasoner "True"})

(defn save-ontology
  "When the `(craete-or-update-entity)`, or the `(update-entity-uri)`, functions
  are called via this web service endpoint, the ontology is tagged with a
  `wsf:ontologyModified` \"true\" triple that specifies that the ontology got
  modified in some ways.
  
  What this `(save-ontology)` call does, is only to remove that tag (so, to remove 
  the tag that says that the ontology got modified).
  
  The ontology is after saved, and persisted, in the OWLAPI instance. However, 
  if you want your system to save a hard-copy of an ontology that got modified 
  (to reload it elsewhere), the you system will have to perform these steps:
  
  Check if the ontology got modified by checking if the `wsf:OntologyModified`
  \"true\" triple appears in the ontology's description after a call to the 
  Ontology Read web service endpoint for the function `(get-ontology)`: mode=description.
  
  If is has been modified, then it calls the Ontology Read endpoints again, but 
  using the getSerialized function. Once it gets the serialization form of the 
  ontology, the system can now save it on its local file system, or elsewhere.
  
  Finally it calls the saveOntology function of the Ontology Update web service 
  endpoint to mark the ontology as saved.
  
  ##### Usage

  ```
  (ontology/update
    (ontology/save-ontology)
  ```"   
  []
  {:function "saveOntology"})

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
  (ontology/update
    (ontology/ontology \"http://purl.org/ontology/bibo/\")
  ```"    
  [uri]
  {:ontology uri})

(defn update
  "Ontology: Update query.

  **Required**

  ##### Usage

  ```
  ;; Create a new class
  (turtle/defns iron \"http://purl.org/ontology/iron#\")
  (turtle/defns bibo \"http://purl.org/ontology/bibo/\")
  (turtle/defns owl \"http://www.w3.org/2002/07/owl#\")

  (ontology-u/update
    (ontology-u/ontology \"http://purl.org/ontology/bibo/\")
  
  (ontology-u/create-or-update-entity
    (turtle/rdf
     (bibo :new-property) (turtle/a) (owl :ObjectProperty)
     (bibo :new-property) (iron :prefLabel) (turtle/literal \"Some New Property\"))))

  ;; Update the URI of an existing entity
  (ontology-u/update
    (ontology-u/ontology \"http://purl.org/ontology/bibo/\")
      (ontology-u/update-entity-uri
        \"http://purl.org/ontology/bibo/new-property\" \"http://purl.org/ontology/bibo/update-uri\"))
  ```"    
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/ontology/update/" params)))
