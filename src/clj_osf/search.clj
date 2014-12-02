(ns clj-osf.search
  "Send a The Search Web service is used to perform full text searches on the structured 
  data indexed on a OSF instance. A search query can be as simple as querying 
  the data store for a single keyword, or to query it using a series of complex 
  filters. Each search query can be applied to all, or a subset of, datasets 
  accessible by the requester. All of the full text queries comply with the 
  Lucene querying syntax.
   
  Each Search query can be filtered by these different filtering criteria:
   + Type of the record(s) being requested
   + Dataset where the record(s) got indexed
   + Presence of an attribute describing the record(s)
   + A specific value, for a specific attribute describing the record(s)
   + A distance from a lat/long coordinate (for geo-enabled OSF instance)
   + A range of lat/long coordinates (for geo-enabled OSF instance)
   
  To use the Search code, you have to:
  
  ```
  ;; Use/require the namespace
  (require '[clj-osf.search :as search])

  ;; Define the OSF Sandbox credentials (or your own):
  (require '[clj-osf.core :as osf])

  (osf/defosf osf-test-endpoint {:protocol :http
                                 :domain \"sandbox.opensemanticframework.org\"
                                 :api-key \"EDC33DA4D977CFDF7B90545565E07324\"
                                 :app-id \"administer\"})

  (osf/defuser osf-test-user {:uri \"http://sandbox.opensemanticframework.org/wsf/users/admin\"})
  ```

  [Open Semantic Framework Endpoint Documentation](http://wiki.opensemanticframework.org/index.php/Search#Web_Service_Endpoint_Information)"    
  (:require [clj-osf.core :as core]
            [clojure.string :as string]
            [cemerick.url :as url]))

(defn aggregate-attributes
  "Specify a vector of attributes URI for which we want their aggregated values.
  This is used to get a list of values, and their counts for a given attribute.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[attributes]` Vector of attributes URI for which we want their aggregates

  ##### Usage

  ```
  (search/search
    (search/aggregate-attributes [\"http://purl.org/ontology/iron#prefLabel\"
                                  \"http://xmlns.com/foaf/0.1/knows\"]))
  ```"         
  [attributes]
  {:aggregate_attributes (core/params-list attributes)})

(defn attributes-phrase-boost
  "Modifying the score of the results returned by the Search endpoint by
  boosting the results where the field has all keywords within the
  distance defined by phrase distance.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris-boosts]` Vector of attribute-uri/boot of the form:
                      [{:uri \"...\"
                        :boost 2}]
                    where, `:uri` is the attribute URI to boost and
                    `:boost` is the score modifier weight. Score modifier
                    can be quite small like 0.0001, or quite big like 10000.

  ##### Usage

  ```
  (search/search
    (search/attributes-phrase-boost
      [{:uri \"http://purl.org/ontology/iron#prefLabel\"
        :boost 2}]))
  ```"           
  [uris-boosts]
  {:attributes_phrase_boost (->> uris-boosts
                                 (map (fn [{uri :uri boost :boost}]
                                        (vector (str (-> uri
                                                         (string/replace #";" "%3B")
                                                         (string/replace #"\^" "%5E")) "^" (.toString boost)))))
                                 (apply concat)
                                 (string/join ";"))})

(defn attributes-values-filters
  "Set all the attribute/value filters to use for this search query

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris-boosts]` Vector of attribute-uri/value of the form:
                      [{:uri \"...\"
                       :values []}
                       {:uri \"...\"}]  
                    where, `:uri` is the attribute URI to filter by and
                    `:values` is a vector of values to filter by the given
                    attribute.

  ##### Usage

  ```
  (search/search
    (search/attributes-values-filters
      [{:uri \"http://purl.org/ontology/iron#prefLabel\"
        :values [\"bob\"]}]))
  ```"  
  [attr-values]
  (if (empty? attr-values)
    {:attributes "all"}
    {:attributes (->> attr-values
                      (map (fn [{uri :uri values :values}]
                             (if (empty? values)
                               (vector uri)
                               (->> values
                                    (map #(vector (str (string/replace uri #";" "%3B") "::" (string/replace %1 #";" "%3B"))))
                                    (apply concat)))))
                      (apply concat)
                      (string/join ";"))}))

(defn attributes-values-boost
  "Modifying the score of the results returned by the Search endpoint by
  boosting the results that have that attribute/value, and boosting it by the
  modifier weight.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris-boosts]` Vector of attribute-uri/value of the form:
                      [{:uri \"...\"
                        :values []
                        :boost 12}]
                       ...]  
                    where, `:uri` is the attribute URI to filter by,
                    `:values` is a vector of values to filter by the given
                    attribute and `:boost` is the score modifier weight.
                    Score modifier can be quite small like 0.0001, or
                    quite big like 10000.

  ##### Usage

  ```
  (search/search
    (search/attributes-values-boost
      [{:uri \"http://purl.org/ontology/iron#prefLabel\"
        :values [\"bob\"]
        :boost 12}]))
  ```"   
  [attr-values & {:keys [is-uri]
                  :or {is-uri false}}]
  {:attributes_boost (->> attr-values
                          (map (fn [attr-values]
                                 (if (empty? (attr-values :values))
                                   (vector (str (-> attr-values
                                                    :uri
                                                    (string/replace #";" "%3B")
                                                    (string/replace #"\^" "%5E"))
                                                (when is-uri "[uri]")
                                                "^"
                                                (attr-values :boost)))
                                   (->> (attr-values :values)
                                        (map #(vector (str (-> attr-values
                                                               :uri
                                                               (string/replace #";" "%3B")
                                                               (string/replace #"\^" "%5E"))
                                                           (when is-uri "[uri]")
                                                           "::"
                                                           (string/replace %1 #";" "%3B")
                                                           "^"
                                                           (attr-values :boost))))
                                        (apply concat)))))
                          (apply concat)
                          (string/join ";"))})

(defn dataset-filters
  "Set all the dataset filters to use for this search query

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris]` Vector of dataset URIs to use as filters

  ##### Usage

  ```
  (search/search
    (search/dataset-filters [\"http://sandbox.opensemanticframework.org/datasets/test/\"]))
  ```"    
  [uris]
  (if (empty? uris)
    {:datasets "all"}
    {:datasets (core/params-list uris)}))

(defn default-operator-and
 "Set the default search query operator to AND

  The usage of this function is **Optional**
  
  ##### Usage

  ```
  (search/search
    (search/default-operator-and))
  ```"   
  []
  {:default_operator "and"})
  
(defn default-operator-or
 "Set the default search query operator to OR

  The usage of this function is **Optional**

  ##### Parameters
  
  * `[constraints]` Minimal number of words that should be present in the returned records.
                    More complex behaviors can be defined, the full syntax is explained 
                    in the [Min Number Should Match Specification Format](http://lucene.apache.org/solr/4_1_0/solr-core/org/apache/solr/util/doc-files/min-should-match.html) document.
  
  ##### Usage

  ```
  (search/search
    (search/default-operator-or 2))
  ```"   
  [constraints]
  {:default_operator (str "or::" constraints)})

(defn disable-inference
  "Disable the inference for this query

  The usage of this function is **Optional**
  
  ##### Usage

  ```
  (search/search
    (search/disable-inference))
  ```"     
  []
  {:inference "off"})

(defn enable-inference
  "Enable the inference for this query

  The usage of this function is **Optional**
  
  ##### Usage

  ```
  (search/search
    (search/enable-inference))
  ```"     
  []
  {:inference "on"})

(defn distance-filter
  "The distance filter is a series of parameter that are used to filter records 
  of the dataset according to the distance they are located from a given lat;long point

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[lat]` Latitude of the point of origin
  * `[long]` Longitude of the point of origin
  * `[distance]` The distance from the point of origin
  * `[type]` *(optional)* One of: `:km` or `:mile`

  ##### Usage

  ```
  (search/search
    (search/distance-filter 40.3 49.2 100 :km))
  ```"      
  [lat long distance & {:keys [type]
                        :or {type :km}}]
  (let [distance (if (= distance 0) 1 distance)
        type (if (= type :mile) 1 0)]
    {:distance_filter (str lat ";" long ";" distance ";" (name type))}))

(defn exclude-aggregates
  "Exclude the aggregate records in the resultset of this query

  The usage of this function is **Optional**
  
  ##### Usage

  ```
  (search/search
    (search/exclude-aggregates))
  ```"     
  []
  {:include_aggregates "false"})

(defn include-aggregates
  "Include the aggregate records in the resultset of this query

  The usage of this function is the **default behavior**
  
  ##### Usage

  ```
  (search/search
    (search/include-aggregates))
  ```"     
  []
  {:include_aggregates "true"})

(defn exclude-score
  "Exclude the scores of the results into the resultset

  ##### Usage

  ```
  (search/search
    (search/exclude-score))
  ```"   
  []
  {:include_score "false"})

(defn include-score
  "Include the scores of the results into the resultset. The score will be represented
  by the `wsf:score` property.
  
  ##### Usage

  ```
  (search/search
    (search/include-score))
  ```"     
  []
  {:include_score "true"})

(defn exclude-spellcheck
  "Exclude the spellchecking suggestions to the resultset even if the resultset is empty
  
  ##### Usage

  ```
  (search/search
    (search/exclude-spellcheck))
  ```"     
  []
  {:include_spellcheck "false"})

(defn include-spellcheck
  "Includes the spellchecking suggestions to the resultset in the case that the resultset is empty.
  
  ##### Usage

  ```
  (search/search
    (search/include-spellcheck))
  ```"
  []
  {:include_spellcheck "true"})

(defn extended-filters
  "Extended filters are used to define more complex search filtered searches. 
  
  ##### Usage

  ```
  (search/search
    (search/build-extended-filters
      (search/+dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
      (search/+and)
      (search/+type \"http://xmlns.com/foaf/0.1/Person\")
      (search/+and)
      \"bob\"))
  ```"  
  [filters]
  {:extended_filters filters})

(defn include-attributes
  "Set a list of attribute URIs to include in the resultset returned by the search endpoint.
  All the attributes used to defined the returned resultset that are not listed in this 
  array will be ignored, and won't be returned by the endpoint. This is normally
  used when you know the properties you need for your application, and that you want
  to limit the bandwidth and minimize the size of the resultset.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris]` Vector of attribute URIs to see in the resultset

  ##### Usage

  ```
  (search/search
    (search/include-attributes [\"http://purl.org/ontology/iron#prefLabel\"
                                \"http://xmlns.com/foaf/0.1/knows\"]))
  ```"     
  [uris]
  {:include_attributes_list (core/params-list uris)})

(defn include-no-attributes
  "Specify that no attributes should be returned by the query. The only two attributes
  that will be returned by the search endpoint are the URI and the Type of each
  result.
  
  ##### Usage

  ```
  (search/search
    (search/include-no-attributes))
  ```"    
  []
  {:include_attributes_list "none"})

(defn items
  "Set the number of items to return in a single resultset 

  Default value is 10
  
  ##### Parameters
  
  * `[nb]` The number of items to return in a single resultset 

  ##### Usage

  ```
  (search/search
    (search/items 20))
  ```"  
  [nb]
  {:items (if (< nb 0) 0 nb)})

(defn page
  "Set the offset of the resultset to return. By example, to get the item 90 to 100, this 
  parameter should be set to 90. 
  
  Default page is 0
  
  ##### Parameters
  
  * `[nb]` The offset of the resultset to return. By example, to get the item 90 to 100,
           this parameter should be set to 90. 

  ##### Usage

  ```
  (search/search
    (search/page 2))
  ```"  
  [nb]
  {:page (if (< nb 0) 0 nb)})

(defn lang
  "Set the language of the records to be returned by the search endpoint. Only the textual 
  information of the requested language will be returned to the user. If no textual 
  information is available for a record, for a requested language, then only non-textual 
  information will be returned about the record.
  
  Default is `:en`
  
  ##### Parameters
  
  * `[lang]` Keyword that represents the ISO 639-1 language code standard. Examples are
             `fr`, `en` and `es`

  ##### Usage

  ```
  (search/search
    (search/lang :fr))
  ```"    
  [lang]
  {:lang (string/lower-case (name lang))})

(defn number-of-aggregate-attributes-object
  "Determine the number of value to aggregate for each aggregated attributes for 
  this query. If the value is -1, then it means that all possible values for the 
  target aggregated attributes will to be returned.
  
  ##### Parameters
  
  * `[nb]` Number of values to aggregate

  ##### Usage

  ```
  (search/search
    (search/number-of-aggregate-attributes-object 10))
  ```"      
  [nb]
  {:aggregate_attributes_object_nb (if (< nb -1) -1 nb)})

(defn phrase-boost-distance
  "Define the maximum distance between the keywords of the search query that is 
  used by the `(attribute-phrase-boost)`.

  ##### Parameters
  
  * `[distance]` Maximum distance betweeen the search terms

  ##### Usage

  ```
  (search/search
    (search/phrase-boost-distance 4))
  ```"      
  [distance]
  {:phrase_boost_distance distance})

(defn query
  "Set the keywords to use for this search query.

  ##### Parameters
  
  * `[q]` Keywords to use to search for this query. Keywords can use some
          boolean operations. An empty string returns everything.

  ##### Usage

  ```
  (search/search
    (search/q \"bob\"))
  ```"
  [q]
  {:query q})

(defn range-filter
  "The range filter is a series of parameter that are used to filter records of the 
  dataset according to a rectangle bounds they are located in given their lat;long 
  position.

  ##### Parameters
  
  * `[top-left-lat]` Latitude of the top left corner of the bounding box
  * `[top-left-long]` Longitude of the top left corner of the bounding box
  * `[bottom-right-lat]` Latitude of the bottom right corner of the bounding box
  * `[bottom-right-long]` Longitude of the bottom right corner of the bounding box

  ##### Usage

  ```
  (search/search
    (search/range-filter \"40.2\" \"50.2\" \"54.4\" \"45.6\"))
  ```"  
  [top-left-lat top-left-long bottom-right-lat bottom-right-long]
  {:range_filter (str top-left-lat ";" top-left-long ";" bottom-right-lat ";" bottom-right-long)})

(defn records-location-aggregator
  "Specifies a lat/long location where all the results should be aggregated around. For example, if we have 
  a set of results compromised within a region. If we don't want the results spread everywhere in that region, 
  we have to specify a location for this parameter such that all results get aggregated around that specific 
  location within the region.

  ##### Parameters
  
  * `[lat]` Latitude of the target location
  * `[long]` Longitude of the target location

  ##### Usage

  ```
  (search/search
    (search/records-location-aggregator \"40.2\" \"50.2\"))
  ```"
  [lat long]
  {:results_location_aggregator (str lat ";" long)})

(defn search-restrictions
  "Restrict a search to the records that are described using some specific attributes.
  Then the results are ordered by a boosting factor.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris-boosts]` Vector of attribute-uri/value of the form:
                      [{:uri \"...\"
                        :boost 2}]
                    where, `:uri` is the attribute URI to filter by,
                    `:values` is a vector of values to filter by the given
                    attribute and `:boost` is the score modifier weight.
                    Score modifier can be quite small like 0.0001, or
                    quite big like 10000.

  ##### Usage

  ```
  (search/search
    (search/search-restrictions
      [{:uri \"http://purl.org/ontology/iron#prefLabel\"
        :boost 12}]))
  ```"   
  [uris-boosts]
  {:search_restrictions (->> uris-boosts
                             (map (fn [{uri :uri boost :boost}]
                                    (vector (str (-> uri
                                                     (string/replace #";" "%3B")
                                                     (string/replace #"\^" "%5E")) "^" (.toString boost)))))
                             (apply concat)
                             (string/join ";"))})

(defn aggregate-attribute-object-type-uri
  "Determines that the aggregated value returned by the endpoint is a URI. If the value 
  of the attribute(s) is a URI (a reference to some record) then that URI will be 
  returned as the aggregated value. 
  
  ##### Usage

  ```
  (search/search
    (search/aggregate-attribute-object-type-uri))
  ```"      
  []
  {:aggregate_attributes_object_type "uri"})

(defn aggregate-attribute-object-type-literal
  "Determines that the aggregated value returned by the endpoint is a literal. If the 
  value is a URI (a reference to some record), then the literal value will be the 
  preferred label of that referred record.
   
  This is the default behavior of this service.
  
  ##### Usage

  ```
  (search/search
    (search/aggregate-attribute-object-type-literal))
  ```"      
  []
  {:aggregate_attributes_object_type "literal"})

(defn aggregate-attribute-object-type-uri-literal
  "Determines that the aggregated value returned by the endpoint is a URI and a Literal
  
  ##### Usage

  ```
  (search/search
    (search/aggregate-attribute-object-type-uri-literal))
  ```"      
  []
  {:aggregate_attributes_object_type "uriliteral"})

(defn attributes-boolean-operation-and
  "Set the attributes boolean operator to AND. If you have multiple attribute/value filters
  defined for this search query, then the Search endpoint will AND all of them.
   
  This is the default behavior of this service.
  
  ##### Usage

  ```
  (search/search
    (search/attributes-boolean-operation-and))
  ```"    
  []
  {:attributes_boolean_operator "and"})

(defn attributes-boolean-operation-or
  "Set the attributes boolean operator to OR. If you have multiple attribute/value
  filters defined for this search query, then the Search endpoint will OR all of them.
  
  ##### Usage

  ```
  (search/search
    (search/attributes-boolean-operation-or))
  ```"    
  []
  {:attributes_boolean_operator "or"})

(defn sorts
  "Add a sort criteria to the Search query.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris-orders]` Vector of attribute-uri/value of the form:
                      [{:uri \"...\"
                        :order :asc}]
                    where, `:uri` is the attribute URI to filter by,
                    `:order` is the sort for that property. Can
                    be `:desc` or `:asc`

  ##### Usage

  ```
  (search/search
    (search/sort
      [{:uri \"http://purl.org/ontology/iron#prefLabel\"
        :order :asc}]))
  ```"     
  [uris-orders]
  {:sort (->> uris-orders
              (map (fn [{uri :uri order :order}]
                     (vector (str (string/replace uri #";" "%3B") " " (name order)))))
              (apply concat)
              (string/join ";"))})

(defn types-boost
  "Modifying the score of the results returned by the Search endpoint by
  boosting the results that have that type, and boosting it by the
  modifier weight.

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris-boosts]` Vector of type-uri/value of the form:
                      [{:uri \"...\"
                        :boost 2}]
                    where, `:uri` is the attribute URI to filter by,
                    `:boost` is the score modifier weight.
                    Score modifier can be quite small like 0.0001, or
                    quite big like 10000.

  ##### Usage

  ```
  (search/search
    (search/sort
      [{:uri \"http://xmlns.com/foaf/0.1/Person\"
        :boost 11}]))
  ```"       
  [uris-boosts]
  {:types-boost (->> uris-boosts
                     (map (fn [{uri :uri boost :boost}]
                            (vector (str (-> uri
                                             (string/replace #";" "%3B")
                                             (string/replace #"\^" "%5E")) "^" (.toString boost)))))
                     (apply concat)
                     (string/join ";"))})

(defn type-filters
  "Set all the type filters to use for this search query

  The usage of this function is **Optional**
  
  ##### Parameters
  
  * `[uris]` Vector of type URIs to use as filters

  ##### Usage

  ```
  (search/search
    (search/type-filters [\"http://xmlns.com/foaf/0.1/Person\"]))
  ```"    
  [uris]
  {:types(core/params-list uris)})




;; Extended filter builder functions

(defn build-extended-filters
  "Used to generate a set of extended attribute filters that should be added
  to a `(search/query)`. These extended attributes filters support grouping of 
  attributes/values filters along with the boolean operators AND, OR and NOT.
  
  Here is an example of how this DSL should be used to create an extended
  search filters for the `(search/extended-search)` function:

  ##### Usage

  ```
  (search/build-extended-filters
    (search/)
  ```"        
  [& body]
  (apply str body))

(defn +and
  "Add a AND operator to the extended filters query

  ##### Usage

  ```
  (search/build-extended-filters
    \"test\" (search/+and) \"bob\")
  ```"        
  []
  (str " AND "))
        
(defn +or
  "Add a OR operator to the extended filters query

  ##### Usage

  ```
  (search/build-extended-filters
    \"test\" (search/+or) \"bob\")
  ```"        
  []
  (str " OR "))

(defn +not
  "Add a NOT operator to the extended filters query

  ##### Usage

  ```
  (search/build-extended-filters
    (search/+not) \"bob\")
  ```"        
  []
  (str " NOT "))

(defn +dataset
  "Add a dataset URI to filter

  ##### Parameters
  
  * `[uri]` Dataset URI to add to the extended filters query.
  
  ##### Usage

  ```
  (search/build-extended-filters
    (search/+dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (search/+and)
    \"bob\")
  ```"        
  [uri]
  (str "dataset:\"" uri "\""))

(defn +type
  "Add a type URI to filter

  ##### Parameters
  
  * `[uri]` Type URI to add to the extended filters query.
  * `[enable-inference]` *(optional)* inferencing for this type filter. `true` to enable inference
  
  ##### Usage

  ```
  (search/build-extended-filters
    (search/+dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
    (search/+and)
    (search/+type \"http://xmlns.com/foaf/0.1/Person\")
    (search/+and)
    \"bob\")
  ```"        
  [uri & {:keys [enable-inference]
          :or {enable-inference false}}]
  (if enable-inference
    (str "(type:\"" uri "\" OR inferred_type:\"" uri "\"")
    (str "type:\"" uri "\"")))

(defn +attribute
  "Add an attribute/value filter

  ##### Parameters
  
  * `[uri]` Type URI to add to the extended filters query.
  * `[value]` *(optional)* Value to filter by. By default, all values are used (\"*\")
  * `[value-is-uri]` *(optional)* Specify if the value (or set of values) for this attribute have to be considered
                                  as URIs (this should be specified to TRUE if the attribute is an object property)
  
  ##### Usage

  ```
  (search/build-extended-filters
    (search/+attribute \"http://purl.org/ontology/iron#prefLabel\" :value \"bob\"))
  ```"        
  [uri & {:keys [value value-is-uri]
          :or {value "*"
               value-is-uri false}}]
   (str (url/url-encode uri) (when value-is-uri "[uri]") ":" value))

(defn grouping
  "Group a series of extended filters criterias

  ##### Usage

  ```
  (search/build-extended-filters
    (search/grouping
     \"string-a\"
     (search/+and)
     \"string-b\")
    (search/+or)
    (search/grouping
     \"string-c\"
     (search/+and)
     \"string-d\"))
  ```"  
  [& body]
  (str "(" (apply str body) ")"))

(defn search
  "Search query.

  **Required**

  ##### Usage

  ```
  ;; Simple search query
  (search/search
    (search/query \"bob\"))

  ;; Filter by type
  (search/search
    (search/type-filters [\"http://xmlns.com/foaf/0.1/Person\"]))

  ;; Extended search filter
  (search/search
    (search/extended-filters
      (search/build-extended-filters
        (search/+dataset \"http://sandbox.opensemanticframework.org/datasets/test/\")
        (search/+and)
        (search/+type \"http://xmlns.com/foaf/0.1/Person\")
        (search/+and)
        \"bob\")))
  ```"
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (query "")
                       (attributes-boolean-operation-and)
                       (items 10)
                       (page 0)
                       (disable-inference)
                       (include-aggregates)
                       (aggregate-attribute-object-type-literal)
                       (number-of-aggregate-attributes-object 10)                       
                       (core/->mime "application/json")
                       (core/->post))
        params (merge default params)]
    (core/osf-query "/ws/search/" params)))
