(ns nubank-authorizer.database)

(def storage (atom {}))

(defn create-account! [account]
  (swap! storage assoc :account account))

(defn get-account []
  (:account @storage))
