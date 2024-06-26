(ns nubank-authorizer.business-logic-test
  (:require [clojure.test :refer [deftest is testing]]
            [nubank-authorizer.business-logic :refer [apply-authorization-rules
                                                      authorize
                                                      card-not-active-rule
                                                      create-account
                                                      doubled-transaction-rule
                                                      has-same-payload?
                                                      high-frequency-small-interval-rule
                                                      insufficient-limit-rule
                                                      is-within-two-minutes?
                                                      first-transaction-exceeded-limit-rule]]))

(deftest create-account-empty-database
  (testing "Should return the input when database hasn't an account"
    (let [account {:account {:active-card true
                             :available-limit 100}}
          expected {:account {:active-card true
                              :available-limit 100}
                    :violations []}]
      (is (= (create-account nil account) expected)))))

(deftest create-account-non-empty-database
  (testing "Should return the input with `:violations` when database already has account"
    (let [db-account {:active-card true
                      :available-limit 100}
          account {:account {:active-card false
                             :available-limit 350}}
          expected {:account {:active-card true
                              :available-limit 100}
                    :violations [:account-already-initialized]}]
      (is (= (create-account db-account account) expected)))))

(deftest insufficient-limit-rule-with-limit
  (testing "Should return input if it has enough limit"
    (let [account {:active-card true
                   :available-limit 100}
          transaction {:merchant "Habbib's"
                       :amount 20
                       :time "2019-02-13T11:00:00.000Z"}
          data {:account account
                :transaction transaction
                :violations []}]
      (is (= (insufficient-limit-rule data) data)))))

(deftest has-not-enough-limit-with-limit
  (testing "Should return violation when it has NOT enough limit"
    (let [account {:active-card true
                   :available-limit 100}
          transaction {:merchant "Habbib's"
                       :amount 120
                       :time "2019-02-13T11:00:00.000Z"}
          data {:account account
                :transaction transaction
                :violations []}
          expected {:account account
                    :transaction transaction
                    :violations [:insufficient-limit]}]
      (is (= (insufficient-limit-rule data) expected)))))

(deftest card-not-active-rule-with-active-card
  (testing "Should return input the card is active"
    (let [account {:active-card true
                   :available-limit 100}
          data {:account account
                :violations []}]
      (is (= (card-not-active-rule data) data)))))

(deftest card-not-active-rule-with-inactive-card
  (testing "Should return violation input the card is NOT active"
    (let [account {:active-card false
                   :available-limit 100}
          data {:account account
                :violations []}
          expected {:account account
                    :violations [:card-not-active]}]
      (is (= (card-not-active-rule data) expected)))))

(deftest is-within-two-minutes-within-interval
  (testing "Should return true when in interval"
    (let [trx1 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:01:30.000Z"}
          trx2 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:00.000Z"}]
      (is (= (is-within-two-minutes? trx1 trx2) true)))))

(deftest is-within-two-minutes-out-of-interval
  (testing "Should return true when in interval"
    (let [trx1 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:02:15.000Z"}
          trx2 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:00.000Z"}]
      (is (= (is-within-two-minutes? trx1 trx2) false)))))

(deftest high-frequency-small-interval-rule-with-low-frequency
  (testing "Should return input when frequency is low"
    (let [new-trx {:merchant "Habbib's"
                   :amount 120
                   :time "2019-02-13T11:02:50.000Z"}
          trx1 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:02:20.000Z"}
          trx2 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:30.000Z"}
          trx3 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:00.000Z"}
          data {:transaction new-trx
                :last-three-transactions [trx1 trx2 trx3]
                :violations []}]
      (is (= (high-frequency-small-interval-rule data) data)))))

(deftest high-frequency-small-interval-rule-with-high-frequency
  (testing "Should return input when frequency is high"
    (let [new-trx {:merchant "Habbib's"
                   :amount 120
                   :time "2019-02-13T11:01:30.000Z"}
          trx1 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:01:20.000Z"}
          trx2 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:30.000Z"}
          trx3 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:00.000Z"}
          data {:transaction new-trx
                :last-three-transactions [trx1 trx2 trx3]
                :violations []}
          expected {:transaction new-trx
                    :last-three-transactions [trx1 trx2 trx3]
                    :violations [:high-frequency-small-interval]}]
      (is (= (high-frequency-small-interval-rule data) expected)))))

(deftest has-same-payload-with-same-payload
  (testing "Should return true when payload is the same"
    (let [trx1 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:00.000Z"}
          trx2 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:00.000Z"}]
      (is (= (has-same-payload? trx1 trx2) true)))))

(deftest has-same-payload-with-different-payload
  (testing "Should return true when payload is NOT the same"
    (let [trx1 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:00.000Z"}
          trx2 {:merchant "Habbib's"
                :amount 60
                :time "2019-02-13T11:00:00.000Z"}]
      (is (= (has-same-payload? trx1 trx2) false)))))

(deftest is-doubled-transaction-with-different-transactions
  (testing "Should return input when transactions are different"
    (let [new-trx {:merchant "Bob's"
                   :amount 60
                   :time "2019-02-13T11:02:20.000Z"}
          trx1 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:15.000Z"}
          trx2 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:00.000Z"}
          data {:transaction new-trx
                :last-three-transactions [trx1 trx2]
                :violations []}]
      (is (= (doubled-transaction-rule data) data)))))

(deftest is-doubled-transaction-with-the-same-transaction
  (testing "Should return violation when transactions are the same"
    (let [new-trx {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:02:20.000Z"}
          trx1 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:02:20.000Z"}
          trx2 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:15.000Z"}
          data {:transaction new-trx
                :last-three-transactions [trx1 trx2]
                :violations []}
          expected {:transaction new-trx
                    :last-three-transactions [trx1 trx2]
                    :violations [:doubled-transaction]}]
      (is (= (doubled-transaction-rule data) expected)))))

(deftest apply-authorization-rules-with-multiple-rules
  (testing "Should return violations when multiple rules are wrong"
    (let [account {:active-card false
                   :available-limit 120}
          new-trx {:merchant "Habbib's"
                   :amount 120
                   :time "2019-02-13T11:02:14.000Z"}
          trx1 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:01:14.000Z"}
          trx2 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:15.000Z"}
          trx3 {:merchant "Habbib's"
                :amount 120
                :time "2019-02-13T11:00:15.000Z"}
          data {:account account
                :transaction new-trx
                :last-three-transactions [trx1 trx2 trx3]
                :violations []}
          expected {:account account
                    :transaction new-trx
                    :last-three-transactions [trx1 trx2 trx3]
                    :violations [:card-not-active :high-frequency-small-interval :doubled-transaction]}]
      (is (= (apply-authorization-rules data) expected)))))

(deftest authorize-without-rules
  (testing "Should return input with transaction authorized"
    (let [account {:active-card true
                   :available-limit 120}
          transaction {:merchant "Habbib's"
                       :amount 120
                       :time "2019-02-13T11:00:15.000Z"}
          data {:account account
                :transaction transaction
                :violations []}
          expected {:account {:active-card true
                              :available-limit 0}
                    :transaction {:merchant "Habbib's"
                                  :amount 120
                                  :time "2019-02-13T11:00:15.000Z"
                                  :authorized true}
                    :violations []}]
      (is (= (authorize data) expected)))))

(deftest authorize-with-violations
  (testing "Should return input with transaction authorized"
    (let [account {:active-card false
                   :available-limit 90}
          transaction {:merchant "Habbib's"
                       :amount 120
                       :time "2019-02-13T11:00:15.000Z"}
          data {:account account
                :transaction transaction
                :violations [:card-not-active :insufficient-limit]}
          expected {:account account
                    :transaction {:merchant "Habbib's"
                                  :amount 120
                                  :time "2019-02-13T11:00:15.000Z"
                                  :authorized false}
                    :violations [:card-not-active :insufficient-limit]}]
      (is (= (authorize data) expected)))))

(deftest first-transaction-exceeded-limit-rule-test
  (testing "When it is the first transaction"
    (testing "And the limit exceeds"
      (let [account {:active-card true
                     :available-limit 100}
            new-trx {:merchant "Habbib's"
                     :amount 95
                     :time "2019-02-13T11:02:14.000Z"}
            data {:account account
                  :transaction new-trx
                  :first-transaction? true
                  :violations []}
            expected {:account account
                      :transaction new-trx
                      :first-transaction? true
                      :violations [:first-transaction-exceeded-limit]}]
      (is (= (first-transaction-exceeded-limit-rule data) expected))))
    (testing "And the limit does NOT exceed"
      (let [account {:active-card true
                     :available-limit 100}
            new-trx {:merchant "Habbib's"
                     :amount 90
                     :time "2019-02-13T11:02:14.000Z"}
            data {:account account
                  :transaction new-trx
                  :first-transaction? true
                  :violations []}
            expected {:account account
                      :transaction new-trx
                      :first-transaction? true
                      :violations []}]
      (is (= (first-transaction-exceeded-limit-rule data) expected)))))
  (testing "When it is NOT the first transaction"
    (let [account {:active-card true
                   :available-limit 100}
          new-trx {:merchant "Habbib's"
                   :amount 95
                   :time "2019-02-13T11:02:14.000Z"}
          data {:account account
                :transaction new-trx
                :first-transaction? false
                :violations []}
          expected {:account account
                    :transaction new-trx
                    :first-transaction? false
                    :violations []}]
    (is (= (first-transaction-exceeded-limit-rule data) expected)))))
