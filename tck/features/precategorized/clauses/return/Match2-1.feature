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

Feature: Match2-1 - Match relationships RETURN clause scenarios

  Scenario: Returning a relationship property value
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:T {num: 1}]->()
      """
    When executing query:
      """
      MATCH ()-[r]->()
      RETURN r.num
      """
    Then the result should be, in any order:
      | r.num |
      | 1     |
    And no side effects

  Scenario: Missing relationship property should become null
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:T {name: 1}]->()
      """
    When executing query:
      """
      MATCH ()-[r]->()
      RETURN r.name2
      """
    Then the result should be, in any order:
      | r.name2 |
      | null    |
    And no side effects

  Scenario: Projecting a list of nodes and relationships
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (b:B)
      CREATE (a)-[:T]->(b)
      """
    When executing query:
      """
      MATCH (n)-[r]->(m)
      RETURN [n, r, m] AS r
      """
    Then the result should be, in any order:
      | r                  |
      | [(:A), [:T], (:B)] |
    And no side effects

  Scenario: Projecting a map of nodes and relationships
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (b:B)
      CREATE (a)-[:T]->(b)
      """
    When executing query:
      """
      MATCH (n)-[r]->(m)
      RETURN {node1: n, rel: r, node2: m} AS m
      """
    Then the result should be, in any order:
      | m                                     |
      | {node1: (:A), rel: [:T], node2: (:B)} |
    And no side effects
