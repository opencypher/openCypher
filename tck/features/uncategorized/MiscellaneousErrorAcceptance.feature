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

Feature: MiscellaneousErrorAcceptance

  @NegativeTest
  Scenario: Failing when using a path variable that is already bound
    Given any graph
    When executing query:
      """
      MATCH p = (a)
      WITH p, a
      MATCH p = (a)-->(b)
      RETURN a
      """
    Then a SyntaxError should be raised at compile time: VariableAlreadyBound

  @NegativeTest
  Scenario: Failing when using a list as a node
    Given any graph
    When executing query:
      """
      MATCH (n)
      WITH [n] AS users
      MATCH (users)-->(messages)
      RETURN messages
      """
    Then a SyntaxError should be raised at compile time: VariableTypeConflict

  @NegativeTest
  Scenario: Failing when creating without direction
    Given any graph
    When executing query:
      """
      CREATE (a)-[:FOO]-(b)
      """
    Then a SyntaxError should be raised at compile time: RequiresDirectedRelationship

  @NegativeTest
  Scenario: Failing when multiple columns have the same name
    Given any graph
    When executing query:
      """
      RETURN 1 AS a, 2 AS a
      """
    Then a SyntaxError should be raised at compile time: ColumnNameConflict

  @NegativeTest
  Scenario: Failing when using RETURN * without variables in scope
    Given any graph
    When executing query:
      """
      MATCH ()
      RETURN *
      """
    Then a SyntaxError should be raised at compile time: NoVariablesInScope
