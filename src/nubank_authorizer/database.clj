(ns nubank-authorizer.database
  (:require [nubank-authorizer.storage :as storage]))

(defn create-account! [storage account]
  (storage/insert-key! storage :account account))

(defn get-account [storage]
  (storage/get-key storage :account))

(defn create-transaction! [storage transaction]
  (if (storage/has-key? storage :transactions)
    (storage/update-key! storage :transactions #(conj % transaction))
    (storage/insert-key! storage :transactions [])))

(defn get-transactions [storage]
  (storage/get-key-with-default storage :transactions []))
