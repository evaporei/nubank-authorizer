(ns nubank-authorizer.core
  (:gen-class)
  (:require [clojure.data.json :as json]))

(defn json-to-edn-adapter [json]
  (json/read-str json :key-fn keyword))

(defn cli-port []
  (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
    (-> input-line
        json-to-edn-adapter
        println)))

(defn -main
  [& args]
  (cli-port))
