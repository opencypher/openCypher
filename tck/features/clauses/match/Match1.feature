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

Feature: Match1 - Zero-length patterns

  Background:
    Given an empty graph

  Scenario: [1] Matching all nodes of an empty graph
    When executing query:
      """
      MATCH (n)
      RETURN n
      """
    Then the result should be:
      | n |
    And no side effects

  Scenario: [2] Matching all nodes
    And having executed:
      """
      CREATE (:A), (:B)
      """
    When executing query:
      """
      MATCH (n)
      RETURN n
      """
    Then the result should be:
      | n    |
      | (:A) |
      | (:B) |
    And no side effects

  Scenario: [3] Matching nodes using multiple labels
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
    Then the result should be:
      | a        |
      | (:A:B:C) |
    And no side effects

  Scenario: [4] Simple node property predicate
    And having executed:
      """
      CREATE ({foo: 'bar'})
      """
    When executing query:
      """
      MATCH (n)
      WHERE n.foo = 'bar'
      RETURN n
      """
    Then the result should be:
      | n              |
      | ({foo: 'bar'}) |
    And no side effects

  Scenario: [5] Returning a node property value
    And having executed:
      """
      CREATE ({prop: 1})
      """
    When executing query:
      """
      MATCH (a)
      RETURN a.prop
      """
    Then the result should be:
      | a.prop |
      | 1      |
    And no side effects

  Scenario: [6] Returning multiple node property values
    And having executed:
      """
      CREATE ({name: 'Philip J. Fry', age: 2046, seasons: [1, 2, 3, 4, 5, 6, 7]})
      """
    When executing query:
      """
      MATCH (a)
      RETURN a.name, a.age, a.seasons
      """
    Then the result should be:
      | a.name          | a.age | a.seasons             |
      | 'Philip J. Fry' | 2046  | [1, 2, 3, 4, 5, 6, 7] |

  Scenario: [7] Handle OR in the WHERE clause
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
    Then the result should be:
      | n             |
      | (:A {p1: 12}) |
      | (:B {p2: 13}) |
    And no side effects