(ns nubank-authorizer.controller-test
  (:require [clojure.test :refer :all]
            [nubank-authorizer.controller :refer :all]
            [nubank-authorizer.database :refer [new-database]]))

(deftest integration-success
  (testing "Should perform all operations and return JSON"
    (let [account "{\"account\":{\"activeCard\":true,\"availableLimit\":100}}"]
      (is (= (controller (new-database) account) (str account "\n"))))))

(deftest integration-else
  (testing "Should perform nothing and return an empty String"
    (is (= (controller (new-database) "{\"nothing\":\"related\"}") ""))))
