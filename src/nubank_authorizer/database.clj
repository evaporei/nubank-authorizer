(ns nubank-authorizer.database
  (:require [nubank-authorizer.storage :as storage]))

(defn create-account
  "Adds account in `:account` key of Storage."
  [storage account]
  (storage/insert-key! storage :account account))

(defn get-account
  "Gets account in `:account` key of Storage."
  [storage]
  (storage/get-key storage :account))

(defn update-account
  "Updates account in `:account` key of Storage."
  [storage new-account]
  (storage/update-key! storage :account #(merge % new-account))
  new-account)

(defn create-transaction
  "Creates transaction list or appends to existing one in Storage."
  [storage transaction]
  (if (storage/has-key? storage :transactions)
    (storage/update-key! storage :transactions #(conj % transaction))
    (storage/insert-key! storage :transactions (list transaction)))
  transaction)

(defn get-transactions
  "Gets transactions list in `:transactions` key of Storage."
  [storage]
  (storage/get-key-with-default storage :transactions []))
