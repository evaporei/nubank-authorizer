(ns nubank-authorizer.controller
  (:require [nubank-authorizer.business-logic :as business-logic]
            [nubank-authorizer.adapters :as adapters]
            [nubank-authorizer.database :as db]))

(defn create-account!
  "Creates account, if it exists, returns violations with it."
  [storage input-data]
  (let [db-account (db/get-account storage)
        output (business-logic/create-account db-account input-data)]
    (when (nil? db-account)
      (db/create-account! storage (:account output)))
    output))

(def clean-unecessary-fields
  "Removes `:transaction` and `:last-two-transactions` fields of a map."
  (partial #(dissoc % :transaction :last-three-transactions)))

(defn authorize-transaction!
  "Authorizes a transaction, it needs an account created to work.
  It returns the account, but if any transaction rules get violated
  it will return with it the `:violations` filled."
  [storage input-data]
  (let [db-account (db/get-account storage)
        db-transactions (db/get-transactions storage)
        output (business-logic/authorize-transaction {:account db-account
                                                      :transaction (:transaction input-data)
                                                      :last-three-transactions (take 3 db-transactions)})]
    (db/create-transaction! storage (:transaction output))
    (db/update-account! storage (:account output))
    (clean-unecessary-fields output)))

(defn routing
  "Routes to appropriate controller based of map key."
  [input-data]
  (cond
    (contains? input-data :account)
      [create-account! input-data]
    (contains? input-data :transaction)
      [authorize-transaction! input-data]
    :else
      [(constantly nil) input-data]))

(defn execute-controller!
  "Executes controller with storage and user input."
  [storage controller-and-input]
  (let [[controller input-data] controller-and-input]
      (controller storage input-data)))

(defn controller!
  "Adapts and routes the user input to the correct controller, and executes it.
  It returns the JSON string just as it received."
  [storage input]
  (->> input
       adapters/json->edn
       routing
       (execute-controller! storage)
       adapters/edn->json))
