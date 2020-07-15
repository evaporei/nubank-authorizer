(ns nubank-authorizer.core
  (:gen-class))

(defn -main
  [& args]
  (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
    (println input-line)))
