(ns nubank-authorizer.adapters
  (:require [clojure.data.json :as json]
            [camel-snake-kebab.core :as csk]))

(defn json-to-edn [json]
  (json/read-str json :key-fn csk/->kebab-case-keyword))

(defn edn-to-json [edn]
  (if (nil? edn)
    ""
    (str (json/write-str edn :key-fn csk/->camelCaseString) "\n")))
