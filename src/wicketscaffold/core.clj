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
      (generate)
      (write)))

(defn parse ; come up with a better name
  "'properties' is a vector of maps." 
  [clazz]
  {:name (.getSimpleName clazz)
   :package (.getName (.getPackage clazz))
   :annotations (.getAnnotations clazz)
   :properties (map (fn [d] {:name (-> d (.getName)) :type (-> d (.getReadMethod) (.getReturnType))})
                    (-> (Introspector/getBeanInfo clazz) (.getPropertyDescriptors)))})

(defn validate [{:keys [name annotations] :as clazz}]
  "evaluates to nil if x is not valid, evaluates to x otherwise"
  (when-not (or (when-message (some (partial = Deprecated) annotations)
                              (str "[" name "] "  "a hibernate entity is not valid"))
                (when-message (not (re-find #".*VO$" name))
                              (str "[" name "] "  "only classes ending in VO are valid")))
    clazz))

; AG: I'm not sure transform is the right name for this code
(defn transform
  "transforms 'x' into a seq of code generation closures of 'x'"
  [x]
  (when x
    [#(html/generate x)
     #(java/generate x)]))

(defn generate [fs]
  ""
  (map #(%) fs))

(defn write [xs]
  (doseq [x xs]
    (let [f (file "temp" (:file-path x) (:file-name x))]
      (make-parents f)
      (spit f (:content x)))))

(defn -main
  ""
  [& args]
  (let [name (first args)
        clazz (Class/forName name)]
    (generate-wicket-scaffold clazz)))
