(defproject clj-snake "0.1.0-SNAPSHOT"
  :description "Yet Another Snake Game"
  :url "https://github.com/bodewig/clj-snake"
  :license {:name "Apache License Version 2.0"
            :url "http://www.apache.org/licenses/"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :main ^:skip-aot de.samaflost.clj-snake.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
