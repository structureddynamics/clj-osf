(ns clj-osf.cognonto.classify
  (:require [clj-osf.core :as core]
            [clojure.string :as string]))

(defn document
  [document]
  {:document document})

(defn classify
  [& body]
  (let [params (apply merge body)
        default (apply merge
                       (core/->post)
                       (core/->mime "application/json"))
        params (merge default params)]
    (core/osf-query "/ws/cognonto/classify/" params)))
