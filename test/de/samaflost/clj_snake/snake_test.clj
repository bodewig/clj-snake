(ns de.samaflost.clj-snake.snake-test
  (:import (java.awt Color))
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.snake :refer :all]))

(deftest snake-creation
  (testing "Creating a new Snake"
    (testing "for the player"
      (is (= (list [25 49]) (:body (new-snake true))))
      (is (= :up (:direction (new-snake true))))
      (is (= Color/GREEN (:color (new-snake true)))))
    (testing "for the AI"
      (is (= (list [25 0]) (:body (new-snake false))))
      (is (= :down (:direction (new-snake false))))))
      (is (= Color/BLUE (:color (new-snake false)))))

(deftest moving-the-snake
  (testing "moving"
    (testing "without growing"
      (is (= (list [25 48]) (:body (move (assoc (new-snake true)
                                           :to-grow 0)))))
      (is (= (list [25 50]) (:body (move (assoc (new-snake true)
                                           :direction :down
                                           :to-grow 0)))))
      (is (= (list [24 49]) (:body (move (assoc (new-snake true)
                                           :direction :left
                                           :to-grow 0)))))
      (is (= (list [26 49]) (:body (move (assoc (new-snake true)
                                           :direction :right
                                           :to-grow 0)))))))
    (testing "with growing"
      (is (= (list [25 48] [25 49]) (:body (move (new-snake true)))))
      (is (= (list [25 50] [25 49])
             (:body (move (assoc (new-snake true)
                            :direction :down)))))
      (is (= (list [24 49] [25 49])
             (:body (move (assoc (new-snake true)
                            :direction :left)))))
      (is (= (list [26 49] [25 49])
             (:body (move (assoc (new-snake true)
                            :direction :right))))))
    (testing "changes in to-grow"
      (is (= 3 (:to-grow (move (new-snake true)))))
      (is (= 0 (:to-grow (move (assoc (new-snake true)
                                 :to-grow 1)))))
      (is (= 0 (:to-grow (move (assoc (new-snake true)
                                 :to-grow -1)))))
      (is (= 41 (:to-grow (move (assoc (new-snake true)
                                  :to-grow 42)))))))

(deftest changing-direction
  (testing "changing from :up"
    (is (= :up (:direction (change-direction {:direction :up} :up))))
    (is (= :left (:direction (change-direction {:direction :up} :left))))
    (is (= :up (:direction (change-direction {:direction :up} :down))))
    (is (= :right (:direction (change-direction {:direction :up} :right)))))
  (testing "changing from :left"
    (is (= :up (:direction (change-direction {:direction :left} :up))))
    (is (= :left (:direction (change-direction {:direction :left} :left))))
    (is (= :down (:direction (change-direction {:direction :left} :down))))
    (is (= :left (:direction (change-direction {:direction :left} :right)))))
  (testing "changing from :down"
    (is (= :down (:direction (change-direction {:direction :down} :up))))
    (is (= :left (:direction (change-direction {:direction :down} :left))))
    (is (= :down (:direction (change-direction {:direction :down} :down))))
    (is (= :right (:direction (change-direction {:direction :down} :right)))))
  (testing "changing from :right"
    (is (= :up (:direction (change-direction {:direction :right} :up))))
    (is (= :right (:direction (change-direction {:direction :right} :left))))
    (is (= :down (:direction (change-direction {:direction :right} :down))))
    (is (= :right (:direction (change-direction {:direction :right} :right))))))

(deftest consuming-apples
  (testing "consume"
    (is (= {:to-grow 13} (consume {:to-grow 12} {:remaining-nutrition 25})))))

(deftest head-test
  (testing "head"
    (is (= {:location [1 2]} (head {:body [[1 2] [3 4] [5 6]]})))
    (is (= {:location [1 2]} (head {:body [[1 2]]})))
    (is (= {:location nil} (head {:body []})))))

(deftest tail-test
  (testing "tail"
    (is (= {:body [[3 4] [5 6]]} (tail {:body [[1 2] [3 4] [5 6]]})))
    (is (= {:body nil} (tail {:body [[1 2]]})))
    (is (= {:body nil} (tail {:body []})))))
