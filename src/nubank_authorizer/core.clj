(ns nubank-authorizer.core
  (:gen-class)
  (:require [nubank-authorizer.business_logic :refer [business-logic]]
            [nubank-authorizer.adapters :as adapters]))

(defn cli-port []
  (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
    (-> input-line
        adapters/json-to-edn
        business-logic
        adapters/edn-to-json
        print)))

(defn -main
  [& args]
  (cli-port))
