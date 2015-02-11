# clj-osf

`clj-osf` is a simple Clojure Domain Specific Language (DSL) used to query Open Semantic Framework (OSF) web service endpoints.

Each of the OSF web service endpoint has its own clojure function. A series of function can be chained to generate a OSF query. That function is used to generate any query, to send it to be endpoint of a OSF Web Service instance and to get back a resultset. The resultset can then be manipulated by using the internal [structEDN](http://wiki.opensemanticframework.org/index.php/StructEDN) data structure. 

## Installation

### Using Linengen

You can easily install `clj-osf` using Linengen. The only thing you have to do is to add Add `[clj-osf "0.1.2"]` as a dependency to your `project.clj`.

Then make sure that you downloaded this dependency by running the `lein deps` command.

## Documentation

[The complete `clj-osf` documentation is available here.](http://opensemanticframework.org/documentation/apis/clojure/index.html)

## Usage

### How to use the API

Using this API is really simple. Developers have to:

* Use the clj-osf package in their project
* Declare the namespaces they want to use
* Configure their applications to use the clj-osf package by calling the defosf and defuser macros
* Use the appropriate function to send the query to a web service endpoint.

The first thing you have to do is to make sure you make you add clj-osf as a dependancy in your Leiningen project:

```clojure
(defproject test-osf-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]                
                 [clj-osf "0.1.0"]
                 [clj-turtle "0.1.3"]]
  :target-path "target/%s")
```

Then you have to declare the namespaces you want to use. Let's just declare all of them for this tutorial, but you should only use the ones you required in your application:

```clojure
(ns test-osf-api.core
  (:require [clj-turtle.core :as turtle]
            [clj-osf.core :as osf]
            [clj-osf.utils :as utils]
            [clj-osf.auth.lister :as auth-l]
            [clj-osf.auth.registrar.group :as auth-rg]
            [clj-osf.auth.registrar.user :as auth-ru]
            [clj-osf.auth.registrar.access :as auth-ra]
            [clj-osf.auth.registrar.ws :as auth-rw]
            [clj-osf.crud.create :as crud-c]
            [clj-osf.crud.delete :as crud-d]
            [clj-osf.crud.read :as crud-r]
            [clj-osf.crud.update :as crud-u]
            [clj-osf.revision.lister :as revision-l]
            [clj-osf.revision.read :as revision-r]
            [clj-osf.revision.diff :as revision-diff]
            [clj-osf.revision.update :as revision-u]
            [clj-osf.revision.delete :as revision-d]
            [clj-osf.dataset.create :as dataset-c]
            [clj-osf.dataset.delete :as dataset-d]
            [clj-osf.dataset.read :as dataset-r]
            [clj-osf.dataset.update :as dataset-u]
            [clj-osf.ontology.create :as ontology-c]
            [clj-osf.ontology.read :as ontology-r]
            [clj-osf.ontology.update :as ontology-u]
            [clj-osf.ontology.delete :as ontology-d]
            [clj-osf.sparql :as sparql]
            [clj-osf.search :as search]))
```

Finally you have to make sure that you configure the clj-osf library before starting to use it. This configuration is required to make the API aware of the OSF endpoints to query and to configure the credentials to be able to send queries to it:


```clojure
;; Use/require the namespace
(require '[clj-osf.auth.lister :as auth])

;; Define the OSF Sandbox credentials (or your own):
(require '[clj-osf.core :as osf])

(osf/defosf osf-test-endpoint {:protocol :http
                               :domain "sandbox.opensemanticframework.org"
                               :api-key "EDC33DA4D977CFDF7B90545565E07324"
                               :app-id "administer"})

(osf/defuser osf-test-user {:uri "http://sandbox.opensemanticframework.org/wsf/users/admin"})
```

### Patterns and Usage

This section shows some patterns necessary to understand in order to properly use a OSF instance.

#### Creating a new dataset

When you use the Dataset: Create web service endpoint, the permissions required to access it are not automatically created. It is the developer/user that has to specify the permissions to give to that newly created dataset in order to be able to access it:

```clojure
;; Create a new dataset
(dataset-c/create
 (dataset-c/uri "http://sandbox.opensemanticframework.org/datasets/test/")
 (dataset-c/title "Sandbox testing dataset")
 (dataset-c/description "Sandbox testing dataset.")
 (dataset-c/target-web-services (utils/get-ws-endpoints-uris))
 (dataset-c/creator "http://sandbox.opensemanticframework.org/wsf/users/admin"))

;; Create new CRUD permissions for the test dataset
;; All the users of the 'administrators' group will have full CRUD access to it
(auth-ra/registrar-access
 (auth-ra/create "http://sandbox.opensemanticframework.org/wsf/groups/administrators"
                 "http://sandbox.opensemanticframework.org/datasets/test/"
                 (auth-ra/crud-permissions true true true true)
                 (utils/get-ws-endpoints-uris)))
```

#### Creating/Importing a new ontology

When you use the Ontology: Create web service endpoint, the permissions required to access it are not automatically created. It is the developer/user that has to specify the permissions to give to that newly created ontology in order to be able to access it:

```clojure
;; Create/import an ontology from a dereferencable URI on the Web
(ontology-c/create
 (ontology-c/enable-advanced-indexation)
 (ontology-c/enable-reasoner)
 (ontology-c/uri "http://purl.org/ontology/bibo/"))

;; Create the permissions to access the imported dataset
;; All the users of the 'administrators' group will have full CRUD access to it
(auth-ra/registrar-access
 (auth-ra/create "http://sandbox.opensemanticframework.org/wsf/groups/administrators"
                 "http://purl.org/ontology/bibo/"
                 (auth-ra/crud-permissions true true true true)
                 (utils/get-ws-endpoints-uris)))
```

### Web Services Usage

This section show how each OSF endpoint can be queried using the clj-osf Clojure API.

#### Auth Lister

```clojure
;; Get all groups accesses for a given dataset
(auth-l/lister
 (auth-l/dataset-groups-accesses "http://sandbox.opensemanticframework.org/datasets/test/"))

;; Get all the datasets accessible to a group of users
(auth-l/lister
 (auth-l/group-accesses "http://sandbox.opensemanticframework.org/wsf/groups/administrators"))

;; Get all the groups of the OSF instance
(auth-l/lister
 (auth-l/groups))

;; Get all the users of a target group
(auth-l/lister
 (auth-l/group-users "http://sandbox.opensemanticframework.org/wsf/groups/administrators"))

;; Get all the groups of a target user
(auth-l/lister
 (auth-l/user-groups))

;; Get all the accesses available for that user
(auth-l/lister
 (auth-l/user-accesses))

;; Specifying the target web service endpoint to include in the resultset
(auth-l/lister
 (auth-l/user-accesses)
 (auth-l/include-target-web-service-uri "http://sandbox.opensemanticframework.org/wsf/ws/auth/lister/"))
```

#### Auth Registrar Access

```clojure
;; Create new CRUD permissions for the test dataset
(auth-ra/registrar-access
 (auth-ra/create "http://sandbox.opensemanticframework.org/wsf/groups/administrators"
                 "http://sandbox.opensemanticframework.org/datasets/test/"
                 (auth-ra/crud-permissions true true true true)
                 (utils/get-ws-endpoints-uris)))
```

#### Auth Registrar Group

```clojure
;; Create a new group
(auth-rg/registrar-group
 (auth-rg/group "http://sandbox.opensemanticframework.org/wsf/groups/test")
 (auth-rg/application "administer")
 (auth-rg/create-group))

;; Delete an existing group
(auth-rg/registrar-group
 (auth-rg/group "http://sandbox.opensemanticframework.org/wsf/groups/test")
 (auth-rg/application "administer")
 (auth-rg/delete-group))
```

#### Auth Registrar User

```clojure
;; Add a user to a group
(auth-ru/registrar-user
 (auth-ru/group "http://sandbox.opensemanticframework.org/wsf/groups/test")
 (auth-ru/user "http://sandbox.opensemanticframework.org/wsf/users/admin")
 (auth-ru/join-group))

;; Remove a user from a group
(auth-ru/registrar-user
 (auth-ru/group "http://sandbox.opensemanticframework.org/wsf/groups/test")
 (auth-ru/user "http://sandbox.opensemanticframework.org/wsf/users/admin")
 (auth-ru/leave-group))
```

#### Auth: Registrar WS

```clojure
(auth-rw/registrar-ws
 (auth-rw/title "My New Web Service Endpoint")
 (auth-rw/endpoint-uri "http://sandbox.opensemanticframework.org/wsf/ws/my/new/endpoint/")
 (auth-rw/endpoint-url "http://sandbox.opensemanticframework.org/ws/my/new/endpoint/")
 (auth-rw/crud-usage
  (auth-rw/crud-permissions true true true true)))
```

#### Dataset: Create

```clojure
;; Create a new dataset
(dataset-c/create
 (dataset-c/uri "http://sandbox.opensemanticframework.org/datasets/test/")
 (dataset-c/title "Sandbox testing dataset")
 (dataset-c/description "Sandbox testing dataset.")
 (dataset-c/target-web-services (utils/get-ws-endpoints-uris))
 (dataset-c/creator "http://sandbox.opensemanticframework.org/wsf/users/admin"))
```
#### Dataset: Read

```clojure
;; Reading a dataset's description
(dataset-r/read
 (dataset-r/uri "http://sandbox.opensemanticframework.org/datasets/test/"))

;; Reading all accessible datasets descriptions
(dataset-r/read
 (dataset-r/all))
```

#### Dataset: Update

```clojure
;; Update the description of a dataset
(dataset-u/update
 (dataset-u/uri "http://sandbox.opensemanticframework.org/datasets/test/")
 (dataset-u/title "Sandbox testing dataset - update")
 (dataset-u/description "Sandbox testing dataset - update")
 (dataset-u/contributors ["http://sandbox.opensemanticframework.org/datasets/users/a/"
                          "http://sandbox.opensemanticframework.org/datasets/users/b/"]))
```

#### Dataset: Delete

```clojure
;; Delete all the content of a dataset, including its revisions
(dataset-d/delete
 (dataset-d/uri "http://sandbox.opensemanticframework.org/datasets/test/"))
```

#### Crud: Create

```clojure
;; Create a new record
(turtle/defns iron "http://purl.org/ontology/iron#")
(turtle/defns foo "http://sandbox.opensemanticframework.org/datasets/test/")
(turtle/defns foaf "http://xmlns.com/foaf/0.1/")

(crud-c/create
 (crud-c/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
 (crud-c/document
  (turtle/rdf
   (foo :bob) (turtle/a) (foaf :Person)
   (foo :bob) (iron :prefLabel) (turtle/literal "Bob" :lang :en)
   (foo :bob) (foaf :knows) (foo :alice) (turtle/rei
                                          (foo :since) (turtle/literal "2014-10-25" :type :xsd:dateTime))))
 (crud-c/full-indexation-mode)
 (crud-c/is-rdf-n3))
```

#### Crud: Read

```clojure
;; Read a record including its reification statements
(crud-r/read
 (crud-r/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
 (crud-r/uri "http://sandbox.opensemanticframework.org/datasets/test/bob")
 (crud-r/include-reification)
 (crud-r/lang :en))
```

#### CRUD: Update

```clojure
;; Update an existing record and publish the change
(turtle/defns iron "http://purl.org/ontology/iron#")
(turtle/defns foo "http://sandbox.opensemanticframework.org/datasets/test/")
(turtle/defns foaf "http://xmlns.com/foaf/0.1/")

(crud-u/update
 (crud-u/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
 (crud-u/document
  (turtle/rdf
   (foo :bob) (turtle/a) (foaf :Person)
   (foo :bob) (iron :prefLabel) (turtle/literal "Bob" :lang :en)
   (foo :bob) (foaf :knows) (foo :alice) (turtle/rei
                                           (foo :since) (turtle/literal "2014-10-25" :type :xsd:dateTime))
   (foo :bob) (foaf :knows) (foo :eli) (turtle/rei
                                         (foo :since) (turtle/literal "2014-11-20" :type :xsd:dateTime))))

 (crud-u/is-rdf-n3)
 (crud-u/status-published))
```

#### CRUD: Delete

```clojure
;; Delete an existing record, but keeping the revisions of that record
;; This action is like unpublishing the record. It could be recreated
;; from its revisions.
(crud-d/delete
 (crud-d/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
 (crud-d/uri "http://sandbox.opensemanticframework.org/datasets/test/bob")
 (crud-d/soft))

;; Delete an existing record, but deleting all its revisions as well
(crud-d/delete
 (crud-d/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
 (crud-d/uri "http://sandbox.opensemanticframework.org/datasets/test/bob")
 (crud-d/hard))
```

#### Revision: Diff

```clojure
;; Get all the revisions of the test:bob record
(def revisions (revision-l/lister
                (revision-l/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
                (revision-l/uri "http://sandbox.opensemanticframework.org/datasets/test/bob")
                (revision-l/short-results)))

;; Compare two revisions and return the differences between the two
(revision-diff/diff
 (revision-diff/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
 (revision-diff/left-revision (-> revisions :resultset :subject first :uri))
 (revision-diff/right-revision (-> revisions :resultset :subject second :uri)))
```

#### Revision: Delete

```clojure
;; Get all the revisions of the test:bob record
(def revisions (revision-l/lister
                (revision-l/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
                (revision-l/uri "http://sandbox.opensemanticframework.org/datasets/test/bob")
                (revision-l/short-results)))


;; Delete the second (unpublished) revision of a record
(revision-d/delete
 (revision-d/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
 (revision-d/uri (-> revisions :resultset :subject second :uri)))
```

#### Revision: Lister

```clojure
;; Get all the revisions of the test:bob record
(revision-l/lister
  (revision-l/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
  (revision-l/uri "http://sandbox.opensemanticframework.org/datasets/test/bob")
  (revision-l/short-results))
```

#### Revision: Read

```clojure
;; Get all the revisions of the test:bob record
(def revisions (revision-l/lister
                (revision-l/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
                (revision-l/uri "http://sandbox.opensemanticframework.org/datasets/test/bob")
                (revision-l/short-results)))

;; Get the full description of the first revision returned by `Revision: Lister`
(revision-r/read
 (revision-r/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
 (revision-r/uri (-> revisions :resultset :subject first :uri))
 (revision-r/get-revision))
```
#### Revision: Update

```clojure
;; Update the status of the `test:bob` record to "archive"
(revision-u/update
 (revision-u/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
 (revision-u/uri "http://sandbox.opensemanticframework.org/datasets/test/bob")
 (revision-u/is-archive))
```

#### Ontology: Create

```clojure
;; Create/import an ontology from a dereferencable URI on the Web
(ontology-c/create
 (ontology-c/enable-advanced-indexation)
 (ontology-c/enable-reasoner)
 (ontology-c/uri "http://purl.org/ontology/bibo/"))
```

#### Ontology: Read

```clojure
;; Get all loaded ontologies of the OSF instance
(ontology-r/read
 (ontology-r/get-loaded-ontologies))

;; Get all the classes of the BIBO ontology
(ontology-r/read
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-classes :get-classes-descriptions true))

;; Get the description of the bibo:Book class
(ontology-r/read
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-class "http://purl.org/ontology/bibo/Book"))

;; Get all the properties of the BIBO ontology
(ontology-r/read
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-properties))

;; Get the ironSchema for the BIBO ontology
(ontology-r/read
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-iron-json-schema))

;; Get the a RDF serialization representation of the ontology
(ontology-r/read
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-serialized))

;; Get all the named individuals of the BIBO ontology
(ontology-r/read
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-named-individuals))

;; Get the description of the bibo:unpublished named individual
(ontology-r/read
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-named-individual "http://purl.org/ontology/bibo/status/unpublished"))

;; Get all the ontologies loaded by a given ontology
(ontology-r/read                        
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-ontologies))

;; Get the description of the bibo:abstract property
(ontology-r/read                        
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-property "http://purl.org/ontology/bibo/abstract"))

;; Get the super classes of the bibo:Book class
(ontology-r/read                        
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-super-classes "http://purl.org/ontology/bibo/Book"))

;; Get the sub classes of the bibo:Book class
(ontology-r/read                        
 (ontology-r/ontology "http://purl.org/ontology/bibo/")
 (ontology-r/get-sub-classes "http://purl.org/ontology/bibo/Book"))
```

#### Ontology: Update

```clojure
;; Create a new class
(turtle/defns iron "http://purl.org/ontology/iron#")
(turtle/defns bibo "http://purl.org/ontology/bibo/")
(turtle/defns owl "http://www.w3.org/2002/07/owl#")

(ontology-u/update
 (ontology-u/ontology "http://purl.org/ontology/bibo/")
 (ontology-u/create-or-update-entity
  (turtle/rdf
   (bibo :new-property) (turtle/a) (owl :ObjectProperty)
   (bibo :new-property) (iron :prefLabel) (turtle/literal "Some New Property"))))

;; Update the URI of an existing entity
(ontology-u/update
 (ontology-u/ontology "http://purl.org/ontology/bibo/")
 (ontology-u/update-entity-uri
  "http://purl.org/ontology/bibo/new-property" "http://purl.org/ontology/bibo/update-uri"))
```

#### Ontology: Delete

```clojure
;; Delete a property
(ontology-d/delete
 (ontology-d/ontology "http://purl.org/ontology/bibo/")
 (ontology-d/delete-property "http://purl.org/ontology/bibo/annotates"))
Search
;; Simple search query
(search/search
 (search/query "bob"))

;; Filter by type
(search/search
 (search/type-filters ["http://xmlns.com/foaf/0.1/Person"]))

;; Extended search filter
(search/search
 (search/extended-filters
  (search/build-extended-filters
   (search/+dataset "http://sandbox.opensemanticframework.org/datasets/test/")
   (search/+and)
   (search/+type "http://xmlns.com/foaf/0.1/Person")
   (search/+and)
   "bob")))
```
#### SPARQL

```clojure
;; Simple SPARQL query
(sparql/sparql
  (sparql/dataset "http://sandbox.opensemanticframework.org/datasets/test/")
  (sparql/query "select * where {?s ?p ?o}"))
```

 