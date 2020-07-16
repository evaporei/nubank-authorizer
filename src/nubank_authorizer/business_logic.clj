(ns nubank-authorizer.business-logic
  (:require [clj-time.core :as t]
            [clj-time.format :as f]))

(defn create-account [db-account input-data]
  "Checks if `db-account` is `nil`, then returns `input-data`
  with the `:violations` vector saying if the account was or not
  already initialized"
  (if (nil? db-account)
    (assoc input-data :violations [])
    (assoc input-data :violations [:account-already-initialized])))

(defn has-enough-limit? [data]
  (let [{:keys [transaction account]} data]
    (if (> (:amount transaction) (:available-limit account))
      (update-in data [:violations] #(conj % :insufficient-limit))
      data)))

(defn is-card-active? [data]
  (let [{:keys [account]} data]
    (if (not (:active-card account))
      (update-in data [:violations] #(conj % :card-not-active))
      data)))

(defn is-within-two-minutes? [trx1 trx2]
  (if (nil? trx2)
    false
    (let [[date1 date2] [(:time trx1) (:time trx2)]
          interval-in-minutes (t/in-minutes (t/interval (f/parse date1) (f/parse date2)))]
      (< interval-in-minutes 2))))

(defn is-frequency-high? [data]
  (let [{:keys [transaction last-two-transactions]} data]
    (if (is-within-two-minutes? transaction (last last-two-transactions))
      (update-in data [:violations] #(conj % :high-frequency-small-interval))
      data)))

(defn has-same-payload? [trx1 trx2]
  (and
    (= (:amount trx1) (:amount trx2))
    (= (:merchant trx1) (:merchant trx2))))

(defn is-doubled-transaction? [data]
  (let [{:keys [transaction last-two-transactions]} data]
    (if (and
          (is-within-two-minutes? transaction (first last-two-transactions))
          (has-same-payload? transaction (first last-two-transactions)))
      (update-in data [:violations] #(conj % :doubled-transaction))
      data)))

(defn rules-to-violations [data]
  (-> data
      (assoc :violations [])
      has-enough-limit?
      is-card-active?
      is-frequency-high?
      is-doubled-transaction?))

(defn authorize [data]
  (if (empty? (:violations data))
    (-> data
        (update-in [:account :available-limit] #(- % (get-in data [:transaction :amount])))
        (assoc-in [:transaction :authorized] true))
    (assoc-in data [:transaction :authorized] false)))

(def clean-unecessary-fields (partial #(dissoc % :transaction :last-two-transactions)))

(defn authorize-transaction [data]
  (-> data
      rules-to-violations
      authorize
      clean-unecessary-fields))
