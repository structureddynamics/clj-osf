(ns clj-osf.core
  "If the `application/json` mime type is specified for a OSF query (it is the default format
  used by all the endpoints) then the JSON resultset returned by the endpoints will be transformed
  in a native EDN format.

  Here is an example of the resultset that is returned by any endpoint:  

  ```
  {:prefixes {:dcterms \"http://purl.org/dc/terms/\"
            :ns0 \"http://purl.org/ontology/bibo#\"
            :xsd \"http://www.w3.org/2001/XMLSchema#\"
            :owl \"http://www.w3.org/2002/07/owl#\"
            :rdfs \"http://www.w3.org/2000/01/rdf-schema#\"
            :iron \"http://purl.org/ontology/iron#\"
            :cognonto \"http://purl.org/ontology/cognonto#\"
            :wsf \"http://purl.org/ontology/wsf#\"
            :rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"}
  :resultset {
             :subject
             [{:uri \"http://techcrunch.com/?p=1081212\"
               :type \"ns0:Article\"
               :predicate
               {:iron:prefLabel [\"Microsoft's ...\"]
                :cognonto:url [\"http://techcrunch.com/?p=1081212\"]
                :cognonto:tag [{:uri \"http://purl.org/ontology/bso#interactive-computing\"
                                :reify [{:type \"cognonto:weight\"
                                         :value \"0.04369007180887\"}]}
                               {:uri \"http://purl.org/ontology/bso#data-migration\"
                                :reify [{:type \"cognonto:weight\"
                                         :value \"0.034965652904319\"}]}
                               {:uri \"http://purl.org/ontology/bso#semantic-technologies\"
                                :reify [{:type \"cognonto:weight\"
                                         :value \"0.043697216645113\"}]}
                               {:uri \"http://purl.org/ontology/bso#machine-learning-algorithms\"
                                :reify [{:type \"cognonto:weight\"
                                         :value \"0.056243105409715\"}]}
                               :cognonto:reviewed [{:value \"0\"
                                                    :type \"xsd:integer\"}]
                 :cognonto:published [{:value \"Tue Nov 11 08:05:49 EST 2014\"
                                       :type \"xsd:dateTime\"}]
                 :cognonto:inDomain [{:value \"0\"
                                      :type \"xsd:integer\"},
                 :cognonto:content [\"Microsofts New 199 Bundle Includes ... \"]
                 :dcterms:isPartOf [{:uri \"http://bigstructure.org/datasets/articles/\"}]}}]}
  ```

  The outline of this structEDN format is the same as the one of the
  [structJSON](http://wiki.opensemanticframework.org/index.php/StructJSON) one at the exception
  that all the keys are keywords. Also, the `:properties` attribute is composed of a map of predicates
  and each of them have a vector of values. [More information about structEDN can be found on this
  wiki page.](http://wiki.opensemanticframework.org/index.php/StructEDN)"
  (:require [clojure.data.codec.base64 :as b64]
            [pandect.core :refer (sha1-hmac-bytes)]
            [clj-time.core :as time]
            [clj-time.coerce :as tc]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clojure.data.json :as json]))

;; Default OSF endpoint to be used by the clj-osf
(defonce ^:no-doc _default-osf (atom nil))

;; Default OSF user to be used by the clj-osf
(defonce ^:no-doc _default-user (atom nil))

(defn ^:no-doc default-endpoint
  "Set the default endpoint options and parameters to be used by the API
   if no endpoint specifications is specified"
  [endpoint]
  (reset! _default-osf endpoint))

(defn ^:no-doc default-user
  "Set the default user options and parameters to be used by the API
   if no user specifications is specified"
  [user]
  (reset! _default-user user))

(defmacro defosf
  "Macro to be used to create new OSF endpoints

   Here is an example for the options parameter which includes all possible options:
  
     {:protocol :http
      :domain \"test.com\"
      :api-key \"...\"
      :app-id \"...\"}"
  [osf-name options]
  `(do 
     (defonce ~osf-name ~options)
     (default-endpoint ~osf-name)))

(defmacro defuser
  "Macro to be used to create new OSF user

   Here is an example for the options parameter which includes all possible options:
  
     {:uri \"http://localhost/ws/users/admin\"}"
  [user-name options]
  `(do 
     (defonce ~user-name ~options)
     (default-user ~user-name)))

(defn- bytes-to-base64-string [bytes]
  (String. (b64/encode bytes) "UTF-8"))

(defn- timestamp
  []
  (tc/to-long (time/now)))

(defn- get-serialized-params
  [params]
  (subs (->> (for [[key value] params]
               (str "&" (name key) "=" value))
             (apply str)) 1))

(defn- md5-raw [s]
  (let [algorithm (java.security.MessageDigest/getInstance "MD5")
	size (* 2 (.getDigestLength algorithm))]
    (.digest algorithm (.getBytes s "UTF-8"))))

(defn- security-hash
  [params method ws api-key timestamp]
  (let [md5-payload (-> params
                        get-serialized-params
                        md5-raw
                        bytes-to-base64-string)
        data (str (if (= method :post) "POST" "GET") md5-payload ws timestamp)
        hash (sha1-hmac-bytes data api-key)]
    (bytes-to-base64-string hash)))

(defn- escape-utf16
  [[_ _ a b c d]]
  (format "\\u%02X%02X\\u%02X%02X" a b c d))

(defn- replace-utf32
  [^String s]
  (let [n (Integer/parseInt (subs s 2) 16)]
    (-> (->> (map #(bit-shift-right n %) [24 16 8 0])
             (map #(bit-and % 0xFF))
             (byte-array))
        (String. "UTF-32")
        (.getBytes "UTF-16")
        (escape-utf16))))

(defn- fix-json-utf32
  [json]
  (clojure.string/replace
   json
   #"\\U[0-9A-F]{8}"
   replace-utf32))


(defn ^:no-doc params-list
  "Create a serialized list of values to be used as parameters in a OSF query.
   If [v] is not a vector, then [v] is returned as-is. If it is a vector then
   a string of values seperated by semi-colon will be returned. Semi-colon
   present in the strings are encoded as %3B."
  [v]
  (if-not (vector? v)
            v
            (->> v
                 (map #(vector (string/replace %1 #";" "%3B")))
                 (apply concat)
                 (string/join ";"))))

(defn ^:no-doc internalize-json-response
  "Change the OSF structJSON serialization such that all the we only get one key
   per predicate with a vector of values"
  [response]
  (let [json (json/read-str (fix-json-utf32 (response :body)) :key-fn #(keyword %))]
    (if (json :prefixes)
      {:prefixes (json :prefixes)
       :resultset {:subject (->> (for [subject (-> json :resultset :subject)]
                                   (update-in subject
                                              [:predicate]
                                              (fn [predicates]
                                                (->> predicates
                                                     (map (fn [predicate]
                                                            (let [k (-> predicate first key)
                                                                  v (-> predicate first val)]
                                                              {k [v]})))
                                                     (apply merge-with into)))))
                                 (into []))}}
      json)))

(defn ^:no-doc osf-query
  [ws options & {:keys [osf user]
                 :or {osf @_default-osf
                      user @_default-user}}]
  (let [additional-http-params (if (options :debug) {:debug true} {})
        params (dissoc options :->mime :method :debug)
        timestamp (timestamp)
        hash (security-hash params (options :method) ws (osf :api-key) timestamp)
        response (case (options :method)
                   :get (client/get (str (name (osf :protocol)) "://" (osf :domain) ws)
                                    (apply merge
                                           {:query-params params
                                            :throw-exceptions false
                                            :headers {:OSF-TS timestamp
                                                      :OSF-APP-ID (osf :app-id)
                                                      :OSF-USER-URI (user :uri)
                                                      :Authorization hash
                                                      :Accept (options :->mime)}}
                                           additional-http-params))
                   :post (client/post (str (name (osf :protocol)) "://" (osf :domain) ws)
                                      (apply merge
                                             {:form-params params
                                              :throw-exceptions false
                                              :headers {:OSF-TS timestamp
                                                        :OSF-APP-ID (osf :app-id)
                                                        :OSF-USER-URI (user :uri)
                                                        :Authorization hash
                                                        :Accept (options :->mime)}}
                                             additional-http-params)))]

    (when (additional-http-params :debug)
      (println "\n\n\n")
      (println "---------------")
      (println "Parameters:")
      (prn params)
      (println "---------------")      
      (println "\n\n\n")
      (println "---------------")
      (println "Response:")
      (prn response)
      (println "---------------"))
    
    (case (options :->mime)
      "application/json" (internalize-json-response response)
      "application/iron+json" (json/read-str (fix-json-utf32 (response :body))
                                             :key-fn #(keyword %))
      "application/edn" (read-string (response :body))
      "application/clojure" (read-string (response :body))
      "application/sparql-results+json" (fix-json-utf32 (response :body))
      (response :body))))


(defn ->mime
  "Define the mime type of the query. The mime type will be used
  in the `Accept` header of the HTTP query."
  [m]
  {:->mime m})

(defn ->get
  "Specify that the query is a HTTP GET query"
  []
  {:method :get})

(defn ->post
  "Specify that the query is a HTTP POST query"
  []
  {:method :post})

(defn debug
  "Specify that you want to have debug information displayed in the REPL
  for that query."
  []
  {:debug true})












