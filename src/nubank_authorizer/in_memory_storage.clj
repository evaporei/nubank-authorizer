(ns nubank-authorizer.in-memory-storage
  (:require [nubank-authorizer.storage :refer [Storage]]))

(defrecord InMemoryStorage [storage]
  Storage
  (insert-key! [_this new-key new-value]
    (swap! storage assoc new-key new-value))
  (update-key! [_this k update-fn]
    (swap! storage update-in [k] update-fn))
  (get-key [_this k]
    (k @storage))
  (get-key-with-default [_this k default]
    (k @storage default))
  (has-key? [_this k]
    (contains? @storage k)))

(defn new-in-memory-storage
  "Creates a new InMemoryStorage with an atom for concurrency safety."
  ([]
   (new-in-memory-storage {}))
  ([initial-storage]
   (->InMemoryStorage (atom initial-storage))))
