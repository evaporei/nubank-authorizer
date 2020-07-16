(ns nubank-authorizer.business-logic)

(defn create-account [db-account input-data]
  "Receives the database account and the user input account,
  if the database already has an account, it will add a key `:violations`
  to the `input-data` with the `:account-already-initialized` in a list."
  (if (nil? db-account)
    input-data
    (assoc input-data :violations [:account-already-initialized])))

(defn authorize-transaction [data]
  data)
