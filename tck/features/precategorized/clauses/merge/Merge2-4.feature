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

Feature: Merge2-3 - Merge Edge and Delete Interoperability

  Scenario: Do not match on deleted entities
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)
      CREATE (b1:B {num: 0}), (b2:B {num: 1})
      CREATE (c1:C), (c2:C)
      CREATE (a)-[:REL]->(b1),
             (a)-[:REL]->(b2),
             (b1)-[:REL]->(c1),
             (b2)-[:REL]->(c2)
      """
    When executing query:
      """
      MATCH (a:A)-[ab]->(b:B)-[bc]->(c:C)
      DELETE ab, bc, b, c
      MERGE (newB:B {num: 1})
      MERGE (a)-[:REL]->(newB)
      MERGE (newC:C)
      MERGE (newB)-[:REL]->(newC)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | -nodes         | 4 |
      | +relationships | 2 |
      | -relationships | 4 |
      | +properties    | 1 |
      | -properties    | 2 |

  Scenario: Do not match on deleted relationships
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (b:B)
      CREATE (a)-[:T {name: 'rel1'}]->(b),
             (a)-[:T {name: 'rel2'}]->(b)
      """
    When executing query:
      """
      MATCH (a)-[t:T]->(b)
      DELETE t
      MERGE (a)-[t2:T {name: 'rel3'}]->(b)
      RETURN t2.name
      """
    Then the result should be, in any order:
      | t2.name |
      | 'rel3'  |
      | 'rel3'  |
    And the side effects should be:
      | +relationships | 1 |
      | -relationships | 2 |
      | +properties    | 1 |
      | -properties    | 2 |
