#
# Copyright (c) 2015-2018 "Neo Technology,"
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

Feature: ListOperations

  Background:
    Given any graph

  Scenario: IN should work with nested list subscripting
    When executing query:
      """
      WITH [[1, 2, 3]] AS list
      RETURN 3 IN list[0] AS r
      """
    Then the result should be:
      | r    |
      | true |
    And no side effects

  Scenario: IN should work with nested literal list subscripting
    When executing query:
      """
      RETURN 3 IN [[1, 2, 3]][0] AS r
      """
    Then the result should be:
      | r    |
      | true |
    And no side effects

  Scenario: IN should work with list slices
    When executing query:
      """
      WITH [1, 2, 3] AS list
      RETURN 3 IN list[0..1] AS r
      """
    Then the result should be:
      | r     |
      | false |
    And no side effects

  Scenario: IN should work with literal list slices
    When executing query:
      """
      RETURN 3 IN [1, 2, 3][0..1] AS r
      """
    Then the result should be:
      | r     |
      | false |
    And no side effects

  Scenario: Return collection size
    Given any graph
    When executing query:
      """
      RETURN size([1, 2, 3]) AS n
      """
    Then the result should be:
      | n |
      | 3 |
    And no side effects

  Scenario: Setting and returning the size of a list property
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH (n)
      SET n.x = [1, 2, 3]
      RETURN size(n.x)
      """
    Then the result should be:
      | size(n.x) |
      | 3         |
    And the side effects should be:
      | +properties | 1 |

  Scenario: Concatenating and returning the size of literal lists
    Given any graph
    When executing query:
      """
      RETURN size([[], []] + [[]]) AS l
      """
    Then the result should be:
      | l |
      | 3 |
    And no side effects

  Scenario: Returning nested expressions based on list property
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH (n)
      SET n.array = [1, 2, 3, 4, 5]
      RETURN tail(tail(n.array))
      """
    Then the result should be:
      | tail(tail(n.array)) |
      | [3, 4, 5]           |
    And the side effects should be:
      | +properties | 1 |

  Scenario: Indexing into nested literal lists
    Given any graph
    When executing query:
      """
      RETURN [[1]][0][0]
      """
    Then the result should be:
      | [[1]][0][0] |
      | 1           |
    And no side effects

  Scenario: Concatenating lists of same type
    Given any graph
    When executing query:
      """
      RETURN [1, 10, 100] + [4, 5] AS foo
      """
    Then the result should be:
      | foo                |
      | [1, 10, 100, 4, 5] |
    And no side effects

  Scenario: Appending lists of same type
    Given any graph
    When executing query:
      """
      RETURN [false, true] + false AS foo
      """
    Then the result should be:
      | foo                  |
      | [false, true, false] |
    And no side effects

  Scenario: Execute n[0]
    When executing query:
      """
      RETURN [1, 2, 3][0] AS value
      """
    Then the result should be:
      | value |
      | 1     |
    And no side effects

  Scenario: Use collection lookup based on parameters when there is no type information
    And parameters are:
      | expr | ['Apa'] |
      | idx  | 0       |
    When executing query:
      """
      WITH $expr AS expr, $idx AS idx
      RETURN expr[idx] AS value
      """
    Then the result should be:
      | value |
      | 'Apa' |
    And no side effects

  Scenario: Use collection lookup based on parameters when there is lhs type information
    And parameters are:
      | idx | 0 |
    When executing query:
      """
      WITH ['Apa'] AS expr
      RETURN expr[$idx] AS value
      """
    Then the result should be:
      | value |
      | 'Apa' |
    And no side effects

  Scenario: Use collection lookup based on parameters when there is rhs type information
    And parameters are:
      | expr | ['Apa'] |
      | idx  | 0       |
    When executing query:
      """
      WITH $expr AS expr, $idx AS idx
      RETURN expr[toInteger(idx)] AS value
      """
    Then the result should be:
      | value |
      | 'Apa' |
    And no side effects

  Scenario: Fail at runtime when attempting to index with a String into a Collection
    And parameters are:
      | expr | ['Apa'] |
      | idx  | 'name'  |
    When executing query:
      """
      WITH $expr AS expr, $idx AS idx
      RETURN expr[idx]
      """
    Then a TypeError should be raised at runtime: ListElementAccessByNonInteger

  Scenario: Fail at runtime when trying to index into a list with a list
    And parameters are:
      | expr | ['Apa'] |
      | idx  | ['Apa'] |
    When executing query:
      """
      WITH $expr AS expr, $idx AS idx
      RETURN expr[idx]
      """
    Then a TypeError should be raised at runtime: ListElementAccessByNonInteger

  Scenario: Fail at compile time when attempting to index with a non-integer into a list
    When executing query:
      """
      WITH [1, 2, 3, 4, 5] AS list, 3.14 AS idx
      RETURN list[idx]
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType
