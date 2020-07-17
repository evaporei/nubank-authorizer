(ns nubank-authorizer.controller-test
  (:require [clojure.test :refer :all]
            [nubank-authorizer.controller :refer :all]
            [nubank-authorizer.in-memory-storage :refer [new-in-memory-storage]]))

(deftest integration-create-account
  (testing "Should perform all account operations and return JSON"
    (let [account "{\"account\":{\"activeCard\":true,\"availableLimit\":100}}"
          expected "{\"account\":{\"activeCard\":true,\"availableLimit\":100},\"violations\":[]}\n"]
      (is (= (controller! (new-in-memory-storage) account) expected)))))

(deftest integration-authorize-transaction
  (testing "Should perform all transaction operations and return JSON"
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
