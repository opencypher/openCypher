#
# Copyright (c) 2015-2019 "Neo Technology,"
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

  Background:
    Given any graph

  Scenario: [1] Returning a too large integer
    When executing query:
      """
      RETURN 9223372036854775808 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [2] Returning a too small integer
    When executing query:
      """
      RETURN -9223372036854775809 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [3] Returning an integer containing a alphabetic character
    When executing query:
      """
      RETURN 9223372h54775808 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [4] Returning an integer containing a invalid symbol character
    When executing query:
      """
      RETURN 9223372#54775808 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [5] Returning an incomplete hexadecimal integer
    When executing query:
      """
      RETURN 0x AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [6] Returning an hexadecimal literal containing a lower case invalid alphanumeric character
    When executing query:
      """
      RETURN 0x1A2b3j4D5E6f7 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [7] Returning an hexadecimal literal containing a upper case invalid alphanumeric character
    When executing query:
      """
      RETURN 0x1A2b3c4Z5E6f7 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [8] Returning an hexadecimal literal containing a invalid symbol character
    When executing query:
      """
      RETURN 0x1A2b3c4?5E6f7 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [9] Returning a too large hexadecimal integer
    When executing query:
      """
      RETURN 0x8000000000000000 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [10] Returning a too small hexadecimal integer
    When executing query:
      """
      RETURN -0x8000000000000001 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [11] Returning a too large hexadecimal integer
    When executing query:
      """
      RETURN 0800000000000000000000 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral

  Scenario: [12] Returning a too small hexadecimal integer
    When executing query:
      """
      RETURN -0800000000000000000001 AS literal
      """
    Then a SyntaxError should be raised at compile time: InvalidNumberLiteral
