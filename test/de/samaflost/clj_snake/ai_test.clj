(ns de.samaflost.clj-snake.ai-test
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.game-test :as gt]
            [de.samaflost.clj-snake.ai :refer :all]))

(deftest random-direction-choice
  (testing "the expected three directions are present when snake has a tail"
    (is (every? #{:left :right :up}
                (choose-directions {:strategy :random :direction :up
                                    :body [1 2]} nil)))
    (is (every? (set (choose-directions {:strategy :random :direction :up
                                         :body [1 2]} nil))
                [:left :right :up]))
    (is (= 3 (count (choose-directions {:strategy :random :direction :up
                                        :body [1 2]} nil)))))
  (testing "but all directions are possible if there is a lone head"
    (is (= 4 (count (choose-directions {:strategy :random :direction :up
                                        :body [1]} nil))))))

(deftest clockwise-direction-choice
  (testing "the expected four directions are present"
    (is (= [:up :right :down :left]
           (choose-directions {:strategy :clockwise :direction :rigth} nil)))))

(deftest picking-direction
  (testing "takes first choice when possible"
    (is (= :up (pick-direction {:body [[10 2] [10 3]]
                                :direction :right
                                :to-grow 0
                                :strategy :clockwise}
                               (gt/base-state)))))
  (testing "looks for a better choice when way is blocked"
    ;; 0 /-\
    ;; 1 -\|
    ;; 2 o-|
    ;; 3 --/
    (is (= :left (pick-direction {:body [[10 2] [11 2] [11 1] [10 1]
                                         [10 0] [11 0] [12 0] [12 1]
                                         [12 2] [12 3] [11 3] [10 3]]
                                  :direction :left
                                  :to-grow 0
                                  :strategy :clockwise}
                                 (gt/base-state)))))
  (testing "returns nil if there is no way to go"
    ;; 0 |/-\
    ;; 1 |-\|
    ;; 2 |o-|
    ;; 3 \--/
    (is (nil? (pick-direction {:body [[10 2] [11 2] [11 1] [10 1]
                                      [10 0] [11 0] [12 0] [12 1]
                                      [12 2] [12 3] [11 3] [10 3]
                                      [9 3] [9 2] [9 1] [9 0]]
                               :direction :left
                               :to-grow 0
                               :strategy :clockwise}
                              (gt/base-state))))))

(deftest walking
  (testing "moves into taken direction"
    (is (= {:direction :up :body [[10 1] [10 2]] :to-grow 0 :strategy :clockwise}
           (walk {:body [[10 2] [10 3]]
                  :direction :right
                  :to-grow 0
                  :strategy :clockwise}
                 (gt/base-state)))))
  (testing "just shrinks if there is no way to go"
    ;; 0 |/-\
    ;; 1 |-\|
    ;; 2 |o-|
    ;; 3 \--/
    (is (= {:direction :left
            :body [[10 2] [11 2] [11 1] [10 1]
                   [10 0] [11 0] [12 0] [12 1]
                   [12 2] [12 3] [11 3] [10 3]
                   [9 3] [9 2] [9 1]]
            :to-grow 0
            :strategy :clockwise}
           (walk {:body [[10 2] [11 2] [11 1] [10 1]
                         [10 0] [11 0] [12 0] [12 1]
                         [12 2] [12 3] [11 3] [10 3]
                         [9 3] [9 2] [9 1] [9 0]]
                  :direction :left
                  :to-grow 0
                  :strategy :clockwise}
                 (gt/base-state)))))
    (testing "keeps head when stuck"
      ;;    /======
      ;; ====o-
      ;;WWWWWWWWWWWWW
      (is (= {:direction :left
              :body [[10 2]]
              :to-grow 0
              :strategy :clockwise}
             (walk {:body [[10 2] [11 2]]
                    :to-grow 0
                    :direction :left
                    :strategy :clockwise}
                   (assoc (gt/base-state)
                     :player
                     (ref
                      {:body [[8 2] [9 2] [9 1] [10 1] [11 1]]})
                     :level
                     (ref
                      {:walls [[8 3] [9 3] [10 3] [11 3]]
                       :type :level}))))))
    (testing "and turns around when unstuck through shrinking"
      ;;    /======
      ;; ====o
      ;;WWWWWWWWWWWWW
      (is (= {:direction :right
              :body [[11 2]]
              :to-grow 0
              :strategy :clockwise}
             (walk {:body [[10 2]]
                    :to-grow 0
                    :direction :left
                    :strategy :clockwise}
                   (assoc (gt/base-state)
                     :player
                     (ref
                      {:body [[8 2] [9 2] [9 1] [10 1] [11 1]]})
                     :level
                     (ref
                      {:walls [[8 3] [9 3] [10 3] [11 3]]
                       :type :level})))))
      ;; trapped in corner
      (is (= {:body [[1 2]], :direction :down, :to-grow 0, 
              :strategy :clockwise}
             (walk {:body [[1 1]]
                    :to-grow 0
                    :direction :up
                    :strategy :clockwise}
                   (assoc (gt/base-state)
                     :player
                     (ref
                      {:body [[3 1] [2 1] [2 2] [2 3]]})
                     :level
                     (ref
                      {:walls [[0 3] [0 2] [0 1] [0 0] [1 0] [2 0] [3 0]]
                       :type :level})))))))

(deftest closest-test
  (testing "closest picks closest apple"
    (is (= {:location [0 0]}
           (closest {:location [1 1]}
                          [{:location [3 1]} {:location [0 0]}
                           {:location [1 3]}])))))

(deftest greedy-direction-choice
  (testing "tries to reach closest apple"
    (is (= :up (first (choose-directions {:direction :right
                                          :body [[2 2]]
                                          :strategy :greedy}
                                         {:apples
                                          (ref
                                           [{:location [0 0]}
                                            {:location [3 0]}])}))))
    (is (= :right (second (choose-directions {:direction :right
                                              :body [[2 2]]
                                              :strategy :greedy}
                                             {:apples
                                              (ref
                                               [{:location [0 0]}
                                                {:location [3 0]}])})))))
  (testing "returns random choice if there are no apples"
    (is (= 3 (count (choose-directions {:direction :right
                                        :body [[2 2] [1 2]]
                                        :strategy :greedy}
                                       {:apples (ref [])}))))))

(deftest aggressive-direction-choice
  (testing "tries to reach player"
    (is (= :up (first (choose-directions {:direction :right
                                          :body [[2 2]]
                                          :to-grow 0
                                          :strategy :aggressive}
                                         {:player
                                          (ref {:body [[2 0] [1 0]]
                                                :direction :right
                                                :to-grow 0})}))))
    (is (= :right (second (choose-directions {:direction :right
                                              :body [[2 2]]
                                              :to-grow 0
                                              :strategy :aggressive}
                                             {:player
                                              (ref {:body [[2 0] [1 0]]
                                                    :direction :right
                                                    :to-grow 0})}))))))
