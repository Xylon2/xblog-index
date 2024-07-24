#!/usr/bin/env bb

(ns update-edn
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [babashka.deps :as deps]
            [clojure.instant :as inst]))

(defn read-edn-file [filepath]
  (try
    (with-open [r (io/reader filepath)]
      (edn/read (java.io.PushbackReader. r)))
    (catch Exception e
      (println "Error reading EDN file:" (.getMessage e))
      nil)))

(defn write-edn-file [filepath data]
  (try
    (with-open [w (io/writer filepath)]
      (pprint data w))
    (catch Exception e
      (println "Error writing EDN file:" (.getMessage e)))))

(defn backup-file [filepath]
  (fs/copy filepath (str filepath ".bak") {:replace-existing true}))

(defn get-unique-categories [posts]
  (set (map :category posts)))

(defn prompt [message]
  (print message)
  (flush)
  (read-line))

(defn current-unix-timestamp []
  (quot (System/currentTimeMillis) 1000))

(defn add-post-to-edn []
  (let [filepath "index.edn"
        data (read-edn-file filepath)]
    (if data
      (let [posts (:posts data)
            unique-categories (get-unique-categories posts)]
        (println "Existing categories:" unique-categories)
        (println "Please enter the new post details:")
        (let [title (prompt "Enter the title of the post: ")
              category (prompt (str "Enter the category of the post (existing - " unique-categories "): "))
              link (prompt "Enter the link of the post: ")
              new-post {:title title
                        :date (current-unix-timestamp)
                        :category category
                        :link link}
              updated-posts (conj posts new-post)
              updated-data (assoc data :posts updated-posts)]
          (backup-file filepath)
          (write-edn-file filepath updated-data)
          (println "New post added successfully.")))
      (println "Error: Failed to read EDN data."))))

(defn -main [& args]
  (add-post-to-edn))

(-main)
