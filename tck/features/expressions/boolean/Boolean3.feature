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

Feature: Boolean3 - XOR logical operations

  Scenario: [1] Disjunction of two truth values
    Given any graph
    When executing query:
      """
      RETURN true XOR true AS tt, true XOR false AS tf, false XOR true AS ft, false XOR false AS ff
      """
    Then the result should be, in any order:
      | tt    | tf   | ft   | ff    |
      | false | true | true | false |
    And no side effects

  Scenario: [2] Disjunction of three truth values
    Given any graph
    When executing query:
      """
      RETURN true XOR true XOR true AS ttt,
             true XOR true XOR false AS ttf,
             true XOR false XOR true AS tft,
             true XOR false XOR false AS tff,
             false XOR true XOR true AS ftt,
             false XOR true XOR false AS ftf,
             false XOR false XOR true AS fft,
             false XOR false XOR false AS fff
      """
    Then the result should be, in any order:
      | ttt   | ttf   | tft   | tff  | ftt   | ftf  | fft  | fff   |
      | false | false | false | true | false | true | true | false |
    And no side effects

  Scenario: [3] Disjunction of many truth values
    Given any graph
    When executing query:
      """
      RETURN true XOR true XOR true XOR true XOR true XOR true XOR true XOR true XOR true XOR true XOR true AS t,
             false XOR false XOR false XOR false XOR true XOR false XOR false XOR false XOR false XOR false XOR false AS s,
             true XOR false XOR false XOR false XOR true XOR false XOR false XOR true XOR true XOR true XOR false AS m,
             false XOR false XOR false XOR false XOR false XOR false XOR false XOR false XOR false XOR false XOR false AS f
      """
    Then the result should be, in any order:
      | f    | s     | m    | f     |
      | true | false | true | false |
    And no side effects
