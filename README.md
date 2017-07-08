# sparse-data

[![Clojars Project](https://img.shields.io/clojars/v/sparse-data.svg)](https://clojars.org/sparse-data)

A library for efficiently storing and querying categorical sparse data.

## Rationale

Frequently we are presented with a situation in which we have a large amount
of data which is both sparse and which can be treated categorically.

For example, one might collect event data from a web-sites detailing which page
was visited, when, how long was spent on the page, which browser was used, on
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

Enter the sparse-data library. It holds categorical sparse data in format orders
of magnitude more efficient than raw text and so makes it possible to store and query
vasts amount of data on your PC.

The central enabling concept is the "column spec". The column spec is a list containing
all the permutations of fields and field values that the data may contain. The column spec
happily deals with nesting to any level and any type of values. For example,
the port field may have valid values "mobile", "desktop", "tablet" and so the column spec
would contain:

   ... [:port mobile] [:port desktop] [:port table] ...

Each item in the column spec has an index which it's just it's position in the list. Now,
for each new piece of data, instead of storing the entire record, all we need to store are
the indexes from our column spec of the values we encounter.

## Getting Started

1. Add the library to your project file:

[![Clojars Project](https://img.shields.io/clojars/v/sparse-data.svg)](https://clojars.org/sparse-data)

2. Either *use* or *require* the library in your code:

```clojure
(use 'sparse-data)


3. You may want to start by creating a column spec directly from your data. Note that the
function accepts a sequence, so your data can be extremely large since the sequence can be
lazy. Note also that the spec *does* live in memory.

```clojure
(def spec (make-spec your-coll))
```

A *save-spec* and *read-spec* function are provided for efficient saving and retrieval of
specs to/from disk.

4. Create your archive file. This function uses your spec and efficiently encodes your data to
a gzip compressed file.

```clojure
(make-sparse your-coll spec "some-file.gz")
```

5. Use your archive by selecting the desired fields. The select function will return a lazy
sequence of maps.

```clojure
(select spec "some-file.gz" [[:some-prop][:some-other-prop][:prop :sub-prop]])
```

6. Use your lazy sequence to calculate, create datasets (e.g. Incanter's to-dataset function), etc.

