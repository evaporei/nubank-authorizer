(ns nubank-authorizer.ports
  (:require [nubank-authorizer.controller :refer [controller]]
            [nubank-authorizer.database :as db]))

(defn cli-stdin []
  (let [storage (db/new-database)]
    (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
      (->> input-line
           (controller storage)
           print))))
