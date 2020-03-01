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

Feature: Match10 - Match clause failure scenarios

  Scenario: [1] Fail when asterisk operator is missing
    Given an empty graph
    And having executed:
      """
      CREATE (n0:A {name: 'n0'}),
             (n00:B {name: 'n00'}),
             (n01:B {name: 'n01'}),
             (n000:C {name: 'n000'}),
             (n001:C {name: 'n001'}),
             (n010:C {name: 'n010'}),
             (n011:C {name: 'n011'}),
             (n0000:D {name: 'n0000'}),
             (n0001:D {name: 'n0001'}),
             (n0010:D {name: 'n0010'}),
             (n0011:D {name: 'n0011'}),
             (n0100:D {name: 'n0100'}),
             (n0101:D {name: 'n0101'}),
             (n0110:D {name: 'n0110'}),
             (n0111:D {name: 'n0111'})
      CREATE (n0)-[:LIKES]->(n00),
             (n0)-[:LIKES]->(n01),
             (n00)-[:LIKES]->(n000),
             (n00)-[:LIKES]->(n001),
             (n01)-[:LIKES]->(n010),
             (n01)-[:LIKES]->(n011),
             (n000)-[:LIKES]->(n0000),
             (n000)-[:LIKES]->(n0001),
             (n001)-[:LIKES]->(n0010),
             (n001)-[:LIKES]->(n0011),
             (n010)-[:LIKES]->(n0100),
             (n010)-[:LIKES]->(n0101),
             (n011)-[:LIKES]->(n0110),
             (n011)-[:LIKES]->(n0111)
      """
    When executing query:
      """
      MATCH (a:A)
      MATCH (a)-[:LIKES..]->(c)
      RETURN c.name
      """
    Then a SyntaxError should be raised at compile time: InvalidRelationshipPattern

  Scenario: [2] Fail on negative bound
    Given an empty graph
    And having executed:
      """
      CREATE (n0:A {name: 'n0'}),
             (n00:B {name: 'n00'}),
             (n01:B {name: 'n01'}),
             (n000:C {name: 'n000'}),
             (n001:C {name: 'n001'}),
             (n010:C {name: 'n010'}),
             (n011:C {name: 'n011'}),
             (n0000:D {name: 'n0000'}),
             (n0001:D {name: 'n0001'}),
             (n0010:D {name: 'n0010'}),
             (n0011:D {name: 'n0011'}),
             (n0100:D {name: 'n0100'}),
             (n0101:D {name: 'n0101'}),
             (n0110:D {name: 'n0110'}),
             (n0111:D {name: 'n0111'})
      CREATE (n0)-[:LIKES]->(n00),
             (n0)-[:LIKES]->(n01),
             (n00)-[:LIKES]->(n000),
             (n00)-[:LIKES]->(n001),
             (n01)-[:LIKES]->(n010),
             (n01)-[:LIKES]->(n011),
             (n000)-[:LIKES]->(n0000),
             (n000)-[:LIKES]->(n0001),
             (n001)-[:LIKES]->(n0010),
             (n001)-[:LIKES]->(n0011),
             (n010)-[:LIKES]->(n0100),
             (n010)-[:LIKES]->(n0101),
             (n011)-[:LIKES]->(n0110),
             (n011)-[:LIKES]->(n0111)
      """
    When executing query:
      """
      MATCH (a:A)
      MATCH (a)-[:LIKES*-2]->(c)
      RETURN c.name
      """
    Then a SyntaxError should be raised at compile time: InvalidRelationshipPattern
