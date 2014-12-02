(ns clj-osf.crud.read
  "Send a CRUD Read query to a OSF Crud Read web service endpoint
   
  The CRUD: Read Web service is used to get the description of 
  a target instance record indexed in a dataset belonging to an
  Open Semantic Framework instance.
  
  To use the CRUD Read code, you have to:

  ```
  ;; Use/require the namespace
  (require '[clj-osf.crud.read :as crud])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/CRUD:_Read#Web_Service_Endpoint_Information)"
  (:refer-clojure :exclude [read])
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn dataset
  "Set the URI(s) of the dataset where the instance record is indexed.
 
  If this parameter is omitted (empty), the web service will query all the datasets of the 
  system, that can be read by the requester, to try to find a definition for this record URI.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris]` A single URI string, or a vector of URI strings that refers to the
  datasets where the requested record URIs are indexed. If we have set
  an array: [\"a\" \"b\" \"c\"] for (uri), then we have to list the
  dataset URIs in the same order, such that we have: [\"dataset-a\"
  \"dataset-b\" \"dataset-c\"].

  ##### Usage

  ```
  (crud/read
    (crud/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\"))
  ```"
  [uris]
  {:dataset (core/params-list uris)})

(defn include-linksback
  "Specifies that the reference to the other instance records referring to the target 
  instance record will be added in the resultset.

  The usage of this function is **Optional**

  ##### Usage

  ```
  (crud/read
    (crud/include-linksback))
  ```"
  []
  {:include_linksback true})

(defn exclude-linksback
  "Specifies that the reference to the other instance records referring to the target 
  instance record won't be added in the resultset.

  The usage of this function is the **Default behavior**

  ##### Usage

  ```
  (crud/read
    (crud/exlude-linksback))
  ```"
  []
  {:include_linksback false})

(defn include-reification
  "Specifies that you want to include the reification information for the returned
  records. Reified information is meta-information about some attribute/values
  defined for the records.

  The usage of this function is **Optional**

  ##### Usage

  ```
  (crud/read
    (crud/include-reification))
  ```"
  []
  {:include_reification true})

(defn exclude-reification
  "Specifies that you want to exclude the reification information for the returned
  records. Reified information is meta-information about some attribute/values
  defined for the records.

  The usage of this function is the **Default behavior**
  
  ##### Usage
  
  ```
  (crud/read
    (crud/exclude-reification))
  ```"
  []
  {:include_reification false})

(defn lang
  "Specifies the language of the records to be returned by the search endpoint. Only the textual 
  information of the requested language will be returned to the user. If no textual information 
  is available for a record, for a requested language, then only non-textual information will be 
  returned about the record. The default is `:en`; however, if the parameter is an empty string, 
  then all the language strings for the record(s) will be returned.

  **The default behavior is: `en`**
  
  ##### Parameters
  
  * `lang` Keyword or string that specify the language of the record to return. If the
  language is not supported by the OSF instance, an error message will be returned.

  ##### Usage
  
  ```
  (crud/read
    (crud/lang :fr))
  ```"
  [lang]
  {:lang (name lang)})

(defn uri
  "Set the URI(s) of the records' description needed to be returned by the user

  **This function is required**
  
  ##### Parameters
  
  * `uris` A single URI string, or a vector of URI strings that refers to the
  record(s) that have to be returned by the endpoint.

  ##### Usage

  ```
  (crud/read
    (crud/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\"))
  ```"  
  [uris]
  {:uri (core/params-list uris)})

(defn include-attributes
  "Set a list of attribute URIs to include in the resultset returned by the endpoint.
  All the attributes used to defined the returned resultset that are not listed in this 
  array will be ignored, and won't be returned by the endpoint. This is normally
  used when you know the properties you need for your application, and that you want
  to limit the bandwidth and minimize the size of the resultset.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `uris` A vector of attribute URIs to see in the resultset

  ##### Usage

  ```
  (crud/read
    (crud/include-attributes [\"http://purl.org/ontology/iron#prefLabel\"
                              \"http://purl.org/ontology/cognonto#content\"]))
  ```"
  [uris]
  {:include_attributes_list (core/params-list uris)})

(defn read
  "CRUD Read query.

  **Required**

  ##### Usage

  ```
  (require '[clj-osf.crud.read :as crud-r])

  (crud-r/read
    (crud-r/dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (crud-r/uri \"http://sandbox.opensemanticframework.org/datasets/test/bob\")
    (crud-r/include-reification)
    (crud-r/lang :en))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (exclude-linksback)
                       (exclude-reification)
                       (core/->mime "application/json")
                       (core/->post))
        params (merge default params)]
    (core/osf-query "/ws/crud/read/" params)))
