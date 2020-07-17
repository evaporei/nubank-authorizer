(ns nubank-authorizer.ports
  (:require [nubank-authorizer.controller :refer [controller]]
            [nubank-authorizer.in-memory-storage :refer [new-in-memory-storage]]))

(defn cli-stdin []
  "Receives via stdin the user input line by line and
  executes the controller with it."
  (let [storage (new-in-memory-storage)]
    (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
      (->> input-line
           (controller storage)
           print))))
