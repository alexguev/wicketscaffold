(ns wicketscaffold.core
  (:use [clojure.java.io])
  (:require [clojure.string :as s]))

(def ^:dynamic *options* {})

(def default-options {:output "temp"}) ;todo: use

(declare convert generate)
(defn generate-wicket-scaffold
  "generates CRUD scaffolding for the Hibernate entity identified by 'clazz'"
  [clazz & more]
  (binding [*options* (apply hash-map more)]
    (generate (convert clazz))))

(defn convert
  "tbd"
  [clazz]
  {:name (.getSimpleName clazz)
   :package (.getName (.getPackage clazz))})

(declare generate-html generate-page generate-vo)
(defn generate
  "tbd"
  [m]
  (generate-html m)
  (generate-page m)
  (generate-vo m))

(defn- to-path [package] (s/replace package "." "/"))

(defn generate-file
  "tbd"
  [{:keys [name package] :as m} file-name-suffix f]
  (let [{:keys [output]} *options*
        folder (file output (to-path package))]
    (.mkdirs folder)
    (with-open [w (writer (file folder (str name file-name-suffix)))]
      (.write w (f m)))))

(defn generate-html [m]
  (generate-file m "Page.html" (fn [m] "<html>")))

(defn generate-page [m]
  (generate-file m "Page.java" (fn [{:keys [name package]}] (str "package " package ";"))))

(defn generate-vo [m]
  (generate-file m "VO.java" (fn [{:keys [name package]}] (str "package " package ";"))))

(defn -main
  ""
  [& args]
  (let [name (first args)
        clazz (Class/forName name)]
    (generate-wicket-scaffold clazz)))
