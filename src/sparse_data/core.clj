(ns sparse-data.core
  (:require [clojure.string :as str]
            [clojure.set :as cset]
            [clojure.java.io :as io]))

(defn- flatten-map [m]
  (if (map? m)
    (vec
     (mapcat (fn [[k v]]
               (let [sub (flatten-map v)
                     nested (map #(into [k] %) (filter seq sub))]
                 (if (seq nested)
                   nested
                   [[k v]])))
             m)) []))

(defn make-spec
  "Create a new column spec from a collection"
  [coll]
  (let [f (fn [x y]
            (let [o (flatten-map y)]
              (reduce #(update-in %1 [%2] (constantly false)) x o)))
        o (reduce f {} coll)]
    (doall
     (zipmap
      (filter #(some? (peek %))(keys o))
      (range 0 (count o))))))

(defn save-spec
  "Saves a column spec to a file"
  [spec fname]
  (with-open [w (-> fname io/output-stream java.util.zip.GZIPOutputStream.)]  
    (io/copy (pr-str spec) w)))

(defn read-spec
  "Reads a column spec from a file"
  [fname]
  (with-open [r (-> fname io/input-stream java.util.zip.GZIPInputStream. io/reader)]
    (clojure.edn/read-string (slurp r))))

(defn make-sparse
  "Write the supplied collection to a sparse data file according to the column spec provided."
  [coll spec fname]
  (with-open [w (-> fname io/output-stream java.util.zip.GZIPOutputStream.)]
    (doseq [j coll]
      (io/copy
       (str
        (str/join "\t" (map #(Long/toString % 36)
                            (filter some? (map #(get spec %) (flatten-map j)))))
        "\n") w))))

(defn select
  "select a lazy seq of values from a sparse data file according to the fields specified."
  [spec fname fields]
  (let
      [m (if (= fields :all)
           spec
           (filter some? (map (fn[x] (if (some #(= (pop (first x)) %) fields) x)) spec)))
       cols (into {} (map (fn[x][(peek x) (first x)]) m))]
    (letfn
        [(helper [rdr]
           (lazy-seq
            (if-let [line (.readLine rdr)]
              (cons
               (into
                {}
                (map
                 (fn[x][(pop x) (peek x)])
                 ((comp vals #(select-keys cols %))
                  (map #(Long/parseLong % 36) (str/split line #"\t")))))
               (helper rdr))
              (do (.close rdr) nil))))]
      (helper (-> fname io/input-stream java.util.zip.GZIPInputStream. io/reader)))))
