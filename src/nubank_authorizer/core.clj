(ns nubank-authorizer.core
  (:gen-class)
  (:require [nubank-authorizer.ports :as ports]
            [nubank-authorizer.adapters :as adapters]))

(defn -main
  [& args]
  (ports/cli))
