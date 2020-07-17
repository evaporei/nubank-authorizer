(ns nubank-authorizer.core
  (:gen-class)
  (:require [nubank-authorizer.ports :as ports]))

(defn -main
  "Starts the application via CLI with stdin."
  [& args]
  (ports/cli-stdin!))
