(ns de.samaflost.clj-snake.util-test
  (:import (java.awt Color))
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.util :refer :all]))

(deftest distinct-location-test
  (testing "removes duplicate locations"
    (is (= [{:location [1 1]} {:location [1 2]}]
           (distinct-location [{:location [1 1]} {:location [1 2]}])))
    (is (= [{:location [1 1]} {:location [1 2]}]
           (distinct-location [{:location [1 1]} {:location [1 2]}
                               {:location [1 1]}])))))
