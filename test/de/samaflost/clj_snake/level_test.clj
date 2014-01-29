(ns de.samaflost.clj-snake.level-test
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.level :refer :all]))

(deftest creation-of-level
  (testing "frame-walls"
    (is (some #{[0 0]} (:walls (create-level))))
    (is (some #{[0 49]} (:walls (create-level))))
    (is (some #{[0 10]} (:walls (create-level))))
    (is (some #{[10 0]} (:walls (create-level))))
    (is (some #{[49 0]} (:walls (create-level))))
    (is (some #{[49 10]} (:walls (create-level))))
    (is (some #{[49 49]} (:walls (create-level))))
    (is (not (some #{[10 10]} (:walls (create-level)))))
    (is (not (some #{[25 0]} (:walls (create-level)))))
    (is (not (some #{[25 49]} (:walls (create-level))))))
  (testing "doors"
    (is :open (:bottom-door (create-level)))))

(deftest is-open?
  (testing "bottom-door"
    (is (door-is-open? {:bottom-door :open} bottom-door))
    (is (not (door-is-open? {:bottom-door :closed} bottom-door))))
  (testing "top-door"
    (is (door-is-open? {:top-door :open} top-door))
    (is (not (door-is-open? {:top-door :closed} top-door)))))

(deftest open-close-test
  (testing "bottom-door"
    (is (= :open (:bottom-door
                  (open-close {:bottom-door :closed} bottom-door :open))))
    (is (= :closed (:bottom-door
                    (open-close {:bottom-door :closed} bottom-door :closed)))))
  (testing "top-door"
    (is (= :open (:top-door
                  (open-close {:top-door :closed} top-door :open))))
    (is (= :closed (:top-door
                    (open-close {:top-door :closed} top-door :closed))))))

