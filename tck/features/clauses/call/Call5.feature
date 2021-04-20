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

Feature: Call5 - Results projection

  Scenario: [1] Explicit procedure result projection
    Given an empty graph
    And there exists a procedure test.my.proc(in :: INTEGER?) :: (out :: STRING?):
      | in   | out   |
      | null | 'nix' |
    When executing query:
      """
      CALL test.my.proc(null) YIELD out
      RETURN out
      """
    Then the result should be, in order:
      | out   |
      | 'nix' |
    And no side effects

  Scenario: [2] Rename procedure call output in RETURN clause
    Given an empty graph
    And there exists a procedure test.my.proc(in :: INTEGER?) :: (out :: STRING?):
      | in   | out   |
      | null | 'nix' |
    When executing query:
      """
      CALL test.my.proc(null) YIELD out AS a
      RETURN a
      """
    Then the result should be, in order:
      | a     |
      | 'nix' |
    And no side effects

  Scenario: [3] Explicit procedure result projection with RETURN *
    Given an empty graph
    And there exists a procedure test.my.proc(in :: INTEGER?) :: (out :: STRING?):
      | in   | out   |
      | null | 'nix' |
    When executing query:
      """
      CALL test.my.proc(null) YIELD out
      RETURN *
      """
    Then the result should be, in order:
      | out   |
      | 'nix' |
    And no side effects

  Scenario: [4] Project procedure results between query scopes with WITH clause
    Given an empty graph
    And there exists a procedure test.my.proc(in :: INTEGER?) :: (out :: STRING?):
      | in   | out   |
      | null | 'nix' |
    When executing query:
      """
      CALL test.my.proc(null) YIELD out
      WITH out RETURN out
      """
    Then the result should be, in order:
      | out   |
      | 'nix' |
    And no side effects

  Scenario: [5] Project procedure results between query scopes with WITH clause and rename the projection
    Given an empty graph
    And there exists a procedure test.my.proc(in :: INTEGER?) :: (out :: STRING?):
      | in   | out   |
      | null | 'nix' |
    When executing query:
      """
      CALL test.my.proc(null) YIELD out
      WITH out as a RETURN a
      """
    Then the result should be, in order:
      | a     |
      | 'nix' |
    And no side effects

  Scenario Outline: [6] The order of yield items is irrelevant
    Given an empty graph
    And there exists a procedure test.my.proc(in :: INTEGER?) :: (A :: INTEGER?, B :: INTEGER?) :
      | in   | a | b |
      | null | 1 | 2 |
    When executing query:
      """
      CALL test.my.proc(null) YIELD <yield>
      RETURN a, b
      """
    Then the result should be, in order:
      | a | b |
      | 1 | 2 |
    And no side effects
    
    Examples:
      | yield |
      | a, b  |
      | b, a  |
