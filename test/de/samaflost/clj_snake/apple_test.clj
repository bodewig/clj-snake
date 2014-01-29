(ns de.samaflost.clj-snake.apple-test
  (:import (java.awt Color))
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.apple :refer :all]))

(deftest removing
  (testing "Removing of apples"
    (is (= [:a :b :c] (remove-apple [:a :b :c :d] :d)))
    (is (= [:a :b :c] (remove-apple [:a :b :c] :d)))))

(deftest aging
  (testing "apples lose nutrition and eventually vanish"
    (is (= 2 (:remaining-nutrition
              (first (age [{:remaining-nutrition 3}] [])))))
    (is (= 1 (count
              (age [{:remaining-nutrition 3} {:remaining-nutrition 1}] [])))))
  (testing "new apples are generated when the last one is rotten"
    (is (= 5 (count
              (age [{:remaining-nutrition 1}] [])))))
  (testing "apples change color when rotting"
    (is (= Color/RED (:color
                      (first (age [{:remaining-nutrition 500}] [])))))
    (is (= Color/RED (:color
                      (first (age [{:remaining-nutrition 252}] [])))))
    (is (= Color/YELLOW (:color
                      (first (age [{:remaining-nutrition 251}] [])))))
    (is (= Color/YELLOW (:color
                         (first (age [{:remaining-nutrition 2}] [])))))))

(deftest initial-apples-test
  (testing "potentially long running test until rand has found the few eligible places"
    (let [places-taken [{:body (map vector
                                    (flatten (repeat 40 (range 0 50)))
                                    (repeatedly #(rand-int 50)))}
                        {:location [(rand-int 50) (rand-int 50)]}]]
      (is (= 2000 (count (:body (first places-taken)))))
      (is (= 5 (count (initial-apples places-taken))))
      (is (not (some (set (:body (first places-taken)))
                     (map :location (initial-apples places-taken)))))
      (is (not (some (set (:location (second places-taken)))
                     (map :location (initial-apples places-taken))))))))
