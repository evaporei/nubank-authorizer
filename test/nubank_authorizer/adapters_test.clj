(ns nubank-authorizer.adapters-test
  (:require [clojure.test :refer [deftest is testing]]
            [nubank-authorizer.adapters :refer [json-to-edn edn-to-json]]))

(deftest json-to-edn-simple
  (testing "Should return simple EDN structure"
    (let [json "{\"veryCool\":\"data\"}"
          expected-edn {:very-cool "data"}]
      (is (= (json-to-edn json) expected-edn)))))

(deftest edn-to-json-simple
  (testing "Should return simple JSON String"
    (let [edn {:very-cool "data"}
          expected-json-str "{\"veryCool\":\"data\"}\n"]
      (is (= (edn-to-json edn) expected-json-str)))))
