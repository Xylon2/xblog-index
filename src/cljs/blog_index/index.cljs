(ns blog-index.index
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [goog.dom :as gdom]
            [cljs-time.coerce :as coerce]
            [cljs-time.core :as time]
            [clojure.set :as set]))

(def postdata (atom {}))  ; stores the edn when we download it
(def sort-type (atom :category))  ; either :category or :date

(defn sort-posts-by-date [posts]
  (sort-by :date > posts))

(defn get-ordinal-suffix [day]
  (let [mod10 (mod day 10)
        mod100 (mod day 100)]
    (cond
      (= mod10 1) (if (= mod100 11) "th" "st")
      (= mod10 2) (if (= mod100 12) "th" "nd")
      (= mod10 3) (if (= mod100 13) "th" "rd")
      :else "th")))

(defn format-date [timestamp-str]
  (let [timestamp (js/parseInt timestamp-str)
        date (js/Date. (* timestamp 1000))
        day (.getDate date)
        month (.toLocaleDateString date "en-GB" #js {:month "short"})
        ordinal-suffix (get-ordinal-suffix day)]
    (str day ordinal-suffix " " month)))

(defn categorize-post [buildme post]
  (update buildme (:category post) (fnil conj []) post))

(defn group-posts-by-category
  "returns a map of categories, each containing a vector of maps (posts)" 
  [blog-posts]
  (let [sorted-posts (sort-posts-by-date blog-posts)]
      (reduce
       categorize-post
       {}
       sorted-posts)))

(defn category-by-date
  "sort the categories by the post with the most recent date within that category"
  [cats]
  (let [;; this looks at a single category to find the most recent post
        get-biggest-post (fn [cat]
                           (apply max (map :date cat)))
        ;; this compares categories based on the most recent post from each
        comp (fn [a b]
               (> (get-biggest-post a) (get-biggest-post b)))

        ;; sort the categories
        cats-sorted (sort-by val comp cats)]

    cats-sorted))

(defn render-category [category entries]
  (let [category-div (.createElement js/document "div")
        h2 (.createElement js/document "h2")
        ul (.createElement js/document "ul")]
    (.setAttribute category-div "class" "blog-category")
    (.appendChild h2 (.createTextNode js/document category))
    (.appendChild category-div h2)
    (doseq [entry entries]
      (let [li (.createElement js/document "li")
            a (.createElement js/document "a")]
        (.setAttribute a "href" (:link entry))
        (.appendChild a (.createTextNode js/document (:title entry)))
        (.appendChild li a)
        (.appendChild ul li)))
    (.appendChild category-div ul)
    category-div))

(defn order-categories
  "essentially we have a map, and want to re-arrange what's inside it to a specific order"
  [source-map order]
  (let [ordered (map #(vector % (get source-map %)) order)
        remainder (apply dissoc source-map order)]
    {:ordered ordered :remainder remainder}))

(defn write-posts-by-category!
  []
  (let [content-div (gdom/getElement "blog-posts-container")
        by-category (group-posts-by-category (:posts @postdata))
        {:keys [category-order-top category-order-bottom]} @postdata

        ;; bit of juggling to apply any custom category ordering
        valid-top-categories (set/intersection category-order-top (set (keys by-category)))
        valid-bottom-categories (set/intersection category-order-bottom (set (keys by-category)))
        {top :ordered remainder :remainder} (order-categories by-category valid-top-categories)
        {bottom :ordered unsorted :remainder} (order-categories remainder valid-bottom-categories)
        cats-explicit-order (concat top (category-by-date unsorted) bottom)]

    (doseq [[category entries] cats-explicit-order]
      (.appendChild content-div (render-category category entries)))))

(defn get-year-from-timestamp [timestamp]
  (-> (coerce/from-long (* 1000 timestamp))
      (time/year)))

(defn group-by-year [posts]
  (reduce (fn [acc post]
            (let [year (get-year-from-timestamp (:date post))]
              (update acc year (fnil conj []) post)))
          {}
          posts))

(defn group-posts-by-date
  "create a list of years, sorted by date. and the posts within are sorted by
  date"
  [posts]
  (let [sorted-posts (sort-posts-by-date posts)
        grouped (group-by-year sorted-posts)]
    (sort-by key > grouped)))

(defn add-date-to-title
  [{:keys [title date] :as post}]
  (assoc post :title (str title " - " (format-date date))))

(defn write-posts-by-date!
  []
  (let [content-div (gdom/getElement "blog-posts-container")
        by-date (group-posts-by-date (:posts @postdata))]

    ;; by-date is a map of categories. with the key being the category name and
    ;; the value being a vector of maps which are the posts
    (doseq [[category entries] by-date]
      (.appendChild content-div (render-category category (map add-date-to-title entries))))))

(defn min-max-years [data]
  (let [years (map #(get-year-from-timestamp (:date %)) data)]
    {:min (apply min years)
     :max (apply max years)}))

(defn write-copyright-years!
  []
  (let [copyright-span (gdom/getElement "copyright")
        {:keys [min max]} (min-max-years (:posts @postdata))]
    (set! (.-textContent copyright-span)
     (if (= min max)
       (str min)
       (str min "-" max)))
    ))

(defn reset-contents []
  (let [content-div (gdom/getElement "blog-posts-container")]

    (set! (.-innerHTML content-div) "")))

(defn toggle-btn-state
  []
  (if (= (deref sort-type) :category)
    (reset! sort-type :date)
    (reset! sort-type :category)))

(def button-states
  {:category {:btntext "Sort by Date"
              :renderfn write-posts-by-category!}
   :date {:btntext "Sort by Category"
          :renderfn write-posts-by-date!}})

(defn toggle-sort
  "toggle button state, set text, call render fn"
  []
  (toggle-btn-state)
  (let [button (gdom/getElement "toggle-sort")
        {{:keys [btntext renderfn]} @sort-type} button-states]
    (set! (.-textContent button) btntext)
    (reset-contents)
    (renderfn)))

(defn load-posts []
  (go
    (let [response (<! (http/get "/index.edn"))]
      (if (:success response)
        (do
          (reset! postdata (:body response))
          (write-posts-by-category!)
          (write-copyright-years!))
        (js/console.error "Failed to fetch index.edn:" (:error-text response))))))

(defn main! []
  (let [button (.getElementById js/document "toggle-sort")]
    (.addEventListener button "click" toggle-sort))

  (load-posts)
  )  

(defn ^:dev/before-load stop []
  (js/console.log "stopping")
  (reset-contents))

(defn ^:dev/after-load start []
  (js/console.log "starting")
  (main!))
