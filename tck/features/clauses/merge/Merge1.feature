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

Feature: Merge1 - Merge Node

  Scenario: Merge node when no nodes exist
    Given an empty graph
    When executing query:
      """
      MERGE (a)
      RETURN count(*) AS n
      """
    Then the result should be, in any order:
      | n |
      | 1 |
    And the side effects should be:
      | +nodes | 1 |

  Scenario: Merge node with label
    Given an empty graph
    When executing query:
      """
      MERGE (a:TheLabel)
      RETURN labels(a)
      """
    Then the result should be, in any order:
      | labels(a)    |
      | ['TheLabel'] |
    And the side effects should be:
      | +nodes  | 1 |
      | +labels | 1 |

  Scenario: Merge node with label when it exists
    Given an empty graph
    And having executed:
      """
      CREATE (:TheLabel {id: 1})
      """
    When executing query:
      """
      MERGE (a:TheLabel)
      RETURN a.id
      """
    Then the result should be, in any order:
      | a.id |
      | 1    |
    And no side effects

  Scenario: Merge node should create when it doesn't match, properties
    Given an empty graph
    And having executed:
      """
      CREATE ({num: 42})
      """
    When executing query:
      """
      MERGE (a {num: 43})
      RETURN a.num
      """
    Then the result should be, in any order:
      | a.num |
      | 43    |
    And the side effects should be:
      | +nodes      | 1 |
      | +properties | 1 |

  Scenario: Merge node should create when it doesn't match, properties and label
    Given an empty graph
    And having executed:
      """
      CREATE (:TheLabel {num: 42})
      """
    When executing query:
      """
      MERGE (a:TheLabel {num: 43})
      RETURN a.num
      """
    Then the result should be, in any order:
      | a.num |
      | 43    |
    And the side effects should be:
      | +nodes      | 1 |
      | +properties | 1 |

  Scenario: Merge node with prop and label
    Given an empty graph
    And having executed:
      """
      CREATE (:TheLabel {num: 42})
      """
    When executing query:
      """
      MERGE (a:TheLabel {num: 42})
      RETURN a.num
      """
    Then the result should be, in any order:
      | a.num |
      | 42    |
    And no side effects

  Scenario: Merge should work when finding multiple elements
    Given an empty graph
    When executing query:
      """
      CREATE (:X)
      CREATE (:X)
      MERGE (:X)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes  | 2 |
      | +labels | 1 |

  Scenario: Merge should handle argument properly
    Given an empty graph
    And having executed:
      """
      CREATE ({var: 42}),
        ({var: 'not42'})
      """
    When executing query:
      """
      WITH 42 AS var
      MERGE (c:N {var: var})
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 1 |

  Scenario: Merge should support updates while merging
    Given an empty graph
    And having executed:
      """
      UNWIND [0, 1, 2] AS x
      UNWIND [0, 1, 2] AS y
      CREATE ({x: x, y: y})
      """
    When executing query:
      """
      MATCH (foo)
      WITH foo.x AS x, foo.y AS y
      MERGE (:N {x: x, y: y + 1})
      MERGE (:N {x: x, y: y})
      MERGE (:N {x: x + 1, y: y})
      RETURN x, y
      """
    Then the result should be, in any order:
      | x | y |
      | 0 | 0 |
      | 0 | 1 |
      | 0 | 2 |
      | 1 | 0 |
      | 1 | 1 |
      | 1 | 2 |
      | 2 | 0 |
      | 2 | 1 |
      | 2 | 2 |
    And the side effects should be:
      | +nodes      | 15 |
      | +labels     | 1  |
      | +properties | 30 |

  Scenario: Merge must properly handle multiple labels
    Given an empty graph
    And having executed:
      """
      CREATE (:L:A {num: 42})
      """
    When executing query:
      """
      MERGE (test:L:B {num: 42})
      RETURN labels(test) AS labels
      """
    Then the result should be, in any order:
      | labels     |
      | ['L', 'B'] |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 1 |

  Scenario: Merge should bind a path
    Given an empty graph
    When executing query:
      """
      MERGE p = (a {num: 1})
      RETURN p
      """
    Then the result should be, in any order:
      | p            |
      | <({num: 1})> |
    And the side effects should be:
      | +nodes      | 1 |
      | +properties | 1 |
