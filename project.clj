(defproject clj-osf "0.1.2"
  :description "Clojure API for Open Semantic Framework web services queries"
  :url "https://github.com/structureddynamics/clj-osf"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "1.0.0"]
                 [org.clojure/data.json "0.2.4"]
                 [pandect "0.4.1"]
                 [org.clojure/data.codec "0.1.0"]
                 [clj-time "0.8.0"]
                 [com.cemerick/url "0.1.1"]
                 [clojurewerkz/balagan "1.0.0"]]
  :codox {:defaults {:doc/format :markdown}}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
