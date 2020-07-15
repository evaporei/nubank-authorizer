(ns nubank-authorizer.business_logic)

(defn create-account [data]
  data)

(defn authorize-transaction [data]
  data)

(defn business-logic [data]
  (cond
    (contains? data :account) (create-account data)
    (contains? data :transaction) (authorize-transaction data)
    :else nil))
