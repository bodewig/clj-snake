(ns de.samaflost.clj-snake.level-test
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.config :refer [snake-configuration]]
            [de.samaflost.clj-snake.level :refer :all]))

(def half-width (/ (get-in @snake-configuration [:board-size :width]) 2))

(deftest creation-of-level
  (testing "frame-walls"
    (is (some #{[0 0]} (:walls (create-level))))
    (is (some #{[0 (dec (get-in @snake-configuration [:board-size :height]))]}
              (:walls (create-level))))
    (is (some #{[0 10]} (:walls (create-level))))
    (is (some #{[10 0]} (:walls (create-level))))
    (is (some #{[(dec (get-in @snake-configuration [:board-size :width])) 0]}
              (:walls (create-level))))
    (is (some #{[(dec (get-in @snake-configuration [:board-size :width])) 10]}
              (:walls (create-level))))
    (is (some #{[(dec (get-in @snake-configuration [:board-size :width]))
                 (dec (get-in @snake-configuration [:board-size :height]))]}
              (:walls (create-level))))
    (is (not (some #{[10 10]} (:walls (create-level)))))
    (is (not (some #{[half-width 0]} (:walls (create-level)))))
    (is (not (some #{[half-width
                      (dec (get-in @snake-configuration [:board-size :height]))]}
                   (:walls (create-level))))))
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

