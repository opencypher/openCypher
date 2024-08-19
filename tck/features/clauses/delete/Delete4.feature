#
# Copyright (c) 2015-2024 "Neo Technology,"
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

Feature: Delete4 - Delete clause interoperation with other clauses

  Scenario: [1] Undirected expand followed by delete and count
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:R]->()
      """
    When executing query:
      """
      MATCH (a)-[r]-(b)
      DELETE r, a, b
      RETURN count(*) AS c
      """
    Then the result should be, in any order:
      | c |
      | 2 |
    And the side effects should be:
      | -nodes         | 2 |
      | -relationships | 1 |

  Scenario: [2] Undirected variable length expand followed by delete and count
    Given an empty graph
    And having executed:
      """
      CREATE (n1), (n2), (n3)
      CREATE (n1)-[:R]->(n2)
      CREATE (n2)-[:R]->(n3)
      """
    When executing query:
      """
      MATCH (a)-[*]-(b)
      DETACH DELETE a, b
      RETURN count(*) AS c
      """
    Then the result should be, in any order:
      | c |
      | 6 |
    And the side effects should be:
      | -nodes         | 3 |
      | -relationships | 2 |

  Scenario: [3] Create and delete in same query
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH ()
      CREATE (n)
      DELETE n
      """
    Then the result should be empty
    And no side effects

  Scenario: [4] Returning a deleted node
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH (n)
      DELETE n
      RETURN n
      """
    Then the result should be, in any order:
      | n    |
      | null |
    And the side effects should be:
      | -nodes         | 1 |

  Scenario: [5] Returning a property of a deleted node
    Given an empty graph
    And having executed:
      """
      CREATE ({x: 1})
      """
    When executing query:
      """
      MATCH (n)
      DELETE n
      RETURN n.x AS x
      """
    Then the result should be, in any order:
      | x    |
      | null |
    And the side effects should be:
      | -nodes         | 1 |

  Scenario: [6] Returning all properties of a deleted node
    Given an empty graph
    And having executed:
      """
      CREATE ({x: 1})
      """
    When executing query:
      """
      MATCH (n)
      DELETE n
      RETURN properties(n) AS properties
      """
    Then the result should be, in any order:
      | properties |
      | null       |
    And the side effects should be:
      | -nodes         | 1 |

  Scenario: [7] Returning the labels of a deleted node
    Given an empty graph
    And having executed:
      """
      CREATE (:A:B)
      """
    When executing query:
      """
      MATCH (n)
      DELETE n
      RETURN labels(n) AS l
      """
    Then the result should be, in any order:
      | l    |
      | null |
    And the side effects should be:
      | -nodes         | 1 |

  Scenario: [8] Returning data projected from a node prior to deletion
    Given an empty graph
    And having executed:
      """
      CREATE (:A:B {x: 1})
      """
    When executing query:
      """
      MATCH (n)
      WITH n, labels(n) AS labels, properties(n) AS props, n.x AS property
      DELETE n
      RETURN n, labels, props, property
      """
    Then the result should be, in any order:
      | n             | labels     | props  | property |
      | (:A:B {x: 1}) | ['A', 'B'] | {x: 1} | 1        |
    And the side effects should be:
      | -nodes         | 1 |
