(ns nubank-authorizer.core
  (:gen-class)
  (:require [clojure.data.json :as json]))

(defn create-account [data]
  data)

(defn authorize-transaction [data]
  data)

(defn business-logic [data]
  (cond
    (contains? data :account) (create-account data)
    (contains? data :transaction) (authorize-transaction data)
    :else nil))

(defn json-to-edn-adapter [json]
  (json/read-str json :key-fn keyword))

(defn cli-port []
  (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
    (-> input-line
        json-to-edn-adapter
        business-logic
        println)))

(defn -main
  [& args]
  (cli-port))
