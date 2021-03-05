(ns nubank-authorizer.business-logic
  (:require [clj-time.core :as t]
            [clj-time.format :as f]))

(defn create-account
  "Returns input-data with `:violations` vector with it.
  In the case of `db-account` being nil, this vector will
  be populated."
  [db-account input-data]
  (if (nil? db-account)
    (assoc input-data :violations [])
    {:account db-account
     :violations [:account-already-initialized]}))

(defn insufficient-limit-rule
  "Receives `:transaction`, `:account` and `:violations` in a map
  and returns it, appending to `:violations` vector in the case
  of the transaction amount being bigger than the available account limit."
  [data]
  (let [{:keys [transaction account]} data]
    (if (> (:amount transaction) (:available-limit account))
      (update-in data [:violations] #(conj % :insufficient-limit))
      data)))

(defn card-not-active-rule
  "Receives `:account` and `:violations` in a map and returns it,
  appending to `:violations` vector in the case of the account card
  being not active."
  [data]
  (let [{:keys [account]} data]
    (if (not (:active-card account))
      (update-in data [:violations] #(conj % :card-not-active))
      data)))

(defn is-within-two-minutes?
  "Receives two transactions which their time interval will be checked
  if it is within two minutes. Note that `trx1` MUST be after `trx2`."
  [trx1 trx2]
  (if (or (nil? trx1) (nil? trx2))
    false
    (let [[date1 date2] [(:time trx1) (:time trx2)]
          interval-in-minutes (t/in-minutes (t/interval (f/parse date2) (f/parse date1)))]
      (< interval-in-minutes 2))))

(defn third [coll]
  (-> coll
      next
      next
      first))

(defn high-frequency-small-interval-rule
  "Receives `:transaction`, `:last-three-transactions` and `:violations` in a map and returns it,
  appending to `:violations` vector in the case of the last of three transactions and the new
  transaction being within two minutes."
  [data]
  (let [{:keys [transaction last-three-transactions]} data]
    (if (is-within-two-minutes? transaction (third last-three-transactions))
      (update-in data [:violations] #(conj % :high-frequency-small-interval))
      data)))

(defn has-same-payload?
  "Compares the equality of `:amount` and `:merchant` of two transactions."
  [trx1 trx2]
  (and
    (= (:amount trx1) (:amount trx2))
    (= (:merchant trx1) (:merchant trx2))))

(defn doubled-transaction-rule
  "Receives `:transaction`, `:last-three-transactions` and `:violations` in a map and returns it,
  appending to `:violations` vector in the case of the first of the last two transactions and the new
  transaction being within two minutes AND they having the same payload."
  [data]
  (let [{:keys [transaction last-three-transactions]} data]
    (if (and
          (is-within-two-minutes? transaction (first last-three-transactions))
          (has-same-payload? transaction (first last-three-transactions)))
      (update-in data [:violations] #(conj % :doubled-transaction))
      data)))

(defn apply-authorization-rules
  "Passes through all authorization rules and returns the same input data,
  only with `:violations` being populated on the case of rule violations."
  [data]
  (-> data
      (assoc :violations [])
      insufficient-limit-rule
      card-not-active-rule
      high-frequency-small-interval-rule
      doubled-transaction-rule))

(defn authorize
  "Authorizes `:transaction` map AND subtracts `:amount` from `:account` if no violations happened."
  [data]
  (if (empty? (:violations data))
    (-> data
        (update-in [:account :available-limit] #(- % (get-in data [:transaction :amount])))
        (assoc-in [:transaction :authorized] true))
    (assoc-in data [:transaction :authorized] false)))

(defn authorize-transaction
  "Authorizes transaction if all rules are not violated returning the account with violations."
  [data]
  (-> data
      apply-authorization-rules
      authorize))
