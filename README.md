# sparse-data

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
all the permutations of fields and field values that the data may contain. For example,
the port field may have valid values "mobile", "desktop", "tablet" and so the column spec
would contain:

   ... [:port mobile] [:port desktop] [:port table] ...

Each item in the column spec has an index which it's just it's position in the list. Now,
for each new piece of data, instead of storing the entire record, all we need to store are
the indexes from our column spec of the values we encounter.


## Getting Started

