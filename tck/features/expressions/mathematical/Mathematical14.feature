#
# Copyright (c) "Neo4j"
# Neo4j Sweden AB [https://neo4j.com]
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

Feature: Mathematical14 - Logarithm

  Scenario: [1] `exp()` function
    Given any graph
    When executing query:
      """
      RETURN exp(2.96)
      """
    Then the result should be, in any order:
      | exp(2.96)           |
      | 19.297971755502758  |
    And no side effects

  Scenario: [2] `log()` function
    Given any graph
    When executing query:
      """
      RETURN log(27)
      """
    Then the result should be, in any order:
      | log(27)         |
      | 3.295836866004329 |
    And no side effects

  Scenario: [3] `log10()` function
    Given any graph
    When executing query:
      """
      RETURN log10(2.96)
      """
    Then the result should be, in any order:
      | log10(2.96)        |
      | 0.4712917110589386 |
    And no side effects
