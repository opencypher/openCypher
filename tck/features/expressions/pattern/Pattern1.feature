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

Feature: Pattern1 - Existential Pattern Match

  Scenario Outline: [1] Matching on any single outgoing directed connection
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)-->() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:A) |
      | (:B) |
    And no side effects

  Scenario Outline: [2] Matching on a single undirected connection
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)--() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:A) |
      | (:B) |
      | (:C) |
      | (:D) |
    And no side effects

  Scenario Outline: [3] Matching on any single incoming directed connection
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)<--() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:A) |
      | (:B) |
      | (:C) |
      | (:D) |
    And no side effects

  Scenario Outline: [4] Matching on a specific type of single outgoing directed connection
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)-[:REL1]->() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:A) |
    And no side effects

  Scenario Outline: [5] Matching on a specific type of single undirected connection
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)-[:REL1]-() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:A) |
      | (:B) |
      | (:D) |
    And no side effects

  Scenario Outline: [6] Matching on a specific type of single incoming directed connection
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)<-[:REL1]-() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:B) |
      | (:D) |
    And no side effects

  Scenario Outline: [7] Matching on a specific type of a variable length outgoing directed connection
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)-[:REL1*]->() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:A) |
    And no side effects

  Scenario Outline: [8] Matching on a specific type of variable length undirected connection
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)-[:REL1*]-() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:A) |
      | (:B) |
      | (:D) |
    And no side effects

  Scenario Outline: [9] Matching on a specific type of variable length incoming directed connection
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)<-[:REL1*]-() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:B) |
      | (:D) |
    And no side effects

  Scenario Outline: [10] Matching on a specific type of undirected connection with length 2
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:REL1]->(b:B), (b)-[:REL2]->(a), (a)-[:REL3]->(:C), (a)-[:REL1]->(:D)
      """
    When executing query:
      """
      MATCH (n) WHERE (n)-[:REL1*2]-() RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | (:B) |
      | (:D) |
    And no side effects