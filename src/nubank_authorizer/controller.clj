(ns nubank-authorizer.controller
  (:require [nubank-authorizer.business-logic :as business-logic]
            [nubank-authorizer.adapters :as adapters]
            [nubank-authorizer.database :as db]))

(defn create-account! [storage input-data]
  "Creates account, if it exists, returns violations with it."
  (let [db-account (db/get-account storage)
        output (business-logic/create-account db-account input-data)]
    (db/create-account! storage (:account output))
    output))

(defn authorize-transaction! [storage input-data]
  "Authorizes a transaction, it needs an account created to work.
  It returns the account, but if any transaction rules get violated
  it will return with it the `:violations` filled."
  (let [db-account (db/get-account storage)
        db-transactions (db/get-transactions storage)
        output (business-logic/authorize-transaction {:account db-account
                                                      :transaction (:transaction input-data)
                                                      :last-two-transactions (take 2 db-transactions)})]
    (db/create-transaction! storage (:transaction output))
    output))

(defn routing [input-data]
  "Routes to appropriate controller based of map key."
  (cond
    (contains? input-data :account)
      [create-account! input-data]
    (contains? input-data :transaction)
      [authorize-transaction! input-data]
    :else
      [(constantly nil) input-data]))

(defn execute-controller! [storage controller-and-input]
  "Executes controller with storage and user input."
  (let [[controller input-data] controller-and-input]
      (controller storage input-data)))

(defn controller [storage input]
  "Adapts and routes the user input to the correct controller, and executes it.
  It returns the JSON string just as it received."
  (->> input
       adapters/json-to-edn
       routing
       (execute-controller! storage)
       adapters/edn-to-json))
