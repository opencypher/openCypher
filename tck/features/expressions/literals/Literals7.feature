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

Feature: Literals7 - Negative tests

  @NegativeTest
  Scenario: [16] Fail on a map containing key starting with a number
    Given any graph
    When executing query:
      """
      RETURN {1B2c3e67:1} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  @NegativeTest
  Scenario: [17] Fail on a map containing key with symbol
    Given any graph
    When executing query:
      """
      RETURN {k1#k: 1} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  @NegativeTest
  Scenario: [18] Fail on a map containing key with dot
    Given any graph
    When executing query:
      """
      RETURN {k1.k: 1} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  @NegativeTest
  Scenario: [19] Fail on a map containing unquoted string
    Given any graph
    When executing query:
      """
      RETURN {k1: k2} AS literal
      """
    Then a SyntaxError should be raised at compile time: UndefinedVariable

  @NegativeTest
  Scenario: [20] Fail on a map containing only a comma
    Given any graph
    When executing query:
      """
      RETURN {, } AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  @NegativeTest
  Scenario: [21] Fail on a map containing a value without key
    Given any graph
    When executing query:
      """
      RETURN {1} AS literal
      """
    Then a SyntaxError should be raised at compile time: MissingParameter

  @NegativeTest
  Scenario: [22] Fail on a map containing a list without key
    Given any graph
    When executing query:
      """
      RETURN {[]} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  @NegativeTest
  Scenario: [23] Fail on a map containing a map without key
    Given any graph
    When executing query:
      """
      RETURN {{}} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  @NegativeTest
  Scenario: [24] Fail on a nested map with non-matching braces
    Given any graph
    When executing query:
      """
      RETURN {k: {k: {}} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax
