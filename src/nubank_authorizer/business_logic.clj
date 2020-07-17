(ns nubank-authorizer.business-logic
  (:require [clj-time.core :as t]
            [clj-time.format :as f]))

(defn create-account [db-account input-data]
  "Returns input-data with `:violations` vector with it.
  In the case of `db-account` being nil, this vector will
  be populated."
  (if (nil? db-account)
    (assoc input-data :violations [])
    (assoc input-data :violations [:account-already-initialized])))

(defn insufficient-limit-rule [data]
  "Receives `:transaction`, `:account` and `:violations` in a map
  and returns it, appending to `:violations` vector in the case
  of the transaction amount being bigger than the available account limit."
  (let [{:keys [transaction account]} data]
    (if (> (:amount transaction) (:available-limit account))
      (update-in data [:violations] #(conj % :insufficient-limit))
      data)))

(defn card-not-active-rule [data]
  "Receives `:account` and `:violations` in a map and returns it,
  appending to `:violations` vector in the case of the account card
  being not active."
  (let [{:keys [account]} data]
    (if (not (:active-card account))
      (update-in data [:violations] #(conj % :card-not-active))
      data)))

(defn is-within-two-minutes? [trx1 trx2]
  "Receives two transactions which their time interval will be checked
  if it is within two minutes."
  (if (nil? trx2)
    false
    (let [[date1 date2] [(:time trx1) (:time trx2)]
          interval-in-minutes (t/in-minutes (t/interval (f/parse date1) (f/parse date2)))]
      (< interval-in-minutes 2))))

(defn high-frequency-small-interval-rule [data]
  "Receives `:transaction`, `:last-two-transactions` and `:violations` in a map and returns it,
  appending to `:violations` vector in the case of the last of two transactions and the new
  transaction being within two minutes."
  (let [{:keys [transaction last-two-transactions]} data]
    (if (is-within-two-minutes? transaction (last last-two-transactions))
      (update-in data [:violations] #(conj % :high-frequency-small-interval))
      data)))

(defn has-same-payload? [trx1 trx2]
  "Compares the equality of `:amount` and `:merchant` of two transactions."
  (and
    (= (:amount trx1) (:amount trx2))
    (= (:merchant trx1) (:merchant trx2))))

(defn doubled-transaction-rule [data]
  "Receives `:transaction`, `:last-two-transactions` and `:violations` in a map and returns it,
  appending to `:violations` vector in the case of the first of the last two transactions and the new
  transaction being within two minutes AND they having the same payload."
  (let [{:keys [transaction last-two-transactions]} data]
    (if (and
          (is-within-two-minutes? transaction (first last-two-transactions))
          (has-same-payload? transaction (first last-two-transactions)))
      (update-in data [:violations] #(conj % :doubled-transaction))
      data)))

(defn apply-authorization-rules [data]
  "Passes through all authorization rules and returns the same input data,
  only with `:violations` being populated on the case of rule violations."
  (-> data
      (assoc :violations [])
      insufficient-limit-rule
      card-not-active-rule
      high-frequency-small-interval-rule
      doubled-transaction-rule))

(defn authorize [data]
  "Authorizes `:transaction` map AND subtracts `:amount` from `:account` if no violations happened."
  (if (empty? (:violations data))
    (-> data
        (update-in [:account :available-limit] #(- % (get-in data [:transaction :amount])))
        (assoc-in [:transaction :authorized] true))
    (assoc-in data [:transaction :authorized] false)))

(def clean-unecessary-fields
  "Removes `:transaction` and `:last-two-transactions` fields of a map."
  (partial #(dissoc % :transaction :last-two-transactions)))

(defn authorize-transaction [data]
  "Authorizes transaction if all rules are not violated returning the account with violations."
  (-> data
      apply-authorization-rules
      authorize
      clean-unecessary-fields))
