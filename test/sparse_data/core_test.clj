(ns sparse-data.core-test
  (:require [clojure.test :refer :all]
            [sparse-data.core :as sp]))

(def cmp-in [{ :a 1 :b 2 :c "four" }
             { :a 2 :b 3 }
             { :c "four" :d [ "five" "six" ] }
             { :d { :f "seven" :g 8 }}])

(def cmp-out {[:a 1] 0, [:b 2] 1,
              [:c "four"] 2, [:a 2] 3,
              [:b 3] 4, [:d ["five" "six"]] 5,
              [:d :f "seven"] 6, [:d :g 8] 7})

(deftest make-spec-complex
  (testing "Checking make-spec with a compound input"
    (is (= (sp/make-spec cmp-in) cmp-out))))

(deftest read-write-spec
  (testing "Checking spec read and write work"
    (def fname "sparse-tests.spec.tmp.gz")
    (sp/save-spec cmp-out fname)
    (let [o (sp/read-spec fname)]
      (clojure.java.io/delete-file fname :silently)
      (is (= o cmp-out)))))

(deftest make-sparse-and-select-tests
  (testing "Checking make-sparse and select methods"
    (def fname "sparse-tests.data.tmp.gz")
    (let [enot (comp not empty?)
          in (flatten (repeat 1 cmp-in))]
      (sp/make-sparse in cmp-out fname)
      (is (= (count (filter enot (sp/select cmp-out fname [[:a]]))) 2))
      (is (= (count (filter enot (sp/select cmp-out fname [[:a] [:c]]))) 3))
      (is (= (count (filter enot (sp/select cmp-out fname [[:d :g]]))) 1))
      (is (= (count (filter enot (sp/select cmp-out fname [[:d :f]]))) 1))
      (is (= (count (filter enot (sp/select cmp-out fname [[:d]]))) 1))
      (clojure.java.io/delete-file fname :silently))))
