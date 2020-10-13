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

Feature: WithAcceptance

  # WithOrderByLimit
  Scenario: ORDER BY and LIMIT can be used
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (), (), (),
             (a)-[:REL]->()
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
    Then the result should be, in any order:
      | a    |
      | (:A) |
    And no side effects

  # WithOrderByLimit (does this scenario realy testing LIMIT)
  Scenario: Handle dependencies across WITH with LIMIT
    Given an empty graph
    And having executed:
      """
      CREATE (a:End {num: 42, id: 0}),
             (:End {num: 3}),
             (:Begin {num: a.id})
      """
    When executing query:
      """
      MATCH (a:Begin)
      WITH a.num AS property
        LIMIT 1
      MATCH (b)
      WHERE b.id = property
      RETURN b
      """
    Then the result should be, in any order:
      | b                       |
      | (:End {num: 42, id: 0}) |
    And no side effects

  # WithOrderBySkip
  Scenario: Handle dependencies across WITH with SKIP
    Given an empty graph
    And having executed:
      """
      CREATE (a {name: 'A', num: 0, id: 0}),
             ({name: 'B', num: a.id, id: 1}),
             ({name: 'C', num: 0, id: 2})
      """
    When executing query:
      """
      MATCH (a)
      WITH a.name AS property, a.num AS idToUse
        ORDER BY property
        SKIP 1
      MATCH (b)
      WHERE b.id = idToUse
      RETURN DISTINCT b
      """
    Then the result should be, in any order:
      | b                            |
      | ({name: 'A', num: 0, id: 0}) |
    And no side effects

  # WithWhere
  Scenario: WHERE after WITH should filter results
    Given an empty graph
    And having executed:
      """
      CREATE ({name: 'A'}),
             ({name: 'B'}),
             ({name: 'C'})
      """
    When executing query:
      """
      MATCH (a)
      WITH a
      WHERE a.name = 'B'
      RETURN a
      """
    Then the result should be, in any order:
      | a             |
      | ({name: 'B'}) |
    And no side effects

  # WithWhere
  Scenario: WHERE after WITH can filter on top of an aggregation
    Given an empty graph
    And having executed:
      """
      CREATE (a {name: 'A'}),
             (b {name: 'B'})
      CREATE (a)-[:REL]->(),
             (a)-[:REL]->(),
             (a)-[:REL]->(),
             (b)-[:REL]->()
      """
    When executing query:
      """
      MATCH (a)-->()
      WITH a, count(*) AS relCount
      WHERE relCount > 1
      RETURN a
      """
    Then the result should be, in any order:
      | a             |
      | ({name: 'A'}) |
    And no side effects

  # WithOrderBy
  Scenario: ORDER BY on an aggregating key
    Given an empty graph
    And having executed:
      """
      CREATE ({name: 'A'}),
             ({name: 'A'}),
             ({name: 'B'})
      """
    When executing query:
      """
      MATCH (a)
      WITH a.name AS bars, count(*) AS relCount
      ORDER BY a.name
      RETURN *
      """
    Then the result should be, in any order:
      | bars | relCount |
      | 'A'  | 2        |
      | 'B'  | 1        |
    And no side effects

  # WithOrderBy
  Scenario: ORDER BY a DISTINCT column
    Given an empty graph
    And having executed:
      """
      CREATE ({name: 'A'}),
             ({name: 'A'}),
             ({name: 'B'})
      """
    When executing query:
      """
      MATCH (a)
      WITH DISTINCT a.name AS bars
      ORDER BY a.name
      RETURN *
      """
    Then the result should be, in any order:
      | bars |
      | 'A'  |
      | 'B'  |
    And no side effects

  # WithWhere
  Scenario: WHERE on a DISTINCT column
    Given an empty graph
    And having executed:
      """
      CREATE ({name2: 'A'}),
             ({name2: 'A'}),
             ({name2: 'B'})
      """
    When executing query:
      """
      MATCH (a)
      WITH DISTINCT a.name2 AS bars
      WHERE a.name2 = 'B'
      RETURN *
      """
    Then the result should be, in any order:
      | bars |
      | 'B'  |
    And no side effects

  # WithLimit
  Scenario: Connected components succeeding WITH with LIMIT
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:REL]->(:X)
      CREATE (:B)
      """
    When executing query:
      """
      MATCH (n:A)
      WITH n
      LIMIT 1
      MATCH (m:B), (n)-->(x:X)
      RETURN *
      """
    Then the result should be, in any order:
      | m    | n    | x    |
      | (:B) | (:A) | (:X) |
    And no side effects

  # WithWhere
  Scenario: Single WITH using a predicate and aggregation
    Given an empty graph
    And having executed:
      """
      CREATE ({num: 43}), ({num: 42})
      """
    When executing query:
      """
      MATCH (n)
      WITH n
      WHERE n.num = 42
      RETURN count(*)
      """
    Then the result should be, in any order:
      | count(*) |
      | 1        |
    And no side effects
