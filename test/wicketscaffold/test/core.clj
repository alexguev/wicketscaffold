(ns wicketscaffold.test.core
  (:use [clojure.test]
        [wicketscaffold.core]
        [clojure.java.io :only [file]])
  (:import [wicketscaffold.test.core FakeHibernateEntity]))

(deftest test-verify
  
  (testing "a hibernate entity is not valid" ;TODO use correct annotation
    (is (nil? (validate {:name "NameVO" :package "some.package" :annotations [Deprecated]})))
    (is (= {:name "NameVO" :package "some.package" :annotations []}
           (validate {:name "NameVO" :package "some.package" :annotations []}))))
  
  (testing "only classes ending in VO are valid"
    (is (nil? (validate {:name "Name" :package "some.package" :annotations []})))
    (is (nil? (validate {:name "NameVO1" :package "some.package" :annotations []})))
    (is (= {:name "NameVO" :package "some.package" :annotations []}
           (validate {:name "NameVO" :package "some.package" :annotations []})))))


(deftest test-write
  (is (= "Some Content"
         (do (write [{:file-path "Foo" :file-name "Bar" :content "Some Content"}] "temp")
             (slurp (file "temp" "Foo" "Bar"))))))

(defn my-fixture [f]
  (f)  
  (println f)
  )

(use-fixtures :each my-fixture)