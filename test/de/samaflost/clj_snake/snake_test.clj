(ns de.samaflost.clj-snake.snake-test
  (:import (java.awt Color))
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.config :refer [snake-configuration]]
            [de.samaflost.clj-snake.snake :refer :all]))

(def half-width (/ (get-in @snake-configuration [:board-size :width]) 2))

(deftest snake-creation
  (testing "Creating a new Snake"
    (testing "for the player"
      (is (= (list [half-width
                    (dec (get-in @snake-configuration [:board-size :height]))])
             (:body (new-snake true))))
      (is (= :up (:direction (new-snake true))))
      (is (= Color/GREEN (:color (new-snake true)))))
    (testing "for the AI"
      (is (= (list [half-width 0]) (:body (new-snake false))))
      (is (= :down (:direction (new-snake false))))))
      (is (= Color/BLUE (:color (new-snake false)))))

(deftest moving-the-snake
  (testing "moving"
    (testing "without growing"
      (is (= (list [half-width
                    (dec (dec (get-in @snake-configuration [:board-size :height])))])
             (:body (move (assoc (new-snake true) :to-grow 0)))))
      (is (= (list [half-width (get-in @snake-configuration [:board-size :height])])
             (:body (move (assoc (new-snake true)
                            :direction :down
                            :to-grow 0)))))
      (is (= (list [(dec half-width)
                    (dec (get-in @snake-configuration [:board-size :height]))])
             (:body (move (assoc (new-snake true)
                            :direction :left
                            :to-grow 0)))))
      (is (= (list [(inc half-width)
                    (dec (get-in @snake-configuration [:board-size :height]))])
             (:body (move (assoc (new-snake true)
                            :direction :right
                            :to-grow 0)))))))
    (testing "standing still"
      (is (= (list [half-width
                    (dec (get-in @snake-configuration [:board-size :height]))])
             (:body (move (assoc (new-snake true)
                            :direction :stand
                            :to-grow 0)))))
      (is (= (list [25 48])
             (:body (move (assoc (new-snake true)
                            :body [[25 48] [25 49]]
                            :direction :stand
                            :to-grow 0)))))
      (is (= (list [half-width
                    (dec (get-in @snake-configuration [:board-size :height]))])
             (:body (move (assoc (new-snake true)
                            :direction :stand
                            :to-grow 1))))))
    (testing "with growing"
      (is (= (list [half-width
                    (dec (dec (get-in @snake-configuration [:board-size :height])))]
                   [half-width
                    (dec (get-in @snake-configuration [:board-size :height]))])
             (:body (move (new-snake true)))))
      (is (= (list [half-width (get-in @snake-configuration [:board-size :height])]
                   [half-width
                    (dec (get-in @snake-configuration [:board-size :height]))])
             (:body (move (assoc (new-snake true) :direction :down)))))
      (is (= (list [(dec half-width)
                    (dec (get-in @snake-configuration [:board-size :height]))]
                   [half-width
                    (dec (get-in @snake-configuration [:board-size :height]))])
             (:body (move (assoc (new-snake true) :direction :left)))))
      (is (= (list [(inc half-width)
                    (dec (get-in @snake-configuration [:board-size :height]))]
                   [half-width
                    (dec (get-in @snake-configuration [:board-size :height]))])
             (:body (move (assoc (new-snake true) :direction :right))))))
    (testing "changes in to-grow"
      (is (= 3 (:to-grow (move (new-snake true)))))
      (is (= 0 (:to-grow (move (assoc (new-snake true)
                                 :to-grow 1)))))
      (is (= 0 (:to-grow (move (assoc (new-snake true)
                                 :to-grow -1)))))
      (is (= 41 (:to-grow (move (assoc (new-snake true)
                                  :to-grow 42)))))))

(def base-snake {:body [[1 1]], :direction :up})

(deftest changing-direction
  (testing "changing from :up"
    (is (= :up (:direction (change-direction base-snake :up))))
    (is (= :left (:direction (change-direction base-snake :left))))
    (is (= :up (:direction (change-direction base-snake :down))))
    (is (= :right (:direction (change-direction base-snake :right)))))
  (testing "changing from :left"
    (is (= :up (:direction (change-direction (assoc base-snake :direction :left)
                                             :up))))
    (is (= :left (:direction (change-direction (assoc base-snake :direction :left)
                                               :left))))
    (is (= :down (:direction (change-direction (assoc base-snake :direction :left)
                                               :down))))
    (is (= :left (:direction (change-direction (assoc base-snake :direction :left)
                                               :right)))))
  (testing "changing from :down"
    (is (= :down (:direction (change-direction (assoc base-snake :direction :down)
                                               :up))))
    (is (= :left (:direction (change-direction (assoc base-snake :direction :down)
                                               :left))))
    (is (= :down (:direction (change-direction (assoc base-snake :direction :down)
                                               :down))))
    (is (= :right (:direction (change-direction (assoc base-snake :direction :down)
                                                :right)))))
  (testing "changing from :right"
    (is (= :up (:direction (change-direction (assoc base-snake :direction :right)
                                             :up))))
    (is (= :right (:direction (change-direction (assoc base-snake :direction :right)
                                                :left))))
    (is (= :down (:direction (change-direction (assoc base-snake :direction :right)
                                               :down))))
    (is (= :right (:direction (change-direction (assoc base-snake :direction :right)
                                                :right)))))
  (testing "doesn't change direction before leaving the door"
    (is (= :up (:direction
                (change-direction
                 (assoc base-snake
                   :body (list [half-width
                                (dec
                                 (get-in @snake-configuration [:board-size :height]))]))
                 :left))))
    (is (= :up (:direction
                (change-direction
                 (assoc base-snake
                   :body (list [half-width
                                (dec
                                 (get-in @snake-configuration [:board-size :height]))]))
                 :right))))))

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

(deftest faking-leaving
  (testing "chops tail"
    (is (= [[1 1] [2 2]] (:body (fake-leaving {:body [[1 1] [2 2] [3 3]]}))))
    (is (nil? (:body (fake-leaving {:body [[1 1]]}))))
    (is (nil? (:body (fake-leaving {:body []}))))
    (is (nil? (:body (fake-leaving {:body nil}))))))
