#
# Copyright (c) 2015-2018 "Neo Technology,"
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

Feature: ValueHashJoinAcceptance

  Scenario: Find friends of others
    Given an empty graph
    And having executed:
      """
      CREATE (:A {id: 1}),
             (:A {id: 2}),
             (:B {id: 2}),
             (:B {id: 3})
      """
    When executing query:
      """
      MATCH (a:A), (b:B)
      WHERE a.id = b.id
      RETURN a, b
      """
    Then the result should be:
      | a            | b            |
      | (:A {id: 2}) | (:B {id: 2}) |
    And no side effects

  Scenario: Should only join when matching
    Given an empty graph
    And having executed:
      """
      UNWIND range(0, 1000) AS i
      CREATE (:A {id: i})
      MERGE (:B {id: i % 10})
      """
    When executing query:
      """
      MATCH (a:A), (b:B)
      WHERE a.id = b.id
      RETURN a, b
      """
    Then the result should be:
      | a            | b            |
      | (:A {id: 0}) | (:B {id: 0}) |
      | (:A {id: 1}) | (:B {id: 1}) |
      | (:A {id: 2}) | (:B {id: 2}) |
      | (:A {id: 3}) | (:B {id: 3}) |
      | (:A {id: 4}) | (:B {id: 4}) |
      | (:A {id: 5}) | (:B {id: 5}) |
      | (:A {id: 6}) | (:B {id: 6}) |
      | (:A {id: 7}) | (:B {id: 7}) |
      | (:A {id: 8}) | (:B {id: 8}) |
      | (:A {id: 9}) | (:B {id: 9}) |
    And no side effects
