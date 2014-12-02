(ns clj-osf.sparql
  "Send a SPARQL Query to a OSF SPARQL web service endpoint
   
  The SPARQL Web service is used to send custom SPARQL queries against the 
  OSF data structure. This is a general purpose querying Web service.
   
  To use the SPARQL code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.sparql :as sparql])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/SPARQL#Web_Service_Endpoint_Information)"  
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn dataset
  "URI of the dataset to query. Only use this function when you don't have
  FROM NAMED clauses in your SPARQL query.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uri]` The dataset URI where the record is indexed.

  ##### Usage

  ```
  (sparql/sparql
    (sparql/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"        
  [uri]
  {:dataset uri})

(defn default-graph-uri
  "Specify the URI of the default graph to use for this SPARQL query.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uri]` URI of the default graph

  ##### Usage

  ```
  (sparql/sparql
    (sparql/default-graph-uri \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"        
  [uri]
  {:default-graph-uri uri})

(defn named-graph-uri
  "Specify the URI of the named graph to use for this SPARQL query.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uri]` URI of the named graph

  ##### Usage

  ```
  (sparql/sparql
    (sparql/named-graph-uri \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"        
  [uri]
  {:named-graph-uri uri})

(defn query
  "SPARQL query to send to the endpoint.

  The usage of this function is **Required**
  
  ##### Parameters
  
  * `[q]` SPARQL query to send to the endpoint

  ##### Usage

  ```
  (sparql/sparql
    (sparql/named-graph-uri \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"
  [q]
  {:query q})

(defn sparql
  "SPARQL query.

  **Required**

  ##### Usage

  ```
  (sparql/sparql
    (sparql/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (sparql/query \"select * where {?s ?p ?o}\"))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (query "")
                       (dataset "")
                       (default-graph-uri "")
                       (named-graph-uri "")
                       (core/->mime "application/sparql-results+json")
                       (core/->post))
        params (merge default params)]
    (core/osf-query "/ws/sparql/" params)))
