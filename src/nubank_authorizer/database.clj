(ns nubank-authorizer.database)

(defrecord Database [storage])

(defn new-database
  ([]
   (new-database {}))
  ([storage]
   (->Database (atom storage))))

(defn create-account! [db account]
  (swap! (:storage db) assoc :account account))

(defn get-account [db]
  (:account @(:storage db)))

(defn create-transaction! [db transaction]
  (let [storage (:storage db)]
    (if (contains? @storage :transactions)
      (swap! storage update-in [:transactions] #(conj % transaction))
      (swap! storage assoc :transactions []))))

(defn get-transactions [db]
  (:transactions @(:storage db) []))
