(ns wicketscaffold.core
  (:use [clojure.java.io])
  (:require [clojure.string :as s]
            [wicketscaffold.html :as html]
            [wicketscaffold.java :as java]))

(def ^:dynamic *options* {})

(def default-options {:output "temp"}) ;todo: use

(defn- to-path [package] (s/replace package "." "/"))

(defprotocol Writable
  "tbd"
  (write [x]))

(declare parse verify transform generate)
(defn generate-wicket-scaffold
  "generates CRUD scaffolding for the Hibernate entity identified by 'clazz'"
  [clazz & more]
  (binding [*options* (into default-options (apply hash-map more))]
    (-> (parse clazz)
        (verify)
        (transform)
        (generate))))

(defn generate [xs]
  (doseq [x xs] (write x)))

(defn parse
  "tbd"
  [clazz]
  {:name (.getSimpleName clazz)
   :package (.getName (.getPackage clazz))})

(defrecord WicketHtmlPage [path name]
  Writable
  (write [p]
    (html/generate path name)))

(defrecord WicketJavaPage [package name]
  Writable
  (write [x]
    (java/generate package name)))

(defn transform
  "tbd"
  [{:keys [name package] :as m}]
  (when m
    [(WicketHtmlPage. (to-path package) (str name "Page.html"))
     (WicketJavaPage. package (str name "Page.java"))]))

(defn verify [m]
  m)

(defn -main
  ""
  [& args]
  (let [name (first args)
        clazz (Class/forName name)]
    (generate-wicket-scaffold clazz)))
