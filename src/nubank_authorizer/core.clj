(ns nubank-authorizer.core
  (:gen-class))

(defn cli-port []
  (doseq [input-line (line-seq (java.io.BufferedReader. *in*))]
    (-> input-line
        println)))

(defn -main
  [& args]
  (cli-port))
