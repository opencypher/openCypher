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

Feature: Match7-3 - Optional match WITH clause scenarios

  Scenario: WITH after OPTIONAL MATCH
    Given an empty graph
    And having executed:
      """
      CREATE (s:Single), (a:A {num: 42}),
             (b:B {num: 46}), (c:C)
      CREATE (s)-[:REL]->(a),
             (s)-[:REL]->(b),
             (a)-[:REL]->(c),
             (b)-[:LOOP]->(b)
      """
    When executing query:
      """
      OPTIONAL MATCH (a:A)
      WITH a AS a
      MATCH (b:B)
      RETURN a, b
      """
    Then the result should be, in any order:
      | a              | b              |
      | (:A {num: 42}) | (:B {num: 46}) |
    And no side effects

  Scenario: Matching and optionally matching with bound nodes in reverse direction
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T]->(:B)
      """
    When executing query:
      """
      MATCH (a1)-[r]->()
      WITH r, a1
        LIMIT 1
      OPTIONAL MATCH (a1)<-[r]-(b2)
      RETURN a1, r, b2
      """
    Then the result should be, in any order:
      | a1   | r    | b2   |
      | (:A) | [:T] | null |
    And no side effects

  Scenario: Matching with LIMIT and optionally matching using a relationship that is already bound
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T]->(:B)
      """
    When executing query:
      """
      MATCH ()-[r]->()
      WITH r
        LIMIT 1
      OPTIONAL MATCH (a2)-[r]->(b2)
      RETURN a2, r, b2
      """
    Then the result should be, in any order:
      | a2   | r    | b2   |
      | (:A) | [:T] | (:B) |
    And no side effects

  Scenario: Matching with LIMIT and optionally matching using a relationship and node that are both already bound
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T]->(:B)
      """
    When executing query:
      """
      MATCH (a1)-[r]->()
      WITH r, a1
        LIMIT 1
      OPTIONAL MATCH (a1)-[r]->(b2)
      RETURN a1, r, b2
      """
    Then the result should be, in any order:
      | a1   | r    | b2   |
      | (:A) | [:T] | (:B) |
    And no side effects
