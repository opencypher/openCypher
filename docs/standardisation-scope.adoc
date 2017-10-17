= Standardisation scope of Cypher

One of the challenges faced by the standardisation of Cypher has been to choose which language features to include.
As Cypher has several years of history, some historical artifacts are present in the feature set.
This is in general not because there haven't been good replacements, but because Cypher's role to date has been to support a specific product (Neo4j).
However, the life cycle of a standardised language is different, and the removal of standardised features is generally difficult.

With that in mind, the openCypher project is endeavouring not to include language features that are not envisioned to be the future of the language.
This document aims to provide a description of the language features selected to become part of the standardised set of features, as well as the features that have been excluded.

It should be noted that it is still early days for the openCypher project, and this division between standardised and non-standardised features will likely be subject to change.
It should also be observed that _additive change_, that is, moving a feature _into_ the standardised set, is generally easier than doing the reverse.
It is the goal of this project to create a good and feature-rich standard language for graph querying, and as such any valuable functionality listed as excluded here is going to receive a replacement feature at some point.

== Language features included in openCypher

=== Clauses

* `CALL ... YIELD`
* `CREATE`
* `DELETE`
* `DETACH DELETE`
* `MATCH`
* `MERGE`
* `OPTIONAL MATCH`
* `REMOVE`
* `RETURN`
* `SET`
* `UNION`
* `UNWIND`
* `WITH`

==== Sub-clauses

* `LIMIT`
* `ORDER BY`
* `SKIP`
* `WHERE`

=== Patterns

* Simple patterns with binding
* Variable length patterns without binding
* `allShortestPaths()`
* `shortestPath()`

=== Operators

==== General

* `.` (property access)
* `[]` (subscript)
* `DISTINCT`
* `IN`

==== Mathematical operators

* `+`
* `-`
* `/`
* `*`
* `%`
* `^`

==== String operators

* `+` (string concatenation)
* `CONTAINS`
* `ENDS WITH`
* `STARTS WITH`

==== Comparison operators

* `=`
* `<>`
* `<`
* `>`
* `\<=`
* `>=`

==== Logical operators

* `AND`
* `IS NULL`
* `IS NOT NULL`
* `NOT`
* `OR`
* `XOR`

=== Expressions

* List comprehensions
* Pattern comprehensions
* Parameters with new syntax (`$`)
* Literals
* `CASE`

=== Functions

* user-defined functions
* `abs()`
* `acos()`
* `asin()`
* `atan()`
* `atan2()`
* `coalesce()`
* `ceil()`
* `cos()`
* `cot()`
* `degrees()`
* `e()`
* `endNode()`
* `exp()`
* `floor()`
* `head()`
* `labels()`
* `last()`
* `left()`
* `length()`
* `log()`
* `log10()`
* `lTrim()`
* `nodes()`
* `pi()`
* `keys()`
* `radians()`
* `rand()`
* `range()`
* `relationships()`/`rels()`
* `replace()`
* `reverse()`
* `right()`
* `round()`
* `rTrim()`
* `sign()`
* `sin()`
* `size()`
* `sqrt()`
* `split()`
* `startNode()`
* `substring()`
* `tail()`
* `tan()`
* `toFloat()`
* `toInteger()`
* `toString()`
* `toBoolean()`
* `properties()`
* `timestamp()`
* `trim()`
* `type()`
* `toUpper()`
* `toLower()`

=== Types

* primitives
* list
* map
* node
* relationship
* path

== Language features included in openCypher with new definitions

=== General

* pattern expressions
* `id()`

=== Aggregating functions

* `avg()`
* `collect()`
* `count()`
* `max()`
* `min()`
* `percentileCont()`
* `percentileDisc()`
* `stdev()`
* `stdevP()`
* `sum()`

== Language features excluded from openCypher

=== Clauses

* `CREATE UNIQUE`
* `FOREACH`
* `START`
* `LOAD CSV`

==== Sub-clauses

* `ON CREATE`
* `ON MATCH`

=== Patterns

* Variable length patterns with binding
* double-arrow syntax to describe undirected patterns (`()\<-[]\->()`)
* double-dash syntax to describe undirected patterns (`()--()`)

=== Hints

* `USING INDEX`
* `USING JOIN`
* `USING PERIODIC COMMIT`
* `USING SCAN`

=== Commands

* `CREATE CONSTRAINT`
* `CREATE INDEX`

=== Operators

* `=~` (regular expression)

=== Expressions

* Parameters with old syntax (`{}`)

=== Functions

* `all()`
* `any()`
* `distance()`
* `exists()`
* `extract()`
* `filter()`
* `haversin()`
* `lower()`
* `none()`
* `point()`
* `reduce()`
* `single()`
* `upper()`

=== Types

* datetime types
* point
