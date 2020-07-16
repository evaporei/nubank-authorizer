(ns nubank-authorizer.adapters
  (:require [clojure.data.json :as json]
            [camel-snake-kebab.core :as csk]))

(defn json-to-edn [json]
  "Converts JSON String to EDN data structure."
  (json/read-str json :key-fn csk/->kebab-case-keyword))

(defn edn-to-json [edn]
  "Converts EDN data structure to JSON String."
  (if (nil? edn)
    ""
    (str (json/write-str edn :key-fn csk/->camelCaseString) "\n")))
