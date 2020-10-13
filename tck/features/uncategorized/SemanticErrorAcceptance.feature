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

Feature: SemanticErrorAcceptance

  @NegativeTest
  Scenario: Failing when returning an undefined variable
    Given any graph
    When executing query:
      """
      MATCH ()
      RETURN foo
      """
    Then a SyntaxError should be raised at compile time: UndefinedVariable

  @NegativeTest
  Scenario: Failing when using IN on a string literal
    Given any graph
    When executing query:
      """
      MATCH (n)
      WHERE n.id IN ''
      RETURN 1
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  @NegativeTest
  Scenario: Failing when using IN on an integer literal
    Given any graph
    When executing query:
      """
      MATCH (n)
      WHERE n.id IN 1
      RETURN 1
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  @NegativeTest
  Scenario: Failing when using IN on a float literal
    Given any graph
    When executing query:
      """
      MATCH (n)
      WHERE n.id IN 1.0
      RETURN 1
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  @NegativeTest
  Scenario: Failing when using IN on a boolean literal
    Given any graph
    When executing query:
      """
      MATCH (n)
      WHERE n.id IN true
      RETURN 1
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  @NegativeTest
  Scenario: Failing when a node is used as a relationship
    Given any graph
    When executing query:
      """
      MATCH (r)
      MATCH ()-[r]-()
      RETURN r
      """
    Then a SyntaxError should be raised at compile time: VariableTypeConflict

  @NegativeTest
  Scenario: Failing when a relationship is used as a node
    Given any graph
    When executing query:
      """
      MATCH ()-[r]-(r)
      RETURN r
      """
    Then a SyntaxError should be raised at compile time: VariableTypeConflict

  @NegativeTest
  Scenario: Failing when using `type()` on a node
    Given any graph
    When executing query:
      """
      MATCH (r)
      RETURN type(r)
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

  @NegativeTest
  Scenario: Bad arguments for `range()`
    Given any graph
    When executing query:
      """
      RETURN range(2, 8, 0)
      """
    Then a ArgumentError should be raised at runtime: NumberOutOfRange

  @NegativeTest
  Scenario: Failing when using aggregation in list comprehension
    Given any graph
    When executing query:
      """
      MATCH (n)
      RETURN [x IN [1, 2, 3, 4, 5] | count(*)]
      """
    Then a SyntaxError should be raised at compile time: InvalidAggregation

  @NegativeTest
  Scenario: Failing when using non-constants in SKIP
    Given any graph
    When executing query:
      """
      MATCH (n)
      RETURN n
        SKIP n.count
      """
    Then a SyntaxError should be raised at compile time: NonConstantExpression

  @NegativeTest
  Scenario: Failing when using negative value in SKIP
    Given any graph
    When executing query:
      """
      MATCH (n)
      RETURN n
        SKIP -1
      """
    Then a SyntaxError should be raised at compile time: NegativeIntegerArgument

  @NegativeTest
  Scenario: Failing when using non-constants in LIMIT
    Given any graph
    When executing query:
      """
      MATCH (n)
      RETURN n
        LIMIT n.count
      """
    Then a SyntaxError should be raised at compile time: NonConstantExpression

  @NegativeTest
  Scenario: Failing when using negative value in LIMIT
    Given any graph
    When executing query:
      """
      MATCH (n)
      RETURN n
        LIMIT -1
      """
    Then a SyntaxError should be raised at compile time: NegativeIntegerArgument

  @NegativeTest
  Scenario: Failing when using floating point in LIMIT
    Given any graph
    When executing query:
      """
      MATCH (n)
      RETURN n
        LIMIT 1.7
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType
