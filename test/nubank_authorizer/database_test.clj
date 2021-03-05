(ns nubank-authorizer.database-test
  (:require [clojure.test :refer [deftest is testing]]
            [nubank-authorizer.database :as db]
            [nubank-authorizer.storage :as storage]
            [nubank-authorizer.in-memory-storage :refer [new-in-memory-storage]]))

(deftest create-account-simple
  (testing "Should create an account in storage"
    (let [storage (new-in-memory-storage)
          account {:active-card true
                   :available-limit 100}]
      (is (= (db/create-account storage account) {:account account}))
      (is (= (storage/get-key storage :account) account)))))

(deftest get-account-simple
  (testing "Should get an existing account in storage"
    (let [account {:active-card true
                   :available-limit 100}
          storage (new-in-memory-storage {:account account})]
      (is (= (db/get-account storage) account))
      (is (= (storage/get-key storage :account) account)))))

(deftest create-transaction-without-previous-transactions
  (testing "Should add an transaction in storage"
    (let [storage (new-in-memory-storage)
          transaction {:merchant "Burger King"
                       :amount 20
                       :time "2019-02-13T10:00:00.000Z"
                       :authorized true}]
      (is (= (db/create-transaction storage transaction) transaction))
      (is (= (storage/get-key storage :transactions) [transaction])))))

(deftest create-transaction-with-previous-transactions
  (testing "Should add an transaction along with previous ones in storage"
    (let [trx1 {:merchant "Burger King"
                :amount 20
                :time "2019-02-13T10:00:00.000Z"
                :authorized true}
          storage (new-in-memory-storage {:transactions [trx1]})
          trx2 {:merchant "Habbib's"
                :amount 90
                :time "2019-02-13T10:00:00.000Z"
                :authorized false}]
      (is (= (db/create-transaction storage trx2) trx2))
      (is (= (storage/get-key storage :transactions) [trx1 trx2])))))

(deftest get-transactions-when-empty
  (testing "Should get transactions when empty in storage"
    (let [storage (new-in-memory-storage)]
      (is (= (db/get-transactions storage) []))
      (is (= (storage/has-key? storage :transactions) false)))))

(deftest get-transactions-when-not-empty
  (testing "Should get transactions when NOT empty in storage"
    (let [trx1 {:merchant "Burger King"
                :amount 20
                :time "2019-02-13T10:00:00.000Z"
                :authorized true}
          trx2 {:merchant "Habbib's"
                :amount 90
                :time "2019-02-13T10:00:00.000Z"
                :authorized false}
          storage (new-in-memory-storage {:transactions [trx1 trx2]})]
      (is (= (db/get-transactions storage) [trx1 trx2]))
      (is (= (storage/get-key storage :transactions) [trx1 trx2])))))
