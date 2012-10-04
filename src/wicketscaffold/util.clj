(ns wicketscaffold.util
  (:use [clojure.java.io])
  (:require [clojure.string :as s]))


(defn to-path [package] (s/replace package "." "/"))

(defn when-message [x m]
  (when x (println m) true))

(defn generate-file
  "tbd"
  [{:keys [path name] :as m} f]
  (let [{:keys [output]} {}
        folder (file output path)]
    (.mkdirs folder)
    (with-open [w (writer (file folder name))]
      (.write w (f m)))))