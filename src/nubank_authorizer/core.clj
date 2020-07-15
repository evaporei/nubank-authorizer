(ns nubank-authorizer.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [nubank-authorizer.business_logic :refer [business-logic]]))

(defn json-to-edn-adapter [json]
  (json/read-str json :key-fn keyword))

(defn edn-to-json-adapter [edn]
  (if (nil? edn)
    ""
    (str (json/write-str edn) "\n")))

(defn cli-port []
  (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
    (-> input-line
        json-to-edn-adapter
        business-logic
        edn-to-json-adapter
        print)))

(defn -main
  [& args]
  (cli-port))
