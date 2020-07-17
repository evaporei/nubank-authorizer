(ns nubank-authorizer.storage)

(defprotocol Storage
  "Protocol for saving/retrieving data from a Storage."
  (insert-key! [this new-key new-value] "Inserts value in key of Storage.")
  (update-key! [this k update-fn] "Updates via custom fn a key's value of Storage.")
  (get-key [this k] "Gets key of Storage.")
  (get-key-with-default [this k default] "Gets key with default of Storage.")
  (has-key? [this k] "Checks if key exists in Storage."))
