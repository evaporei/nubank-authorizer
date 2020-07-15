(ns nubank-authorizer.core
  (:gen-class))

(defn -main
  [& args]
  (doseq [line (line-seq (java.io.BufferedReader. *in*))]
    (println line)))
