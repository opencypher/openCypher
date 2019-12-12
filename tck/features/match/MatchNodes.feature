#
# Copyright (c) 2015-2019 "Neo Technology,"
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

Feature: MatchBasic

  Scenario: Do not return non-existent nodes
    Given an empty graph
    When executing query:
      """
      MATCH (n)
      RETURN n
      """
    Then the result should be, in any order:
      | n |
    And no side effects

  Scenario: Matching all nodes
    Given an empty graph
    And having executed:
      """
      CREATE (:A), (:B)
      """
    When executing query:
      """
      MATCH (n)
      RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:A) |
      | (:B) |
    And no side effects

  Scenario: Simple node property predicate
    Given an empty graph
    And having executed:
      """
      CREATE ({name: 'bar'})
      """
    When executing query:
      """
      MATCH (n)
      WHERE n.name = 'bar'
      RETURN n
      """
    Then the result should be, in any order:
      | n               |
      | ({name: 'bar'}) |
    And no side effects

  Scenario: Adding a property and a literal in projection
    Given an empty graph
    And having executed:
      """
      CREATE ({num: 1})
      """
    When executing query:
      """
      MATCH (a)
      RETURN a.num + 1 AS foo
      """
    Then the result should be, in any order:
      | foo |
      | 2   |
    And no side effects

  Scenario: Adding list properties in projection
    Given an empty graph
    And having executed:
      """
      CREATE ({list1: [1, 2, 3], list2: [4, 5]})
      """
    When executing query:
      """
      MATCH (a)
      RETURN a.list2 + a.list1 AS foo
      """
    Then the result should be, in any order:
      | foo             |
      | [4, 5, 1, 2, 3] |
    And no side effects

  Scenario: Returning multiple node property values
    Given an empty graph
    And having executed:
      """
      CREATE ({name: 'Philip J. Fry', age: 2046, seasons: [1, 2, 3, 4, 5, 6, 7]})
      """
    When executing query:
      """
      MATCH (a)
      RETURN a.name, a.age, a.seasons
      """
    Then the result should be, in any order:
      | a.name          | a.age | a.seasons             |
      | 'Philip J. Fry' | 2046  | [1, 2, 3, 4, 5, 6, 7] |
    And no side effects

  Scenario: Missing node property should become null
    Given an empty graph
    And having executed:
      """
      CREATE ({num: 1})
      """
    When executing query:
      """
      MATCH (a)
      RETURN a.name
      """
    Then the result should be, in any order:
      | a.name |
      | null    |
    And no side effects

  Scenario: Matching nodes using multiple labels
    Given an empty graph
    And having executed:
      """
      CREATE (:A:B:C), (:A:B), (:A:C), (:B:C),
             (:A), (:B), (:C)
      """
    When executing query:
      """
      MATCH (a:A:B:C)
      RETURN a
      """
    Then the result should be, in any order:
      | a        |
      | (:A:B:C) |
    And no side effects

  Scenario: Returning label predicate expression
    Given an empty graph
    And having executed:
      """
      CREATE (), (:Foo)
      """
    When executing query:
      """
      MATCH (n)
      RETURN (n:Foo)
      """
    Then the result should be, in any order:
      | (n:Foo) |
      | true    |
      | false   |
    And no side effects

  Scenario: Matching with aggregation
    Given an empty graph
    And having executed:
      """
      CREATE ({num: 42})
      """
    When executing query:
      """
      MATCH (n)
      RETURN n.num AS n, count(n) AS count
      """
    Then the result should be, in any order:
      | n  | count |
      | 42 | 1     |
    And no side effects


  Scenario: Returning a node property value
    Given an empty graph
    And having executed:
      """
      CREATE ({num: 1})
      """
    When executing query:
      """
      MATCH (a)
      RETURN a.num
      """
    Then the result should be, in any order:
      | a.num |
      | 1     |
    And no side effects


  Scenario: Counting an empty graph
    Given an empty graph
    When executing query:
      """
      MATCH (a)
      RETURN count(a) > 0
      """
    Then the result should be, in any order:
      | count(a) > 0 |
      | false        |
    And no side effects


  Scenario: Matching and returning ordered results, with LIMIT
    Given an empty graph
    And having executed:
      """
      CREATE ({num: 1}), ({num: 3}), ({num: 2})
      """
    When executing query:
      """
      MATCH (foo)
      RETURN foo.num AS x
        ORDER BY x DESC
        LIMIT 4
      """
    Then the result should be, in order:
      | x |
      | 3 |
      | 2 |
      | 1 |
    And no side effects