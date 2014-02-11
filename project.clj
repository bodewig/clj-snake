(defproject clj-snake "0.1.0"
  :description "Yet Another Snake Game"
  :url "https://github.com/bodewig/clj-snake"
  :license {:name "Apache License Version 2.0"
            :url "http://www.apache.org/licenses/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.4"]
                 [com.taoensso/tower "2.0.1"]]
  :main ^:skip-aot de.samaflost.clj-snake.game
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
