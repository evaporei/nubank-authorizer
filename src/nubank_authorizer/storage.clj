(ns nubank-authorizer.storage)

(defprotocol Storage
  (insert-key! [this new-key new-value])
  (update-key! [this k update-fn])
  (get-key [this k])
  (get-key-with-default [this k default])
  (has-key? [this k]))
