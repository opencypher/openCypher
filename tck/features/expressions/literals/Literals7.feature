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

  Scenario: [1] Return a too large integer
    Given any graph
    When executing query:
      """
      RETURN 9223372036854775808 AS literal
      """
    Then a SyntaxError should be raised at compile time: IntegerOverflow

  Scenario: [2] Return a too small integer
    Given any graph
    When executing query:
      """
      RETURN -9223372036854775809 AS literal
      """
    Then a SyntaxError should be raised at compile time: IntegerOverflow

  Scenario: [3] Return an integer containing a alphabetic character
    Given any graph
    When executing query:
      """
      RETURN 9223372h54775808 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [4] Return an integer containing a invalid symbol character
    Given any graph
    When executing query:
      """
      RETURN 9223372#54775808 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [5] Return an incomplete hexadecimal integer
    Given any graph
    When executing query:
      """
      RETURN 0x AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [6] Return an hexadecimal literal containing a lower case invalid alphanumeric character
    Given any graph
    When executing query:
      """
      RETURN 0x1A2b3j4D5E6f7 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [7] Return an hexadecimal literal containing a upper case invalid alphanumeric character
    Given any graph
    When executing query:
      """
      RETURN 0x1A2b3c4Z5E6f7 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

### Error detail not necessarily recognizable
#  Scenario: [8] Returning an hexadecimal literal containing a invalid symbol character
#    Given any graph
#    When executing query:
#      """
#      RETURN 0x1A2b3c4#5E6f7 AS literal
#      """
#    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [9] Return a too large hexadecimal integer
    Given any graph
    When executing query:
      """
      RETURN 0x8000000000000000 AS literal
      """
    Then a SyntaxError should be raised at compile time: IntegerOverflow

  Scenario: [10] Return a too small hexadecimal integer
    Given any graph
    When executing query:
      """
      RETURN -0x8000000000000001 AS literal
      """
    Then a SyntaxError should be raised at compile time: IntegerOverflow

  Scenario: [11] Return a too large octal integer
    Given any graph
    When executing query:
      """
      RETURN 01000000000000000000000 AS literal
      """
    Then a SyntaxError should be raised at compile time: IntegerOverflow

  Scenario: [12] Return a too small octal integer
    Given any graph
    When executing query:
      """
      RETURN -01000000000000000000001 AS literal
      """
    Then a SyntaxError should be raised at compile time: IntegerOverflow

  Scenario: [13] Return a list containing a comma
    Given any graph
    When executing query:
      """
      RETURN [, ] AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  Scenario: [14] Return a nested list with non-matching brackets
    Given any graph
    When executing query:
      """
      RETURN [[[]] AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  Scenario: [15] Return a nested list with missing commas
    Given any graph
    When executing query:
      """
      RETURN [[','[]',']] AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  Scenario: [16] Return a map containing key starting with a number
    Given any graph
    When executing query:
      """
      RETURN {1B2c3e67:1} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  Scenario: [17] Return a map containing key with symbol
    Given any graph
    When executing query:
      """
      RETURN {k1#k: 1} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  Scenario: [18] Return a map containing key with dot
    Given any graph
    When executing query:
      """
      RETURN {k1.k: 1} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  Scenario: [19] Return a map containing unquoted string
    Given any graph
    When executing query:
      """
      RETURN {k1: k2} AS literal
      """
    Then a SyntaxError should be raised at compile time: UndefinedVariable

  Scenario: [20] Return a map containing a comma
    Given any graph
    When executing query:
      """
      RETURN {, } AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  Scenario: [21] Return a map containing a value without key
    Given any graph
    When executing query:
      """
      RETURN {1} AS literal
      """
    Then a SyntaxError should be raised at compile time: MissingParameter

  Scenario: [22] Return a map containing a list without key
    Given any graph
    When executing query:
      """
      RETURN {[]} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  Scenario: [23] Return a map containing a map without key
    Given any graph
    When executing query:
      """
      RETURN {{}} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax

  Scenario: [24] Return a nested map with non-matching braces
    Given any graph
    When executing query:
      """
      RETURN {k: {k: {}} AS literal
      """
    Then a SyntaxError should be raised at compile time: UnexpectedSyntax
