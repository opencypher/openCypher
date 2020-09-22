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

Feature: MatchWhere1 - Filter single variable

  Scenario: Filter node with node label predicate on multi-column binding table
    Given an empty graph
    And having executed:
      """
      CREATE (:A {id: 0})<-[:ADMIN]-(:B {id: 1})-[:ADMIN]->(:C {id: 2, a: 'A'})
      """
    When executing query:
      """
      MATCH (a)-[:ADMIN]-(b)
      WHERE a:A
      RETURN a.id, b.id
      """
    Then the result should be, in any order:
      | a.id | b.id |
      | 0    | 1    |
    And no side effects

  Scenario: Filter node with node label predicate on empty multi-column binding table
    Given an empty graph
    And having executed:
      """
      CREATE (:A)
      """
    When executing query:
      """
      MATCH (a)<--()<--(b)-->()-->(c)
      WHERE a:A
      RETURN c
      """
    Then the result should be, in any order:
      | c |
    And no side effects

  Scenario: Filter node with property predicate on single-column binding table
    Given an empty graph
    And having executed:
      """
      CREATE (), ({name: 'Bar'}), (:Bar)
      """
    When executing query:
      """
      MATCH (n)
      WHERE n.name = 'Bar'
      RETURN n
      """
    Then the result should be, in any order:
      | n               |
      | ({name: 'Bar'}) |
    And no side effects

  Scenario: Filter node with property predicate on multi-column binding table 1
    Given an empty graph
    And having executed:
      """
      CREATE (a:Person {name: 'Alice'}), (b:Person {name: 'Bob'}),
             (c), (d)
      CREATE (a)-[:T]->(c),
             (b)-[:T]->(d)
      """
    When executing query:
      """
      MATCH (n:Person)-->()
      WHERE n.name = 'Bob'
      RETURN n
      """
    Then the result should be, in any order:
      | n                       |
      | (:Person {name: 'Bob'}) |
    And no side effects

  Scenario: Filter node with property predicate on multi-column binding table 2
    Given an empty graph
    And having executed:
      """
      CREATE ({name: 'Someone'})<-[:X]-()-[:X]->({name: 'Andres'})
      """
    When executing query:
      """
      MATCH ()-[rel:X]-(a)
      WHERE a.name = 'Andres'
      RETURN a
      """
    Then the result should be, in any order:
      | a                  |
      | ({name: 'Andres'}) |
    And no side effects

  Scenario: Filter node with a parameter in a property predicate
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T {name: 'bar'}]->(:B {name: 'me'})
      """
    And parameters are:
      | param | 'me' |
    When executing query:
      """
      MATCH (a)-[r]->(b)
      WHERE b.name = $param
      RETURN r
      """
    Then the result should be, in any order:
      | r                  |
      | [:T {name: 'bar'}] |
    And no side effects

  Scenario: Filter for an unbound relationship
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (b:B {id: 1}), (:B {id: 2})
      CREATE (a)-[:T]->(b)
      """
    When executing query:
      """
      MATCH (a:A), (other:B)
      OPTIONAL MATCH (a)-[r]->(other)
      WITH other WHERE r IS NULL
      RETURN other
      """
    Then the result should be, in any order:
      | other        |
      | (:B {id: 2}) |
    And no side effects

  Scenario: Filter relationship with relationship type predicate
    Given an empty graph
    And having executed:
      """
      CREATE (a:A {name: 'A'}),
        (b:B {name: 'B'}),
        (c:C {name: 'C'}),
        (a)-[:KNOWS]->(b),
        (a)-[:HATES]->(c)
      """
    When executing query:
      """
      MATCH (n {name: 'A'})-[r]->(x)
      WHERE type(r) = 'KNOWS'
      RETURN x
      """
    Then the result should be, in any order:
      | x                |
      | (:B {name: 'B'}) |
    And no side effects

  Scenario: Filter relationship with property predicate
    Given an empty graph
    And having executed:
      """
      CREATE (:A)<-[:KNOWS {name: 'monkey'}]-()-[:KNOWS {name: 'woot'}]->(:B)
      """
    When executing query:
      """
      MATCH (node)-[r:KNOWS]->(a)
      WHERE r.name = 'monkey'
      RETURN a
      """
    Then the result should be, in any order:
      | a    |
      | (:A) |
    And no side effects

  Scenario: Filter relationship with a parameter in a property predicate
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T {name: 'bar'}]->(:B {name: 'me'})
      """
    And parameters are:
      | param | 'bar' |
    When executing query:
      """
      MATCH (a)-[r]->(b)
      WHERE r.name = $param
      RETURN b
      """
    Then the result should be, in any order:
      | b                 |
      | (:B {name: 'me'}) |
    And no side effects

  Scenario: Filter node with disjunctive property predicate
    Given an empty graph
    And having executed:
      """
      CREATE (a:A {p1: 12}),
        (b:B {p2: 13}),
        (c:C)
      """
    When executing query:
      """
      MATCH (n)
      WHERE n.p1 = 12 OR n.p2 = 13
      RETURN n
      """
    Then the result should be, in any order:
      | n             |
      | (:A {p1: 12}) |
      | (:B {p2: 13}) |
    And no side effects

  Scenario: Filter relationship with disjunctive relationship type predicate
    Given an empty graph
    And having executed:
      """
      CREATE (a {name: 'A'}),
        (b {name: 'B'}),
        (c {name: 'C'}),
        (a)-[:KNOWS]->(b),
        (a)-[:HATES]->(c),
        (a)-[:WONDERS]->(c)
      """
    When executing query:
      """
      MATCH (n)-[r]->(x)
      WHERE type(r) = 'KNOWS' OR type(r) = 'HATES'
      RETURN r
      """
    Then the result should be, in any order:
      | r        |
      | [:KNOWS] |
      | [:HATES] |
    And no side effects

  Scenario: Filter path with path length predicate
    Given an empty graph
    And having executed:
      """
      CREATE (a:A {name: 'A'})-[:KNOWS]->(b:B {name: 'B'})
      """
    When executing query:
      """
      MATCH p = (n)-->(x)
      WHERE length(p) = 1
      RETURN x
      """
    Then the result should be, in any order:
      | x                |
      | (:B {name: 'B'}) |
    And no side effects

  Scenario: Filter path with false path length predicate
    Given an empty graph
    And having executed:
      """
      CREATE (a:A {name: 'A'})-[:KNOWS]->(b:B {name: 'B'})
      """
    When executing query:
      """
      MATCH p = (n)-->(x)
      WHERE length(p) = 10
      RETURN x
      """
    Then the result should be, in any order:
      | x |
    And no side effects

  @NegativeTest
  Scenario: Fail when filtering path with property predicate
    Given any graph
    When executing query:
      """
      MATCH (n)
      MATCH (n)-[r*]->()
      WHERE r.name = 'apa'
      RETURN r
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  @NegativeTest
  Scenario: Fail on aggregation in WHERE
    Given any graph
    When executing query:
      """
      MATCH (a)
      WHERE count(a) > 10
      RETURN a
      """
    Then a SyntaxError should be raised at compile time: InvalidAggregation

