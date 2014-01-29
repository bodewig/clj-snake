(ns de.samaflost.clj-snake.collision-detection-test
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.collision-detection :refer :all]
            [de.samaflost.clj-snake.level
             :refer [bottom-door door-is-open? top-door]]))

(deftest colliding-with-location
  (testing "collisions with something that has a location"
    (is (collide? {:location [1 2]} {:location [1 2]}))
    (is (not (collide? {:location [1 2]} {:location [0 2]})))
    (is (not (collide? {:location [1 2]} {:location [1 3]})))
    (is (not (collide? {:location [1 2]} {:location []})))))

(deftest colliding-with-a-body
  (testing "collisions with something that has a body"
    (is (collide? {:location [1 2]}
                  {:body [[0 0] [1 2] [0 0]]}))
    (is (collide? {:location [1 2]}
                  {:body (list [0 0] [1 2] [0 0])}))
    (is (not (collide? {:location [1 2]}
                       {:body [[0 0] [1 3] [0 2]]})))
    (is (not (collide? {:location [1 2]} 
                       {:body []})))))

(deftest colliding-with-a-level
  (testing "collisions with the wall"
    (is (collide? {:location [1 2]}
                  {:walls [[0 0] [1 2] [0 0]]
                   :top-door :open
                   :bottom-door :open
                   :type :level}))
    (is (collide? {:location [1 2]}
                  {:walls (list [0 0] [1 2] [0 0])
                   :top-door :open
                   :bottom-door :open
                   :type :level}))
    (is (not (collide? {:location [1 2]}
                       {:walls [[0 0] [1 3] [0 2]]
                        :top-door :open
                        :bottom-door :open
                        :type :level})))
    (is (not (collide? {:location [1 2]} 
                       {:walls []
                        :top-door :open
                        :bottom-door :open
                        :type :level}))))
  (testing "collisions with the top door"
    (is (collide? {:location top-door}
                  {:walls [[0 0] [1 2] [0 0]]
                   :top-door :closed
                   :bottom-door :open
                   :type :level}))
    (is (not (collide? {:location top-door}
                       {:walls [[0 0] [1 2] [0 0]]
                        :top-door :open
                        :bottom-door :open
                        :type :level})))
    (is (collide? {:location [1 2]}
                  {:walls [[0 0] [1 2] [0 0]]
                   :top-door :closed
                   :bottom-door :open
                   :type :level})))
  (testing "collisions with the bottom door"
    (is (collide? {:location bottom-door}
                  {:walls [[0 0] [1 2] [0 0]]
                   :top-door :open
                   :bottom-door :closed
                   :type :level}))
    (is (not (collide? {:location bottom-door}
                       {:walls [[0 0] [1 2] [0 0]]
                        :top-door :open
                        :bottom-door :open
                        :type :level})))
    (is (collide? {:location [1 2]}
                  {:walls [[0 0] [1 2] [0 0]]
                   :top-door :open
                   :bottom-door :closed
                   :type :level}))))

