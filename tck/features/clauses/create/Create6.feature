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

Feature: Create6 - Negative tests

  Scenario: [1] Fail when creating a relationship without a type
    Given any graph
    When executing query:
      """
      CREATE ()-->()
      """
    Then a SyntaxError should be raised at compile time: NoSingleRelationshipType

  Scenario: [2] Fail when creating a relationship without a direction
    Given any graph
    When executing query:
      """
      CREATE ({id: 2})-[r:KNOWS]-({id: 1})
      RETURN r
      """
    Then a SyntaxError should be raised at compile time: RequiresDirectedRelationship

  Scenario: [3] Fail when creating a relationship with more than one type
    Given any graph
    When executing query:
      """
      CREATE ()-[:A|:B]->()
      """
    Then a SyntaxError should be raised at compile time: NoSingleRelationshipType

  Scenario: [4] Fail when creating a variable-length relationship
    Given any graph
    When executing query:
      """
      CREATE ()-[:FOO*2]->()
      """
    Then a SyntaxError should be raised at compile time: CreatingVarLength

  Scenario: [5] Fail when creating a node that is already bound
    Given any graph
    When executing query:
      """
      MATCH (a)
      CREATE (a)
      """
    Then a SyntaxError should be raised at compile time: VariableAlreadyBound

  Scenario: [6] Fail when creating a relationship that is already bound
    Given any graph
    When executing query:
      """
      MATCH ()-[r]->()
      CREATE ()-[r]->()
      """
    Then a SyntaxError should be raised at compile time: VariableAlreadyBound
