(ns nubank-authorizer.database
  (:require [nubank-authorizer.storage :as storage]))

(defn create-account! [storage account]
  "Adds account in `:account` key of Storage."
  (storage/insert-key! storage :account account))

(defn get-account [storage]
  "Gets account in `:account` key of Storage."
  (storage/get-key storage :account))

(defn update-account! [storage new-account]
  "Updates account in `:account` key of Storage."
  (storage/update-key! storage :account #(merge % new-account))
  new-account)

(defn create-transaction! [storage transaction]
  "Creates transaction list or appends to existing one in Storage."
  (if (storage/has-key? storage :transactions)
    (storage/update-key! storage :transactions #(conj % transaction))
    (storage/insert-key! storage :transactions [transaction]))
  transaction)

(defn get-transactions [storage]
  "Gets transactions list in `:transactions` key of Storage."
  (storage/get-key-with-default storage :transactions []))
