#
# Copyright (c) 2015-2020 "Neo Technology,"
# Network Engine for Objects in Lund AB [http://neotechnology.com]
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Attribution Notice under the terms of the Apache License 2.0
#
# This work was created by the collective efforts of the openCypher community.
# Without limiting the terms of Section 6, any Derivative Work that is not
# approved by the public consensus process of the openCypher Implementers Group
# should not be described as “Cypher” (and Cypher® is a registered trademark of
# Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
# proposals for change that have been documented or implemented should only be
# described as "implementation extensions to Cypher" or as "proposed changes to
# Cypher that are not yet approved by the openCypher community".
#

#encoding: utf-8

Feature: AggregationAcceptance

  # Consider moving to feature on RETURN clause's expressions on aggregates capability
  Scenario: Support multiple divisions in aggregate function
    Given an empty graph
    And having executed:
      """
      UNWIND range(0, 7250) AS i
      CREATE ()
      """
    When executing query:
      """
      MATCH (n)
      RETURN count(n) / 60 / 60 AS count
      """
    Then the result should be, in any order:
      | count |
      | 2     |
    And no side effects

  # Consider to move to feature on RETURN clause's column renaming capability
  Scenario: Support column renaming for aggregates as well
    Given an empty graph
    And having executed:
      """
      UNWIND range(0, 10) AS i
      CREATE ()
      """
    When executing query:
      """
      MATCH ()
      RETURN count(*) AS columnName
      """
    Then the result should be, in any order:
      | columnName |
      | 11         |
    And no side effects

  # Consider moving to feature on RETURN clause's expressions on aggregates capability
  Scenario: Aggregates inside normal functions
    Given an empty graph
    And having executed:
      """
      UNWIND range(0, 10) AS i
      CREATE ()
      """
    When executing query:
      """
      MATCH (a)
      RETURN size(collect(a))
      """
    Then the result should be, in any order:
      | size(collect(a)) |
      | 11               |
    And no side effects

  # Consider moving to feature on RETURN clause's expressions on aggregates capability
  Scenario: Handle aggregates inside non-aggregate expressions
    Given an empty graph
    When executing query:
      """
      MATCH (a {name: 'Andres'})<-[:FATHER]-(child)
      RETURN {foo: a.name='Andres', kids: collect(child.name)}
      """
    Then the result should be, in any order:
      | {foo: a.name='Andres', kids: collect(child.name)} |
    And no side effects

  # Consider moving to feature on RETURN clause's ORDER BY capability (on aggregates)
  Scenario: Sort on aggregate function and normal property
    Given an empty graph
    And having executed:
      """
      CREATE ({division: 'Sweden'})
      CREATE ({division: 'Germany'})
      CREATE ({division: 'England'})
      CREATE ({division: 'Sweden'})
      """
    When executing query:
      """
      MATCH (n)
      RETURN n.division, count(*)
      ORDER BY count(*) DESC, n.division ASC
      """
    Then the result should be, in order:
      | n.division | count(*) |
      | 'Sweden'   | 2        |
      | 'England'  | 1        |
      | 'Germany'  | 1        |
    And no side effects

  # Consider moving to feature on RETURN clause's implicit grouping capability
  Scenario: Aggregate on property
    Given an empty graph
    And having executed:
      """
      CREATE ({num: 33})
      CREATE ({num: 33})
      CREATE ({num: 42})
      """
    When executing query:
      """
      MATCH (n)
      RETURN n.num, count(*)
      """
    Then the result should be, in any order:
      | n.num | count(*) |
      | 42    | 1        |
      | 33    | 2        |
    And no side effects

  # Consider moving to feature on RETURN clause's aggregates on complex expressions capability
  Scenario: Handle aggregation on functions
    Given an empty graph
    And having executed:
      """
      CREATE (a:L), (b1), (b2)
      CREATE (a)-[:A]->(b1), (a)-[:A]->(b2)
      """
    When executing query:
      """
      MATCH p=(a:L)-[*]->(b)
      RETURN b, avg(length(p))
      """
    Then the result should be, in any order:
      | b  | avg(length(p)) |
      | () | 1.0            |
      | () | 1.0            |
    And no side effects

  # Consider moving to feature on RETURN clause's DISTINCT capability in implicit grouping
  Scenario: Aggregate on list values
    Given an empty graph
    And having executed:
      """
      CREATE ({color: ['red']})
      CREATE ({color: ['blue']})
      CREATE ({color: ['red']})
      """
    When executing query:
      """
      MATCH (a)
      RETURN DISTINCT a.color, count(*)
      """
    Then the result should be, in any order:
      | a.color  | count(*) |
      | ['red']  | 2        |
      | ['blue'] | 1        |
    And no side effects

  # Consider moving to feature on RETURN clause's aggregates on complex expressions capability
  @NegativeTest
  Scenario: Aggregates in aggregates
    Given any graph
    When executing query:
      """
      RETURN count(count(*))
      """
    Then a SyntaxError should be raised at compile time: NestedAggregation

  # Consider moving to feature on RETURN clause's expressions on aggregates capability
  Scenario: Aggregates with arithmetics
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH ()
      RETURN count(*) * 10 AS c
      """
    Then the result should be, in any order:
      | c  |
      | 10 |
    And no side effects

  # Consider moving to feature on RETURN clause's ORDER BY capability (on aggregates)
  Scenario: Aggregates ordered by arithmetics
    Given an empty graph
    And having executed:
      """
      CREATE (:A), (:X), (:X)
      """
    When executing query:
      """
      MATCH (a:A), (b:X)
      RETURN count(a) * 10 + count(b) * 5 AS x
      ORDER BY x
      """
    Then the result should be, in order:
      | x  |
      | 30 |
    And no side effects

  # Consider moving to feature on RETURN clause's multiple aggregate capability
  Scenario: Multiple aggregates on same variable
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH (n)
      RETURN count(n), collect(n)
      """
    Then the result should be, in any order:
      | count(n) | collect(n) |
      | 1        | [()]       |
    And no side effects

  # Consider moving to feature on RETURN clause's ORDER BY capability (on aggregates)
  Scenario: Counting matches
    Given an empty graph
    And having executed:
      """
      UNWIND range(1, 100) AS i
      CREATE ()
      """
    When executing query:
      """
      MATCH ()
      RETURN count(*)
      """
    Then the result should be, in any order:
      | count(*) |
      | 100      |
    And no side effects

  # Consider moving to feature on RETURN clause's ORDER BY capability (on aggregates)
  Scenario: Counting matches per group
    Given an empty graph
    And having executed:
      """
      CREATE (a:L), (b1), (b2)
      CREATE (a)-[:A]->(b1), (a)-[:A]->(b2)
      """
    When executing query:
      """
      MATCH (a:L)-[rel]->(b)
      RETURN a, count(*)
      """
    Then the result should be, in any order:
      | a    | count(*) |
      | (:L) | 2        |
    And no side effects

  # Consider moving to feature on RETURN clause's aggregates on complex expressions capability
  Scenario: Aggregation of named paths
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (b:B), (c:C), (d:D), (e:E), (f:F)
      CREATE (a)-[:R]->(b)
      CREATE (c)-[:R]->(d)
      CREATE (d)-[:R]->(e)
      CREATE (e)-[:R]->(f)
      """
    When executing query:
      """
      MATCH p = (a)-[*]->(b)
      RETURN collect(nodes(p)) AS paths, length(p) AS l
      ORDER BY l
      """
    Then the result should be, in order:
      | paths                                                    | l |
      | [[(:A), (:B)], [(:C), (:D)], [(:D), (:E)], [(:E), (:F)]] | 1 |
      | [[(:C), (:D), (:E)], [(:D), (:E), (:F)]]                 | 2 |
      | [[(:C), (:D), (:E), (:F)]]                               | 3 |
    And no side effects

  # Consider moving to feature on RETURN clause's aggregates on complex expressions capability
  Scenario: Returning the minimum length of paths
    Given an empty graph
    And having executed:
      """
      CREATE (a:T {name: 'a'}), (b:T {name: 'b'}), (c:T {name: 'c'})
      CREATE (a)-[:R]->(b)
      CREATE (a)-[:R]->(c)
      CREATE (c)-[:R]->(b)
      """
    When executing query:
      """
      MATCH p = (a:T {name: 'a'})-[:R*]->(other:T)
      WHERE other <> a
      WITH a, other, min(length(p)) AS len
      RETURN a.name AS name, collect(other.name) AS others, len
      """
    Then the result should be (ignoring element order for lists):
      | name | others     | len |
      | 'a'  | ['c', 'b'] | 1   |
    And no side effects

  # Consider moving to feature on RETURN clause's multiple expression capability
  Scenario: Handle subexpression in aggregation also occurring as standalone expression with nested aggregation in a literal map
    Given an empty graph
    And having executed:
      """
      CREATE (:A), (:B {num: 42})
      """
    When executing query:
      """
      MATCH (a:A), (b:B)
      RETURN coalesce(a.num, b.num) AS foo,
        b.num AS bar,
        {name: count(b)} AS baz
      """
    Then the result should be, in any order:
      | foo | bar | baz       |
      | 42  | 42  | {name: 1} |
    And no side effects

  Scenario: Projection during aggregation in WITH before MERGE and after WITH with predicate
    Given an empty graph
    And having executed:
      """
      CREATE (:A {num: 42})
      """
    When executing query:
      """
      UNWIND [42] AS props
      WITH props WHERE props > 32
      WITH DISTINCT props AS p
      MERGE (a:A {num: p})
      RETURN a.num AS prop
      """
    Then the result should be, in any order:
      | prop |
      | 42   |
    And no side effects
