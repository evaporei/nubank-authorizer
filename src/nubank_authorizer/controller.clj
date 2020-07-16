(ns nubank-authorizer.controller
  (:require [nubank-authorizer.business-logic :as business-logic]
            [nubank-authorizer.adapters :as adapters]
            [nubank-authorizer.database :as db]))

(defn create-account! [input-data]
  (let [db-account (db/get-account)
        output (business-logic/create-account db-account input-data)]
    (db/create-account! (:account output))
    output))

(defn authorize-transaction! [transaction]
  (business-logic/authorize-transaction transaction))

(defn routing [data]
  (cond
    (contains? data :account) (create-account! data)
    (contains? data :transaction) (authorize-transaction! data)
    :else nil))

(defn controller [input]
  (-> input
      adapters/json-to-edn
      routing
      adapters/edn-to-json))
