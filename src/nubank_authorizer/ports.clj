(ns nubank-authorizer.ports
  (:require [nubank-authorizer.controller :refer [controller]]
            [nubank-authorizer.database :as db]))

(defn cli-stdin []
  "Receives via stdin the user input line by line and
  executes the controller with it."
  (let [storage (db/new-database)]
    (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
      (->> input-line
           (controller storage)
           print))))
