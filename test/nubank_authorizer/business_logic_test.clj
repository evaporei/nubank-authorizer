(ns nubank-authorizer.business_logic-test
  (:require [clojure.test :refer :all]
            [nubank-authorizer.business_logic :refer :all]))

(deftest create-account-empty-database
  (testing "Should return the input when database hasn't an account"
    (let [account {:account {:activeCard true
                             :availableLimit 100}}]
      (is (= (create-account nil account) account)))))

(deftest create-account-
  (testing "Should return the input with `:violations` when database already has account"
    (let [db-account {:activeCard true
                      :availableLimit 100}
          account {:account {:activeCard true
                             :availableLimit 100}}
          expected {:account {:activeCard true
                              :availableLimit 100}
                    :violations [:account-already-initialized]}]
      (is (= (create-account db-account account) expected)))))
