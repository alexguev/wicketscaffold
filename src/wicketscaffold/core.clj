(ns wicketscaffold.core
  (:use [clojure.java.io]
        [wicketscaffold.util :only [to-path when-message]])
  (:require [wicketscaffold.html :as html]
            [wicketscaffold.java :as java])
  (import [java.beans Introspector]))

; for study group, describe the solution in plain english first

(declare parse validate transform generate)
(defn generate-wicket-scaffold
  "generates CRUD scaffolding for the Hibernate entity identified by 'clazz'"
  [clazz]
  (-> (parse clazz)
      (validate)
      (transform)
      (generate)))

(defn parse
  "'properties'is a vector of maps." 
  [clazz]
  {:name (.getSimpleName clazz)
   :package (.getName (.getPackage clazz))
   :annotations (.getAnnotations clazz)
   :properties (map (fn [d] {:name (-> d (.getName)) :type (-> d (.getReadMethod) (.getReturnType))})
                    (-> (Introspector/getBeanInfo clazz) (.getPropertyDescriptors)))})

(defn validate [{:keys [name annotations] :as x}]
  "evaluates to nil if x is not valid, evaluates to x otherwise"
  (when-not (or (when-message (some (partial = Deprecated) annotations)
                              (str "[" name "] "  "a hibernate entity is not valid"))
                (when-message (not (re-find #".*VO$" name))
                              (str "[" name "] "  "only classes ending in VO are valid")))
    x))

(defprotocol Writable
  "tbd"
  (write [x]))

(defrecord WicketHtmlPage [path name properties]
  Writable
  (write [x]
    (html/generate path name properties)))

(defrecord WicketJavaPage [package name properties]
  Writable
  (write [x]
    (java/generate package name properties)))

(defn transform
  "tbd"
  [{:keys [name package fields] :as x}]
  (when x
    [(WicketHtmlPage. (to-path package) (str name "Page") fields)
     (WicketJavaPage. package (str name "Page") fields)]))

(defn generate [xs]
  (doseq [x xs] (write x)))

(defn -main
  ""
  [& args]
  (let [name (first args)
        clazz (Class/forName name)]
    (generate-wicket-scaffold clazz)))
