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

Feature: Match1 - Match Nodes scenarios

  Scenario: [1] Match non-existent nodes returns empty
    Given an empty graph
    When executing query:
      """
      MATCH (n)
      RETURN n
      """
    Then the result should be, in any order:
      | n |
    And no side effects

  Scenario: [2] Matching all nodes
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

  Scenario: [3] Matching nodes using multiple labels
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

  Scenario: [4] Simple node inline property predicate
    Given an empty graph
    And having executed:
      """
      CREATE ({name: 'bar'}), ({name: 'monkey'}), ({firstname: 'bar'})
      """
    When executing query:
      """
      MATCH (n {name: 'bar'})
      RETURN n
      """
    Then the result should be, in any order:
      | n               |
      | ({name: 'bar'}) |
    And no side effects

  Scenario: [5] Use multiple MATCH clauses to do a Cartesian product
    Given an empty graph
    And having executed:
      """
      CREATE ({num: 1}),
        ({num: 2}),
        ({num: 3})
      """
    When executing query:
      """
      MATCH (n), (m)
      RETURN n.num AS n, m.num AS m
      """
    Then the result should be, in any order:
      | n | m |
      | 1 | 1 |
      | 1 | 2 |
      | 1 | 3 |
      | 2 | 1 |
      | 2 | 2 |
      | 2 | 3 |
      | 3 | 3 |
      | 3 | 1 |
      | 3 | 2 |
    And no side effects
