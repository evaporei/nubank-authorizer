(ns nubank-authorizer.controller-test
  (:require [clojure.test :refer :all]
            [nubank-authorizer.controller :refer :all]
            [nubank-authorizer.database :as db]
            [nubank-authorizer.in-memory-storage :refer [new-in-memory-storage]]))

(deftest create-account-controller
  (testing "Should perform all account operations and save it on storage"
    (let [account1 {:active-card true
                    :available-limit 100}
          account2 {:active-card false
                    :available-limit 90}
          storage (new-in-memory-storage)
          expected1 {:account account1
                     :violations []}
          expected2 {:account account2
                     :violations [:account-already-initialized]}]
      (is (= (create-account! storage {:account account1}) expected1))
      (is (= (db/get-account storage) account1))
      (is (= (create-account! storage {:account account2}) expected2))
      (is (= (db/get-account storage) account1)))))

(deftest authorize-transaction-controller
  (testing "Should perform all account operations and save it on storage"
    (let [initial-account {:active-card true
                           :available-limit 100}
          storage (new-in-memory-storage {:account initial-account})
          input-data {:transaction {:merchant "Burger King"
                                    :amount 20
                                    :time "2019-02-13T10:00:00.000Z"}}
          expected-account {:active-card true
                            :available-limit 80}
          expected-output {:account expected-account
                           :violations []}
          expected-transactions [{:merchant "Burger King"
                                  :amount 20
                                  :time "2019-02-13T10:00:00.000Z"
                                  :authorized true}]]
      (is (= (authorize-transaction! storage input-data) expected-output))
      (is (= (db/get-account storage) expected-account))
      (is (= (db/get-transactions storage) expected-transactions)))))

(deftest controller-integration-create-account-and-authorize-transaciton
  (testing "Should perform all account and transaction operations and return JSON"
    (let [storage (new-in-memory-storage)
          account "{\"account\":{\"activeCard\":true,\"availableLimit\":100}}"
          expected-operation1 "{\"account\":{\"activeCard\":true,\"availableLimit\":100},\"violations\":[]}\n"
          transaction "{\"transaction\":{\"merchant\":\"Burger King\",\"amount\":20,\"time\":\"2019-02-13T10:00:00.000Z\"}}"
          expected-operation2 "{\"account\":{\"activeCard\":true,\"availableLimit\":80},\"violations\":[]}\n"]
      (is (= (controller! storage account) expected-operation1))
      (is (= (controller! storage transaction) expected-operation2)))))

(deftest integration-else
  (testing "Should perform nothing and return an empty String"
    (is (= (controller! (new-in-memory-storage) "{\"nothing\":\"related\"}") ""))))

(deftest routing-create-account
  (testing "Should return create-account! along with input-data"
    (is (= (routing {:account {}}) [create-account! {:account {}}]))))

(deftest routing-authorize-transaction
  (testing "Should return authorize-transaction! along with input-data"
    (is (= (routing {:transaction {}}) [authorize-transaction! {:transaction {}}]))))

(deftest routing-else
  (testing "Should return fn that returns nil along with input-data"
    (let [[controller-fn data] (routing {:nothing :related})]
      (is (= (controller-fn) nil))
      (is (= data {:nothing :related})))))

(deftest execute-controller-with-fn
  (testing "Should return fn passed with storage and input-data"
    (let [result (execute-controller! {:storage {}} [(constantly {:a :b})])]
      (is (= result {:a :b})))))
