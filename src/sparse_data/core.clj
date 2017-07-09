(ns sparse-data.core
  (:require [clojure.string :as str]
            [clojure.set :as cset]
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
  (defn f [x y]
    (let [o (flatten-map y)]
      (reduce #(update-in %1 [%2] (constantly false)) x o)))
  (let [o (reduce f {} coll)]
    (doall
     (zipmap
      (filter #(some? (last %))(keys o))
      (range 0 (count o))))))

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
        (str/join "\t" (filter some? (map #(get spec %) (flatten-map j))))
        "\n") w))))

(defn- get-map-from-vec [spec cols]
  (apply merge
         (map
          #(let [o (nth (keys spec) %)](assoc-in {} (butlast o) (last o)))
          cols)))

(defn select [spec fname fields]
  "select a lazy seq of values from a sparse data file according to the fields specified."
  (def cols (into #{} (filter some? (map
             (fn[x] (if (some #(= (butlast x) %) fields) (get spec x)))
             (keys spec)))))
  (letfn [(helper [rdr]
            (lazy-seq
              (if-let [line (.readLine rdr)]
                (cons
                 (get-map-from-vec
                  spec
                  (into [] (cset/intersection
                            cols
                            (into #{} (map #(Long. %) (str/split line #"\t"))))))
                 (helper rdr))
                (do (.close rdr) nil))))]
    (helper (-> fname io/input-stream java.util.zip.GZIPInputStream. io/reader))))
