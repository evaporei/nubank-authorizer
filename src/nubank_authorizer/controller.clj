(ns nubank-authorizer.controller
  (:require [nubank-authorizer.business_logic :as business-logic]
            [nubank-authorizer.adapters :as adapters]
            [nubank-authorizer.database :as db]))

(defn create-account! [data]
  (let [account (business-logic/create-account data)]
    (db/create-account! account)))

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
