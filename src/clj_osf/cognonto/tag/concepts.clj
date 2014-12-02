(ns clj-osf.cognonto.tag.concepts
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn document
  [document]
  {:document document})

(defn nb
  [nb]
  {:nb (str nb)})

(defn tag-concepts
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (nb 10)
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/cognonto/tag/concepts/" params)))
