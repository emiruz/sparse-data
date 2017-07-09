# sparse-data

[![Clojars Project](https://img.shields.io/clojars/v/sparse-data.svg)](https://clojars.org/sparse-data)

A library for efficiently storing and querying categorical sparse data.

The library is a work in progress. Please raise issues and contribute if you'd like.

## Rationale

Frequently we are presented with a situation in which we have a large amount
of data which is both sparse and which can be treated categorically.

For example, one might collect event data from a web-sites detailing which page
was visited, and when, how long was spent on the page, which browser was used, on
which device, from which country, etc. The pertinent part of the data may look
something like this.

    page: contact-us
    duration: 17
    port: mobile
    browser: firefox
    date: 2017-07-02
    hour: 15
    min: 34

Now, say our sites are popular and the data collected spans giga-bytes a day,
we're quickly in the position where we are not able to hold all the data we
need on a PC.

Enter the sparse-data library. It aims to store categorical sparse data on disk some N
times less space than gzipped JSON, and to make this data easy to query from Clojure.

The central enabling concept is the "column spec". The column spec is a map containing
all the values of all fields as keys, and an index as the value. The column spec happily deals
with nesting to any level and any type of values. For example, the port field may have valid
values "mobile", "desktop", "tablet" and so the column spec would contain:

    ..., [:port "mobile"] 4, [:port "desktop"] 5, [:port "tablet"] 6, ...

For each new piece of data, instead of storing the entire record, all we need to store are
the indexes of the values we encounter. This list is kept in a TSV GZIP, from which you may
create a lazy sequence of any subset of fields using the *select* function to be used in
further processing.

## Getting Started

1. Add the library to your project file:

[![Clojars Project](https://img.shields.io/clojars/v/sparse-data.svg)](https://clojars.org/sparse-data)

2. Either *use* or *require* the library in your code:

```clojure
(use 'sparse-data)
```

3. You may want to start by creating a column spec directly from your data. The function
expects you to supply a sequence of maps. If your sequence is lazy your data may be
extremely large. Note that unlike the data itself which is stored on disk,  the spec *does*
need to be able to fit into memory.

```clojure
(def spec (make-spec your-coll))
```

*save-spec* and *read-spec* functions are provided for efficient saving and retrieval of
specs to/from disk.

4. Create your archive file. This function uses your spec and efficiently encodes your data to
a gzip compressed file TSV index file.

```clojure
(make-sparse your-coll spec "some-file.gz")
```

5. Select information from your archive. The select function will return a lazy sequence of
maps according to the fields which you have specified.

```clojure
(select spec "some-file.gz" [[:some-prop][:some-other-prop][:prop :sub-prop]])
```

6. Use your lazy sequence to calculate, create datasets (e.g. Incanter's to-dataset function), etc.

# Performance Example

A JSON file containing 5.1M objects was used. It was 1.6GB on disk and 192MB GZIP compressed.
It contained 8 fields, most of which were compulsory, and 134945 possible values. *make-spec* was used to create
a spec file (3.5MB on disk), and *make-sparse* was used to store the data, resulting in a 46MB archive file
(213MB uncompressed). That's roughly **4 times smaller** on disk than the compressed JSON.

I think that sits somewhere around the likely worse case scenario. Generally, the more possible values and the
sparser (more optional) they are, the more efficient the algorithm in comparison to GZIP JSON.
