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

Feature: Boolean1 - And logical operations

  Scenario: [1] Conjunction of two truth values
    Given any graph
    When executing query:
      """
      RETURN true AND true AS tt, true AND false AS tf, false AND true AS ft, false AND false AS ff
      """
    Then the result should be, in any order:
      | tt   | tf    | ft    | ff    |
      | true | false | false | false |
    And no side effects

  Scenario: [2] Conjunction of three truth values
    Given any graph
    When executing query:
      """
      RETURN true AND true AND true AS ttt,
             true AND true AND false AS ttf,
             true AND false AND true AS tft,
             true AND false AND false AS tff,
             false AND true AND true AS ftt,
             false AND true AND false AS ftf,
             false AND false AND true AS fft,
             false AND false AND false AS fff
      """
    Then the result should be, in any order:
      | ttt  | ttf   | tft   | tff   | ftt   | ftf   | fft   | fff   |
      | true | false | false | false | false | false | false | false |
    And no side effects

  Scenario: [3] Conjunction of many truth values
    Given any graph
    When executing query:
      """
      RETURN true AND true AND true AND true AND true AND true AND true AND true AND true AND true AND true AS t,
             false AND false AND false AND false AND true AND false AND false AND false AND false AND false AND false AS s,
             true AND false AND false AND false AND true AND false AND false AND true AND true AND true AND false AS m,
             false AND false AND false AND false AND false AND false AND false AND false AND false AND false AND false AS f
      """
    Then the result should be, in any order:
      | f    | s     | m     | f     |
      | true | false | false | false |
    And no side effects

  Scenario: [4] Conjunction is commutative
    Given any graph
    When executing query:
      """
      UNWIND [true, false] AS a
      UNWIND [true, false] AS b
      RETURN DISTINCT (a AND b) = (b AND a) AS result
      """
    Then the result should be, in any order:
      | result |
      | true   |
    And no side effects

  Scenario: [5] Conjunction is associative
    Given any graph
    When executing query:
      """
      UNWIND [true, false] AS a
      UNWIND [true, false] AS b
      UNWIND [true, false] AS c
      RETURN DISTINCT (a AND (b AND c)) = ((a AND b) AND c) AS result
      """
    Then the result should be, in any order:
      | result |
      | true   |
    And no side effects

  Scenario Outline: [6] Fail on conjunction of at least one non-booleans
    Given any graph
    When executing query:
      """
      RETURN <a> AND <b>
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

    Examples:
      | a       | b       |
      | 123     | true    |
      | 123.4   | false   |
      | 'foo'   | true    |
      | [true]  | false   |
      | {x: []} | true    |
      | false   | 123     |
      | true    | 123.4   |
      | false   | 'foo'   |
      | true    | [false] |
      | false   | {x: []} |
      | 123     | 'foo'   |
      | 123.4   | 123.4   |
      | 'foo'   | {x: []} |
      | [true]  | [true]  |
      | {x: []} | [123]   |