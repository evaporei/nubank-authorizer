(ns nubank-authorizer.ports
  (:require [nubank-authorizer.controller :refer [controller]]))

(defn cli-stdin []
  (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
    (-> input-line
        controller
        print)))
