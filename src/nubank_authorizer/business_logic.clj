(ns nubank-authorizer.business-logic)

(defn create-account [db-account input-data]
  "Checks if `db-account` is `nil`, then returns `input-data`
  with the `:violations` vector saying if the account was or not
  already initialized"
  (if (nil? db-account)
    (assoc input-data :violations [])
    (assoc input-data :violations [:account-already-initialized])))

(defn authorize-transaction [data]
  data)
