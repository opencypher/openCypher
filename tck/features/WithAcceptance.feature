#
# Copyright 2016 "Neo Technology",
# Network Engine for Objects in Lund AB (http://neotechnology.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

Feature: WithAcceptance

  Scenario: Passing on pattern nodes
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL]->(b:B)
      """
    When executing query:
      """
      MATCH (a:A) WITH a
      MATCH (a)-->(b)
      RETURN *
      """
    Then the result should be:
      | a    | b    |
      | (:A) | (:B) |
    And no side effects

  Scenario: ORDER BY and LIMIT can be used
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL]->(), (b), (c)
      """
    When executing query:
      """
      MATCH (a:A)
      WITH a
      ORDER BY a.name
      LIMIT 1
      MATCH (a)-->(b)
      RETURN a
      """
    Then the result should be:
      | a    |
      | (:A) |
    And no side effects

  Scenario: No dependencies between the query parts
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (b:B)
      """
    When executing query:
      """
      MATCH (a)
      WITH a
      MATCH (b)
      RETURN a, b
      """
    Then the result should be:
      | a    | b    |
      | (:A) | (:A) |
      | (:A) | (:B) |
      | (:B) | (:A) |
      | (:B) | (:B) |
    And no side effects

  Scenario: Aliasing
    Given an empty graph
    And having executed:
      """
      CREATE (a:Begin {prop: 42}),
        (b:End {prop: 42}),
        (c:End {prop: 3})
      """
    When executing query:
      """
      MATCH (a:Begin)
      WITH a.prop AS property
      MATCH (b:End)
      WHERE property = b.prop
      RETURN b
      """
    Then the result should be:
      | b                 |
      | (:End {prop: 42}) |
    And no side effects

  Scenario: Handle dependencies across WITH
    Given an empty graph
    And having executed:
      """
      CREATE (a:End {prop: 42}),
        (b:End {prop: 3}),
        (c:Begin {prop: id(a)})
      """
    When executing query:
      """
      MATCH (a:Begin)
      WITH a.prop AS property
      LIMIT 1
      MATCH (b)
      WHERE id(b) = property
      RETURN b
      """
    Then the result should be:
      | b                 |
      | (:End {prop: 42}) |
    And no side effects

  Scenario: Handle dependencies across WITH with SKIP
    Given an empty graph
    And having executed:
      """
      CREATE (a {prop: 'A', id: 0}),
          (b {prop: 'B', id: id(a)}),
          (c {prop: 'C', id: 0})
      """
    When executing query:
      """
      MATCH (a)
      WITH a.prop AS property, a.id AS idToUse
      ORDER BY property
      SKIP 1
      MATCH (b)
      WHERE id(b) = idToUse
      RETURN DISTINCT b
      """
    Then the result should be:
      | b                    |
      | ({prop: 'A', id: 0}) |
    And no side effects

  Scenario: WHERE after WITH should filter results
    Given an empty graph
    And having executed:
      """
      CREATE (a {name: 'A'}),
        (b {name: 'B'}),
        (c {name: 'C'})
      """
    When executing query:
      """
      MATCH (a)
      WITH a
      WHERE a.name = 'B'
      RETURN a
      """
    Then the result should be:
      | a             |
      | ({name: 'B'}) |
    And no side effects

  Scenario: WHERE after WITH can filter on top of an aggregation
    Given an empty graph
    And having executed:
      """
      CREATE (a {name: 'A'}),
        (b {name: 'B'})
      CREATE (a)-[:REL]->()
      CREATE (a)-[:REL]->()
      CREATE (a)-[:REL]->()
      CREATE (b)-[:REL]->()
      """
    When executing query:
      """
      MATCH (a)-->()
      WITH a, count(*) AS relCount
      WHERE relCount > 1
      RETURN a
      """
    Then the result should be:
      | a             |
      | ({name: 'A'}) |
    And no side effects

  Scenario: ORDER BY on an aggregating key
    Given an empty graph
    And having executed:
      """
      CREATE (a1 {bar: 'A'}),
        (a2 {bar: 'A'}),
        (b {bar: 'B'})
      """
    When executing query:
      """
      MATCH (a)
      WITH a.bar AS bars, count(*) AS relCount
      ORDER BY a.bar
      RETURN *
      """
    Then the result should be:
      | bars | relCount |
      | 'A'  | 2        |
      | 'B'  | 1        |
    And no side effects

  Scenario: ORDER BY a DISTINCT column
    Given an empty graph
    And having executed:
      """
      CREATE (a1 {bar: 'A'}),
        (a2 {bar: 'A'}),
        (b {bar: 'B'})
      """
    When executing query:
      """
      MATCH (a)
      WITH DISTINCT a.bar AS bars
      ORDER BY a.bar
      RETURN *
      """
    Then the result should be:
      | bars |
      | 'A'  |
      | 'B'  |
    And no side effects

  Scenario: WHERE on a DISTINCT column
    Given an empty graph
    And having executed:
      """
      CREATE (a1 {bar: 'A'}),
        (a2 {bar: 'A'}),
        (b {bar: 'B'})
      """
    When executing query:
      """
      MATCH (a)
      WITH DISTINCT a.bar AS bars
      WHERE a.bar = 'B'
      RETURN *
      """
    Then the result should be:
      | bars |
      | 'B'  |
    And no side effects

  Scenario: A simple pattern with one bound endpoint
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL]->(b:B)
      """
    When executing query:
      """
      MATCH (a:A)-[r:REL]->(b:B)
      WITH a AS b, b AS tmp, r AS r
      WITH b AS a, r
      LIMIT 1
      MATCH (a)-[r]->(b)
      RETURN a, r, b
      """
    Then the result should be:
      | a    | r      | b    |
      | (:A) | [:REL] | (:B) |
    And no side effects

  Scenario: Null handling
    Given an empty graph
    When executing query:
      """
      OPTIONAL MATCH (a:Start)
      WITH a
      MATCH (a)-->(b)
      RETURN *
      """
    Then the result should be:
      | a | b | r |
    And no side effects

  Scenario: Nested maps
    Given an empty graph
    When executing query:
      """
      WITH {foo: {bar: 'baz'}} AS nestedMap
      RETURN nestedMap.foo.bar
      """
    Then the result should be:
      | nestedMap.foo.bar |
      | 'baz'             |
    And no side effects

  Scenario: Connected components succeeding WITH
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL]->(x:X)
      CREATE (b:B)
      """
    When executing query:
      """
      MATCH (n:A)
      WITH n
      LIMIT 1
      MATCH (m:B), (n)-->(x:X)
      RETURN *
      """
    Then the result should be:
      | m    | n    | x    |
      | (:B) | (:A) | (:X) |
    And no side effects

  Scenario: Single WITH using a predicate and aggregation
    Given an empty graph
    And having executed:
      """
      CREATE (a {prop: 43}), (b {prop: 42})
      """
    When executing query:
      """
      MATCH (n)
      WITH n
      WHERE n.prop = 42
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 1        |
    And no side effects

  Scenario: Multiple WITHs using a predicate and aggregation
    Given an empty graph
    And having executed:
      """
      CREATE (a {name: 'David'}),
        (b {name: 'Other'}),
        (c {name: 'NotOther'}),
        (d {name: 'NotOther2'}),
        (a)-[:REL]->(b),
        (a)-[:REL]->(c),
        (a)-[:REL]->(d),
        (b)-[:REL]->(),
        (b)-[:REL]->(),
        (c)-[:REL]->(),
        (c)-[:REL]->(),
        (d)-[:REL]->()
      """
    When executing query:
      """
      MATCH (david {name: 'David'})--(otherPerson)-->()
      WITH otherPerson, count(*) AS foaf
      WHERE foaf > 1
      WITH otherPerson
      WHERE otherPerson.name <> 'NotOther'
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 1        |
    And no side effects
