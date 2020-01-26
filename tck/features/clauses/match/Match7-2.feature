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

Feature: Match7-2 - Optional match RETURN clause scenarios

  Scenario: Do not fail when predicates on optionally matched and missed nodes are invalid
    Given an empty graph
    And having executed:
      """
      CREATE (a), (b {name: 'Mark'})
      CREATE (a)-[:T]->(b)
      """
    When executing query:
      """
      MATCH (n)-->(x0)
      OPTIONAL MATCH (x0)-->(x1)
      WHERE x1.name = 'bar'
      RETURN x0.name
      """
    Then the result should be, in any order:
      | x0.name |
      | 'Mark'  |
    And no side effects

  Scenario: Matching and optionally matching with unbound nodes and equality predicate in reverse direction
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
      OPTIONAL MATCH (a2)<-[r]-(b2)
      WHERE a1 = a2
      RETURN a1, r, b2, a2
      """
    Then the result should be, in any order:
      | a1   | r    | b2   | a2   |
      | (:A) | [:T] | null | null |
    And no side effects
