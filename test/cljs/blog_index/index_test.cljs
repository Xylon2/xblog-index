(ns blog-index.index-test
  (:require [clojure.test :refer [is deftest run-tests]]
            [blog-index.index :refer [group-posts-by-category
                                      group-posts-by-date
                                      category-by-date format-date
                                      min-max-years
                                      order-categories]]
            [goog.date.DateTime :as gdate]))

(def example-edn
  [
   {
    :title "Stupid?"
    :date 1483228800
    :category "Misc"
    :link "stupid.html"
    }
   {
    :title "Maintaining homes across four domains"
    :date 1718622681
    :category "Misc"
    :link "maintaining_homes_r2.html"
    }
   {
    :title "Carnivore experiment - 10months in"
    :date 1719251874
    :category "Diet"
    :link "carnivore_10mo.html"
    }
   {
    :title "Primal food pyramid - the ultimate diet"
    :date 1672531200
    :category "Diet"
    :link "primal_food_pyramid_r2.html"
    }
   ])

(def cat-grouped-edn
  {"Misc" [{:title "Maintaining homes across four domains"
            :date 1718622681
            :category "Misc"
            :link "maintaining_homes_r2.html"}
           {:title "Stupid?"
            :date 1483228800
            :category "Misc"
            :link "stupid.html"}],
   "Diet" [{:title "Carnivore experiment - 10months in"
            :date 1719251874
            :category "Diet"
            :link "carnivore_10mo.html"}
           {:title "Primal food pyramid - the ultimate diet"
            :date 1672531200
            :category "Diet"
            :link "primal_food_pyramid_r2.html"}]})

(def cat-grouped-more-items
  {"Misc" [1 2 3],
   "Diet" [5 6 7],
   "Futurism" [8 9 10],
   "Intro" [11 12 13],
   "Cooking" [14 15 16],})

(def date-grouped-edn
  '([2024 [{:title "Carnivore experiment - 10months in"
            :date 1719251874
            :category "Diet"
            :link "carnivore_10mo.html"}
           {:title "Maintaining homes across four domains"
            :date 1718622681
            :category "Misc"
            :link "maintaining_homes_r2.html"}]],
    [2023 [{:title "Primal food pyramid - the ultimate diet"
            :date 1672531200
            :category "Diet"
            :link "primal_food_pyramid_r2.html"}]]
    [2017 [{:title "Stupid?"
            :date 1483228800
            :category "Misc"
            :link "stupid.html"}]]))

(def sorted-edn
  '(["Diet" [{:title "Carnivore experiment - 10months in"
              :date 1719251874
              :category "Diet"
              :link "carnivore_10mo.html"}
             {:title "Primal food pyramid - the ultimate diet"
              :date 1672531200
              :category "Diet"
              :link "primal_food_pyramid_r2.html"}]]
    ["Misc" [{:title "Maintaining homes across four domains"
              :date 1718622681
              :category "Misc"
              :link "maintaining_homes_r2.html"}
             {:title "Stupid?"
              :date 1483228800
              :category "Misc"
              :link "stupid.html"}]]))

(deftest test-group-posts-by-category
  (is (= cat-grouped-edn (group-posts-by-category example-edn))))

(deftest test-group-posts-by-date
  (is (= date-grouped-edn (group-posts-by-date example-edn))))

(deftest test-category-by-date
  (is (= sorted-edn (category-by-date cat-grouped-edn))))

(deftest test-format-date
  (is (= "24th Jun" (format-date "1719251874")))
  (is (= "1st Jan" (format-date "1704067200")))
  (is (= "3rd Mar" (format-date "1709443200")))
  (is (= "24th Nov" (format-date "1700822400"))))

(deftest test-min-max-years
  (is (= {:min 2017 :max 2024} (min-max-years example-edn))))

(deftest test-order-categories
  (is (= {:ordered [["Intro" [11 12 13]]
                    ["Cooking" [14 15 16]]
                    ["Futurism" [8 9 10]]],
          :remainder {"Misc" [1 2 3],
                      "Diet" [5 6 7]}}
         (order-categories cat-grouped-more-items ["Intro" "Cooking" "Futurism"]))))

(run-tests)
