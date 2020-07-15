(ns nubank-authorizer.core
  (:gen-class)
  (:require [nubank-authorizer.ports :as ports]))

(defn -main
  [& args]
  (ports/cli-stdin))
