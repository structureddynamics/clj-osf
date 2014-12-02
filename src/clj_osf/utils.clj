(ns clj-osf.utils
  "Different utility functions that can be used in conjunction with the DSL to
  perform some specific tasks."
  (:use [clojurewerkz.balagan.core :only (select)])
  (:require [clj-osf.auth.lister :as auth]))

(defn get-ws-endpoints-uris
  "Utility function used to get a vector of all the web service endpoints
  URIs registered to the OSF network."
  []
  (into [] (select (auth/lister
                    (auth/registered-web-service-endpoints-uri))
                   [:resultset :subject :* :predicate :rdf:li :* :uri])))
