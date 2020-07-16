(ns nubank-authorizer.controller
  (:require [nubank-authorizer.business-logic :as business-logic]
            [nubank-authorizer.adapters :as adapters]
            [nubank-authorizer.database :as db]))

(defn create-account! [storage input-data]
  (let [db-account (db/get-account storage)
        output (business-logic/create-account db-account input-data)]
    (db/create-account! storage (:account output))
    output))

(defn authorize-transaction! [storage input-data]
  (let [db-account (db/get-account storage)
        db-transactions (db/get-transactions storage)
        output (business-logic/authorize-transaction {:account db-account
                                                      :transaction (:transaction input-data)
                                                      :last-two-transactions (take 2 db-transactions)})]
    (db/create-transaction! storage (:transaction output))
    output))

(defn routing [storage input-data]
  (cond
    (contains? input-data :account)
      (create-account! storage input-data)
    (contains? input-data :transaction)
      (authorize-transaction! storage input-data)
    :else nil))

(defn controller [storage input]
  (->> input
       adapters/json-to-edn
       (routing storage)
       adapters/edn-to-json))
