(ns sparse-data.core
  (:require [clojure.string :as str]
            [clojure.set :as cset]
            [clojure.core.reducers :as red]
            [clojure.java.io :as io]))

(defn- flatten-map [m]
  (if (map? m)
    (vec
     (mapcat (fn [[k v]]
               (let [sub (flatten-map v)
                     nested (map #(into [k] %) (filter (comp not empty?) sub))]
                 (if (seq nested)
                   nested
                   [[k v]])))
             m)) []))

(defn make-spec[coll]
  "Create a new column spec from a collection"
  (defn f ([x y] (distinct (doall(concat x (flatten-map y))))) ([][]))
  (red/fold f coll))

(defn save-spec[spec fname]
  "Saves a column spec to a file"
  (with-open [w (-> fname io/output-stream java.util.zip.GZIPOutputStream.)]  
    (io/copy (pr-str spec) w)))

(defn read-spec[fname]
  "Reads a column spec from a file"
  (with-open [r (-> fname io/input-stream java.util.zip.GZIPInputStream. io/reader)]
    (clojure.edn/read-string (slurp r))))

(defn make-sparse[coll spec fname]
  "Write the supplied collection to a sparse data file according to the column spec provided."
  (with-open [w (-> fname io/output-stream java.util.zip.GZIPOutputStream.)]
    (doseq [j coll]
      (io/copy
       (str
        (str/join "\t" (keep-indexed #(if (= (get-in j (butlast %2)) (last %2)) %1) spec))
        "\n") w))))

(defn- get-map-from-vec [spec cols]
  (apply
   merge
   (map #(let [o (nth spec %)](assoc-in {} (butlast o) (last o))) cols)))

(defn select [spec fname fields]
  "select a lazy seq of values from a sparse data file according to the fields specified."
  (def cols (keep-indexed (fn[i x] (if (some #(= (butlast x) %) fields) i)) spec))
  (letfn [(helper [rdr]
            (lazy-seq
             (filter
              some?
              (if-let [line (.readLine rdr)]
                (cons
                 (get-map-from-vec
                  spec
                  (into [] (cset/intersection
                            (into #{} cols)
                            (into #{} (map #(Long. %) (str/split line #"\t"))))))
                 (helper rdr))
                (do (.close rdr) nil)))))]
    (helper (-> fname io/input-stream java.util.zip.GZIPInputStream. io/reader))))
