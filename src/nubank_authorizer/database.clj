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
