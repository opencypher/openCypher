#
# Copyright (c) 2015-2021 "Neo Technology,"
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

Feature: Boolean2 - OR logical operations

  Scenario: [1] Disjunction of two truth values
    Given any graph
    When executing query:
      """
      RETURN true OR true AS tt, true OR false AS tf, false OR true AS ft, false OR false AS ff
      """
    Then the result should be, in any order:
      | tt   | tf   | ft   | ff    |
      | true | true | true | false |
    And no side effects

  Scenario: [2] Disjunction of three truth values
    Given any graph
    When executing query:
      """
      RETURN true OR true OR true AS ttt,
             true OR true OR false AS ttf,
             true OR false OR true AS tft,
             true OR false OR false AS tff,
             false OR true OR true AS ftt,
             false OR true OR false AS ftf,
             false OR false OR true AS fft,
             false OR false OR false AS fff
      """
    Then the result should be, in any order:
      | ttt  | ttf  | tft  | tff  | ftt  | ftf  | fft  | fff   |
      | true | true | true | true | true | true | true | false |
    And no side effects

  Scenario: [3] Disjunction of many truth values
    Given any graph
    When executing query:
      """
      RETURN true OR true OR true OR true OR true OR true OR true OR true OR true OR true OR true AS t,
             false OR false OR false OR false OR true OR false OR false OR false OR false OR false OR false AS s,
             true OR false OR false OR false OR true OR false OR false OR true OR true OR true OR false AS m1,
             true OR true OR false OR false OR true OR false OR false OR true OR true OR true OR false AS m2,
             false OR false OR false OR false OR false OR false OR false OR false OR false OR false OR false AS f
      """
    Then the result should be, in any order:
      | t    | s    | m1   | m2   | f     |
      | true | true | true | true | false |
    And no side effects

  Scenario: [4] Disjunction is commutative
    Given any graph
    When executing query:
      """
      UNWIND [true, false] AS a
      UNWIND [true, false] AS b
      RETURN (a OR b) = (b OR a) AS result
      """
    Then the result should be, in any order:
      | result |
      | true   |
      | true   |
      | true   |
      | true   |
    And no side effects

  Scenario: [5] Disjunction is associative
    Given any graph
    When executing query:
      """
      UNWIND [true, false] AS a
      UNWIND [true, false] AS b
      UNWIND [true, false] AS c
      RETURN (a OR (b OR c)) = ((a OR b) OR c) AS result
      """
    Then the result should be, in any order:
      | result |
      | true   |
      | true   |
      | true   |
      | true   |
      | true   |
      | true   |
      | true   |
      | true   |
    And no side effects

  Scenario Outline: [6] Fail on disjunction of at least one non-booleans
    Given any graph
    When executing query:
      """
      RETURN <a> OR <b>
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

    Examples:
      | a       | b       |
      | 123     | true    |
      | 123.4   | false   |
      | 123.4   | null    |
      | 'foo'   | true    |
      | []      | false   |
      | [true]  | false   |
      | [null]  | null    |
      | {}      | true    |
      | {x: []} | true    |
      | false   | 123     |
      | true    | 123.4   |
      | false   | 'foo'   |
      | null    | 'foo'   |
      | true    | []      |
      | true    | [false] |
      | null    | [null]  |
      | false   | {}      |
      | false   | {x: []} |
      | 123     | 'foo'   |
      | 123.4   | 123.4   |
      | 'foo'   | {x: []} |
      | [true]  | [true]  |
      | {x: []} | [123]   |
