(ns nubank-authorizer.adapters
  (:require [clojure.data.json :as json]))

(defn json-to-edn [json]
  (json/read-str json :key-fn keyword))

(defn edn-to-json [edn]
  (if (nil? edn)
    ""
    (str (json/write-str edn) "\n")))
