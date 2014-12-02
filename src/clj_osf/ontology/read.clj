(ns clj-osf.ontology.read
  "Send an Ontology Read Query to a OSF Ontology Read web service endpoint
   
  The Ontology Read service is used to query OWL ontologies. All kinds of information 
  can be read on different ontology entities such as: classes, object properties, 
  datatype properties, annotation properties and individuals. Reasoners can also be 
  used to include inferred facts in the service's resultset. A full list of actions 
  can be performed that enables you to leverage your ontologies, properly and 
  effectively.
   
  This service is a Web service wrapper over the OWLAPI ontology library. Most of the 
  API has been implemented. So we can say that this Web service (with the other related 
  OSF services) turns the OWLAPI into a Web service API. 
   
  To use the Ontology: Read code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.ontology.read :as ontology-r])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Ontology_Read#Web_Service_Endpoint_Information)"    
  (:refer-clojure :exclude [read])  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]
            [cemerick.url :as url]))

(defn get-class
  "Get the description of a class, in a target ontology. 
  
  ##### Parameters
  
  * `[uri]` URI of the class to get its description from the ontology

  ##### Usage

  ```
  ;; Get the description of the bibo:Book class
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-class \"http://purl.org/ontology/bibo/Book\"))
  ```"
  [uri]  
  {:function "getClass"
   :parameters (str "uri=" (url/url-encode uri))})

(defn get-classes
  "Get all the classes that have been defined in an ontology. The requester can get 
  a list of URIs or the full description of the classes.
  
  ##### Parameters
  
  * `[limit]` *(optional)*: The number of results the requester wants in the resultset.
  * `[offset]` *(optional)*: Where the results to return starts in the complete list of
                             results. This is normally used in conjunction with the limit
                             parameter to paginate the complete list of classes. 
  * `[get-class-uris]` *(optional)*: Get a list of URIs that refers to the classes described
                                     in this ontology. true or false. The default is true.
  * `[get-classes-descriptions]` *(optional)*: Get the list of classes description for the
                                               classes described in this ontology. true or
                                               flase. The default is false.

  ##### Usage

  ```
  ;; Get all the classes of the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-classes :get-classes-descriptions true))
  ```"
  [& {:keys [limit offset get-classes-uris get-classes-descriptions]
      :or {limit nil
           offset nil
           get-classes-uris true
           get-classes-descriptions false}}]  
  {:function "getClasses"
   :parameters (str (when limit
                      "limit=" limit ";")
                    (when offset
                      "offset=" offset ";")
                    (if get-classes-descriptions
                      "mode=descriptions"
                      "mode=uris"))})

(defn get-disjoint-classes
  "Get all the disjoint-classes of a target class of the ontology. The requester can get 
  a list of URIs or the full description of the disjoint-classes.
  
  ##### Parameters
  
  * `[uri]`: URI of the class for which the requester want its disjoint-classes. 
  * `[get-class-uris]` *(optional)*: Get a list of URIs that refers to the disjoint-classes described
                                     in this ontology. true or false. The default is true.
  * `[get-classes-descriptions]` *(optional)*: Get the list of classes description for the
                                               disjoint-classes described in this ontology. true or
                                               flase. The default is false.

  ##### Usage

  ```
  ;; Get all the classes of the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-disjoint-classes \"http://purl.org/ontology/bibo/Book\"
                                     :get-classes-descriptions true))
  ```"  
  [uri & {:keys [get-classes-uris get-classes-descriptions]
          :or {get-classes-uris true
               get-classes-descriptions false}}]  
  {:function "getDisjointClasses"
   :parameters (str "uri=" (url/url-encode uri) ";"                
                    (if get-classes-descriptions
                      "mode=descriptions"
                      "mode=uris"))})

(defn get-disjoint-properties
  "Get all the disjoint-properties that have been defined in an ontology for a
  target property. The requester can get a list of URIs or the full
  description of the disjoint-properties.
  
  ##### Parameters
  
  * `[uri]`: URI of the property for which the requester want its disjoint-properties. 
  * `[get-datatype-properties]` *(optional)*: Get all the Datatype disjoint-properties
                                              of the ontology. true or false. Default is
                                              true.
  * `[get-object-properties]` *(optional)*: Get all the Object disjoint-properties
                                            of the ontology. true or false. Default is
                                            false.
  * `[get-properties-uris]` *(optional)*: Get a list of URIs that refers to the properties
                                          described in this ontology. true or false. Default is
                                          true.
  * `[get-properties-descriptions]` *(optional)*: Get the list of properties description for
                                                  the properties described in this ontology.
                                                  true or false. Default is true.

  ##### Usage

  ```
  ;; Get all the disjoint properties of the bibo:abstract property
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-disjoint-properties \"http://purl.org/ontology/bibo/abstract\"
                                        :get-properties-descriptions true))
  ```"    
  [uri & {:keys [get-datatype-properties
                 get-object-properties
                 get-properties-uris
                 get-properties-descriptions]
          :or {get-datatype-properties true
               get-object-properties false
               get-properties-uris true
               get-properties-descriptions false}}]  
  {:function "getDisjointProperties"
   :parameters (str "uri=" (url/url-encode uri) ";"
                    (if get-properties-descriptions
                      "mode=descriptions;"
                      "mode=uris;")
                    (if get-object-properties
                      "type=objectproperty"
                      "type=dataproperty"))})

(defn get-equivalent-classes
  "Get all the equivalent-classes of a target class of the ontology. The requester can get 
  a list of URIs or the full description of the equivalent-classes.
  
  ##### Parameters
  
  * `[uri]`: URI of the class for which the requester want its equivalent-classes. 
  * `[get-class-uris]` *(optional)*: Get a list of URIs that refers to the equivalent-classes described
                                     in this ontology. true or false. The default is true.
  * `[get-classes-descriptions]` *(optional)*: Get the list of classes description for the
                                               equivalent-classes described in this ontology. true or
                                               flase. The default is false.

  ##### Usage

  ```
  ;; Get all the classes of the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-equivalent-classes \"http://purl.org/ontology/bibo/Book\"
                                       :get-classes-descriptions true))
  ```"
  [uri & {:keys [get-classes-uris get-classes-descriptions]
          :or {get-classes-uris true
               get-classes-descriptions false}}]  
  {:function "getEquivalentClasses"
   :parameters (str "uri=" (url/url-encode uri) ";"                
                    (if get-classes-descriptions
                      "mode=descriptions"
                      "mode=uris"))})

(defn get-equivalent-properties
  "Get all the equivalent-properties that have been defined in an ontology for a
  target property. The requester can get a list of URIs or the full
  description of the equivalent-properties.
  
  ##### Parameters
  
  * `[uri]`: URI of the property for which the requester want its equivalent-properties. 
  * `[get-datatype-properties]` *(optional)*: Get all the Datatype equivalent-properties
                                              of the ontology. true or false. Default is
                                              true.
  * `[get-object-properties]` *(optional)*: Get all the Object equivalent-properties
                                            of the ontology. true or false. Default is
                                            false.
  * `[get-properties-uris]` *(optional)*: Get a list of URIs that refers to the properties
                                          described in this ontology. true or false. Default is
                                          true.
  * `[get-properties-descriptions]` *(optional)*: Get the list of properties description for
                                                  the properties described in this ontology.
                                                  true or false. Default is true.

  ##### Usage

  ```
  ;; Get all the disjoint properties of the bibo:abstract property
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-equivalent-properties \"http://purl.org/ontology/bibo/abstract\"
                                          :get-properties-descriptions true))
  ```"    
  [uri & {:keys [get-datatype-properties
                 get-object-properties
                 get-properties-uris
                 get-properties-descriptions]
          :or {get-datatype-properties true
               get-object-properties false
               get-properties-uris true
               get-properties-descriptions false}}]  
  {:function "getEquivalentProperties"
   :parameters (str "uri=" (url/url-encode uri) ";"
                    (if get-properties-descriptions
                      "mode=descriptions;"
                      "mode=uris;")
                    (if get-object-properties
                      "type=objectproperty"
                      "type=dataproperty"))})

(defn get-iron-json-schema
  "Get the ironJSON Schema, used by the Semantic Components, that represents
  the OWL ontology.
  
  ##### Usage

  ```
  ;; Get the ironSchema for the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-iron-json-schema))
  ```"       
  []  
  {:function "getIronJsonSchema"})

(defn get-iron-xml-schema
  "Get the ironXML Schema, used by the Semantic Components, that represents
  the OWL ontology.
  
  ##### Usage

  ```
  ;; Get the ironXML Schema for the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-iron-xml-schema))
  ```"
  []  
  {:function "getIronXMLSchema"})

(defn get-serialized
  "Get the serialized document that represents the OWL ontology. The serialization 
  format (usually RDF+XML or RDF+N3) depends on the format used when the ontology 
  got created.
  
  ##### Usage

  ```
  ;; Get the a RDF serialization representation of the ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-serialized))
  ```"
  []  
  {:function "getSerialized"})

(defn get-loaded-ontologies
  "Get the list of all loaded ontologies on the OSF network instance.
  
  ##### Parameters
  
  * `[get-loaded-ontologies-uris]` *(optional)*: Get the URIs of the loaded ontologies.
                                                 true or false. Default is true.
  * `[get-loaded-ontologies-descriptions]` *(optional)*:  Get the description of the loaded ontologies.
                                                          true or false. Default is false.

  ##### Usage

  ```
  ;; Get all the disjoint properties of the bibo:abstract property
  (ontology-r/read
    (ontology-r/get-loaded-ontologies))
  ```"      
  [& {:keys [get-loaded-ontologies-uris get-loaded-ontologies-descriptions]
      :or {get-loaded-ontologies-uris true
           get-loaded-ontologies-descriptions false}}]  
  {:function "getLoadedOntologies"
   :parameters (str (if get-loaded-ontologies-descriptions
                      "mode=descriptions"
                      "mode=uris"))})

(defn get-named-individual
 "Get the description of a named individual, in a target ontology.
  
  ##### Parameters
  
  * `[uri]` URI of the named individual to get its description from the ontology

  ##### Usage

  ```
  ;; Get the description of the bibo:unpublished named individual
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-named-individual \"http://purl.org/ontology/bibo/status/unpublished\"))
  ```"
  [uri]  
  {:function "getNamedIndividual"
   :parameters (str "uri=" (url/url-encode uri))})

(defn get-named-individuals
  "Get all the named individuals that have been defined in an ontology. The requester can get 
  a list of URIs or the full description of the named individuals. 
  
  ##### Parameters
  
  * `[limit]` *(optional)*: The number of results the requester wants in the resultset.
  * `[offset]` *(optional)*: Where the results to return starts in the complete list of
                             results. This is normally used in conjunction with the limit
                             parameter to paginate the complete list of classes. 
  * `[get-named-individuals-uris]` *(optional)*: Get a list of URIs that refers to the named individuals described
                                                 in this ontology. true or false. The default is true.
  * `[get-named-individuals-descriptions]` *(optional)*: Get the list of named individuals description for the
                                                         named individuals described in this ontology. true or
                                                         false. The default is false.
  * `[get-named-individuals-list]` *(optional)*: Get the list of named individuals description described
                                                 in this ontology. This list of named individuals has
                                                 been optimized for list controls. Only the types and the
                                                 prefLabel of the named individual has been added to its
                                                 description. true or false. The default is false.
  * `[direct-named-individuals]` *(optional)*: Get all the named individuals that belong to the class
                                               referenced by the `class-uri` parameter, and all the named
                                               individuals that belongs to all the super-classes of
                                               that target class. true or false. The default is false.
  * `[all-named-individuals]` *(optional)*: Get all the named individuals that belong directly to 
                                            that class referenced by the classuri parameter.
                                            true or false. The default is false.
  * `[class-uri]` *(optional)*: Get all the named individuals belonging to that class URI.  

  ##### Usage

  ```
  ;; Get all the named individuals of the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-named-individuals))
  ```"
  [& {:keys [limit
             offset
             get-named-individuals-uris
             get-named-individuals-descriptions
             get-named-individuals-list
             direct-named-individuals
             all-named-individuals
             class-uri]
      :or {limit nil
           offset nil
           class-uri nil
           get-named-individuals-uris true
           get-named-individuals-descriptions false
           get-named-individuals-list false
           all-named-individuals true
           direct-named-individuals false}}]  
  {:function "getNamedIndividuals"
   :parameters (str (if all-named-individuals
                      "direct=False;"
                      "direct=True;")                    
                    (when limit
                      "limit=" limit ";")
                    (when offset
                      "offset=" offset ";")
                    (when class-uri
                      (str "classuri=" (url/url-encode class-uri)))
                    (if get-named-individuals-list
                      "mode=list"
                      (if get-named-individuals-descriptions
                        "mode=descriptions"
                        "mode=uris")))})

(defn get-properties
  "Get all the properties that have been defined in an ontology. The requester can 
  get a list of URIs or the full description of the properties.
  
  ##### Parameters
  
  * `[limit]` *(optional)*: The number of results the requester wants in the resultset.
  * `[offset]` *(optional)*: Where the results to return starts in the complete list of
                             results. This is normally used in conjunction with the limit
                             parameter to paginate the complete list of classes. 
  * `[get-properties-uris]` *(optional)*: Get a list of URIs that refers to the properties described
                                          in this ontology. true or false. The default is true.
  * `[get-properties-descriptions]` *(optional)*: Get the list of properties description for the
                                                  named individuals described in this ontology. true or
                                                  false. The default is false.
  * `[get-all-properties]` *(optional)*: Get all the Datatype, Object and Annotation properties
                                         of the ontology. true or false. The default is true.
  * `[get-datatype-properties]` *(optional)*: Get all the Datatype properties of the ontology.
                                              true or false. The default is false.
  * `[get-object-properties]` *(optional)*: Get all the Datatype properties of the ontology.
                                            true or false. The default is false.
  * `[get-annotation-properties]` *(optional)*: Get all the Datatype properties of the ontology.
                                                true or false. The default is false.

  ##### Usage

  ```
  ;; Get all the named individuals of the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-named-individuals))
  ```"
  [& {:keys [limit
             offset
             get-all-properties
             get-annotation-properties
             get-datatype-properties
             get-object-properties
             get-properties-uris
             get-properties-descriptions]
      :or {limit nil
           offset nil
           get-all-properties true
           get-annotation-properties false
           get-datatype-properties false
           get-object-properties false
           get-properties-uris true
           get-properties-descriptions false}}]  
  {:function "getProperties"
   :parameters (str (when limit
                      "limit=" limit ";")
                    (when offset
                      "offset=" offset ";")
                    (if get-annotation-properties
                      "type=annotationproperty;"
                      (if get-datatype-properties
                        "type=dataproperty;"
                        (if get-object-properties
                          "type=objectproperty;"
                          "type=all;")))
                    (if get-properties-descriptions
                      "mode=descriptions"
                      "mode=uris"))})

(defn get-ontologies
  "Get the list of all the ontologies of the import closure of the ontology being 
  queried. If you want to get the list of all individually loaded ontologies file 
  of this instance, use the getLoadedOntologies API call instead.

  ##### Usage

  ```
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-ontologies))  
  ```"
  []  
  {:function "getOntologies"
   :parameters "mode=uris"})

(defn get-property
  "Get the description of a property, in a target ontology. 
  
  ##### Parameters
  
  * `[uri]` URI of the property to get its description from the ontology

  ##### Usage

  ```
  ;; Get the description of the bibo:abstract property
  (ontology-r/read                        
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-property \"http://purl.org/ontology/bibo/abstract\"))
  ```"
  [uri]  
  {:function "getProperty"
   :parameters (str "uri=" (url/url-encode uri))})

(defn get-sub-classes
  "Get all the sub-classes of a target class of the ontology. The requester can 
  get a list of URIs or the full description of the sub-classes.
  
  ##### Parameters
  
  * `[get-classes-uris]` *(optional)*: Get a list of URIs that refers to the sub-classes
                                       described in this ontology. true or false.
                                       The default is true.
  * `[get-classes-descriptions]` *(optional)*: the list of classes description for the
                                               sub-classes described in this ontology. true
                                               or false. The default is true.
  * `[get-classes-hierarchy]` *(optional)*: Get the list of classes description for the sub-classes
                                            described in this ontology. The class description being
                                            returned is a lightweight version of the full 
                                            descriptions mode. The goal is to manipulate and transmit
                                            a simpler structure such as what might be used by a user
                                            interface to display some parts of the hierarchy of an
                                            ontology. What is returned is all the annotation properties
                                            (used to get some label to display for one of the sub-class)
                                            and a possible attribute: sco:hasSubClass which has true as
                                            value. If this triple exists, it means that the sub-class has 
                                            itself other subclasses (this is mainly used to be able to
                                            display an extend button in a tree control).true or false.
                                            The default is true.
  * `[get-direct-subclasses]` *(optional)*: Only get the direct sub-classes of the target class.
                                            true or false. The default is true.
  * `[get-all-subclasses]` *(optional)*: Get all the sub-classes by inference (so, the sub-classes
                                         of the sub-classes recursively).true or false. The default
                                         is true.

  ##### Usage

  ```
  ;; Get the sub classes of the bibo:Book class
  (ontology-r/read                        
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-sub-classes \"http://purl.org/ontology/bibo/Book\"))  
  ```"  
  [uri & {:keys [get-classes-uris
                 get-classes-descriptions
                 get-classes-hierarchy
                 get-direct-subclasses
                 get-all-subclasses]
          :or {get-classes-uris true
               get-classes-descriptions false
               get-classes-hierarchy false
               get-direct-subclasses true
               get-all-subclasses false}}]  
  {:function "getSubClasses"
   :parameters (str "uri=" (url/url-encode uri) ";"
                    (if get-all-subclasses
                      "direct=False;"
                      "direct=True;")
                    (if get-classes-descriptions
                      "mode=descriptions"
                      (if get-classes-hierarchy
                        "mode=hierarchy"
                        "mode=uris")))})

(defn get-sub-properties
  "Get all the sub-properties of a target property of the ontology. The requester can 
  get a list of URIs or the full description of the sub-properties.
  
  ##### Parameters
  
  * `[get-properties-uris]` *(optional)*: Get a list of URIs that refers to the sub-properties
                                          described in this ontology. true or false.
                                          The default is true.
  * `[get-properties-descriptions]` *(optional)*: the list of classes description for the
                                               sub-properties described in this ontology. true
                                               or false. The default is true.
  * `[get-direct-subproperties]` *(optional)*: Only get the direct sub-properties of the target class.
                                               true or false. The default is true.
  * `[get-all-subproperties]` *(optional)*: Get all the sub-properties by inference (so, the sub-properties
                                            of the sub-properties recursively).true or false. The default
                                            is true.
  * `[get-datatype-properties]` *(optional)*: Get all the Datatype properties of the ontology.
                                              true or false. The default is false.
  * `[get-object-properties]` *(optional)*: Get all the Datatype properties of the ontology.
                                            true or false. The default is false.

  ##### Usage

  ```
  ;; Get the sub classes of the bibo:Book class
  (ontology-r/read                        
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-sub-classes \"http://purl.org/ontology/bibo/Book\"))  
  ```"  
  [uri & {:keys [get-properties-uris
                 get-properties-descriptions
                 get-direct-subproperties
                 get-all-subproperties
                 get-datatypeproperties
                 get-objectproperties]
          :or {get-classes-uris true
               get-classes-descriptions false
               get-direct-subproperties true
               get-all-subproperties false
               get-datatypeproperties true
               get-objectproperties false}}]  
  {:function "getSubProperties"
   :parameters (str "uri=" (url/url-encode uri) ";"
                    (if get-all-subproperties
                      "direct=False;"
                      "direct=True;")
                    (if get-objectproperties
                      "type=objectproperty;"
                      "type=datatypeproperty;")
                    (if get-properties-descriptions
                      "mode=descriptions"
                      "mode=uris"))})

(defn get-super-classes
  "Get all the super-classes of a target class of the ontology. The requester can 
  get a list of URIs or the full description of the super-classes.
  
  ##### Parameters
  
  * `[get-classes-uris]` *(optional)*: Get a list of URIs that refers to the super-classes
                                       described in this ontology. true or false.
                                       The default is true.
  * `[get-classes-descriptions]` *(optional)*:  the list of classes description for the
                                                super-classes described in this ontology. true
                                                or false. The default is true.
  * `[get-direct-superclasses]` *(optional)*: Only get the direct super-classes of the target class.
                                              true or false. The default is true.
  * `[get-all-superclasses]` *(optional)*: Get all the super-classes by inference (so, the
                                           sub-classes of the super-classes recursively).
                                           true or false. The default is true.

  ##### Usage

  ```
  ;; Get the super classes of the bibo:Book class
  (ontology-r/read                        
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-super-classes \"http://purl.org/ontology/bibo/Book\"))
  ```"  
  [uri & {:keys [get-classes-uris
                 get-classes-descriptions
                 get-direct-superclasses
                 get-all-superclasses]
          :or {get-classes-uris true
               get-classes-descriptions false
               get-direct-superclasses true
               get-all-superclasses false}}]  
  {:function "getSuperClasses"
   :parameters (str "uri=" (url/url-encode uri) ";"
                    (if get-all-superclasses
                      "direct=False;"
                      "direct=True;")
                    (if get-classes-descriptions
                      "mode=descriptions"
                      "mode=uris"))})

(defn get-super-properties
  [uri & {:keys [get-properties-uris
                 get-properties-descriptions
                 get-direct-superproperties
                 get-all-superproperties
                 get-datatypeproperties
                 get-objectproperties]
          :or {get-classes-uris true
               get-classes-descriptions false
               get-direct-superproperties true
               get-all-superproperties false
               get-datatypeproperties true
               get-objectproperties false}}]  
  {:function "getSuperProperties"
   :parameters (str "uri=" (url/url-encode uri) ";"
                    (if get-all-superproperties
                      "direct=False;"
                      "direct=True;")
                    (if get-objectproperties
                      "type=objectproperty;"
                      "type=datatypeproperty;")
                    (if get-properties-descriptions
                      "mode=descriptions"
                      "mode=uris"))})

(defn disable-reasoner
  "Disable the reasoner for querying this ontology
  
  ##### Usage

  ```
  (ontology/read
    (ontology/disable-reasoner)
  ```"     
  []
  {:reasoner "False"})

(defn enable-reasoner
  "Enable the reasoner for querying this ontology

  The usage of this function is the **default behavior**  
  
  ##### Usage

  ```
  (ontology/read
    (ontology/enable-reasoner)
  ```"     
  []
  {:reasoner "True"})

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
  (ontology/read
    (ontology/ontology \"http://purl.org/ontology/bibo/\")
  ```" 
  [uri]
  {:ontology uri})

(defn read
  "Ontology: Read query.

  **Required**

  ##### Usage

  ```
  ;; Get all loaded ontologies of the OSF instance
  (ontology-r/read
    (ontology-r/get-loaded-ontologies))

  ;; Get all the classes of the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-classes :get-classes-descriptions true))

  ;; Get the description of the bibo:Book class
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-class \"http://purl.org/ontology/bibo/Book\"))

  ;; Get all the properties of the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-properties))

  ;; Get the ironSchema for the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-iron-json-schema))

  ;; Get the a RDF serialization representation of the ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-serialized))

  ;; Get all the named individuals of the BIBO ontology
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-named-individuals))

  ;; Get the description of the bibo:unpublished named individual
  (ontology-r/read
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-named-individual \"http://purl.org/ontology/bibo/status/unpublished\"))

  ;; Get all the ontologies loaded by a given ontology
  (ontology-r/read                        
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-ontologies))

  ;; Get the description of the bibo:abstract property
  (ontology-r/read                        
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-property \"http://purl.org/ontology/bibo/abstract\"))

  ;; Get the super classes of the bibo:Book class
  (ontology-r/read                        
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-super-classes \"http://purl.org/ontology/bibo/Book\"))

  ;; Get the sub classes of the bibo:Book class
  (ontology-r/read                        
    (ontology-r/ontology \"http://purl.org/ontology/bibo/\")
    (ontology-r/get-sub-classes \"http://purl.org/ontology/bibo/Book\"))  
  ```"      
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (enable-reasoner)
                       (ontology "")
                       (core/->post)
                       (core/->mime (if (= (params :function) "getSerialized")
                                      "text/xml"
                                      "application/json")))
        params (merge default params)]
    (core/osf-query "/ws/ontology/read/" params)))
