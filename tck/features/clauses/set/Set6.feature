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

Feature: Set6 - Persistence of set clause side effects

  Scenario: [1] Limiting to zero results after setting a property on a node affects the result set but not the side effects
    Given an empty graph
    And having executed:
      """
      CREATE (:N {num: 42})
      """
    When executing query:
      """
      MATCH (n:N)
      SET n.num = 43
      RETURN n
      LIMIT 0
      """
    Then the result should be, in any order:
      | n |
    And the side effects should be:
      | +properties | 1 |
      | -properties | 1 |

  Scenario: [2] Skipping all results after setting a property on a node affects the result set but not the side effects
    Given an empty graph
    And having executed:
      """
      CREATE (:N {num: 42})
      """
    When executing query:
      """
      MATCH (n:N)
      SET n.num = 43
      RETURN n
      SKIP 1
      """
    Then the result should be, in any order:
      | n |
    And the side effects should be:
      | +properties | 1 |
      | -properties | 1 |

  Scenario: [3] Skipping and limiting to a few results after setting a property on a node affects the result set but not the side effects
    Given an empty graph
    And having executed:
      """
      CREATE (:N {num: 1})
      CREATE (:N {num: 2})
      CREATE (:N {num: 3})
      CREATE (:N {num: 4})
      CREATE (:N {num: 5})
      """
    When executing query:
      """
      MATCH (n:N)
      SET n.num = 42
      RETURN n.num AS num
      SKIP 2 LIMIT 2
      """
    Then the result should be, in any order:
      | num |
      | 42  |
      | 42  |
    And the side effects should be:
      | +properties | 5 |
      | -properties | 5 |

  Scenario: [4] Skipping zero results and limiting to all results after setting a property on a node does not affect the result set nor the side effects
    Given an empty graph
    And having executed:
      """
      CREATE (:N {num: 1})
      CREATE (:N {num: 2})
      CREATE (:N {num: 3})
      CREATE (:N {num: 4})
      CREATE (:N {num: 5})
      """
    When executing query:
      """
      MATCH (n:N)
      SET n.num = 42
      RETURN n.num AS num
      SKIP 0 LIMIT 5
      """
    Then the result should be, in any order:
      | num |
      | 42  |
      | 42  |
      | 42  |
      | 42  |
      | 42  |
    And the side effects should be:
      | +properties | 5 |
      | -properties | 5 |

  Scenario: [5] Limiting to zero results after adding a label on a node affects the result set but not the side effects
    Given an empty graph
    And having executed:
      """
      CREATE (:N {num: 42})
      """
    When executing query:
      """
      MATCH (n:N)
      SET n:Foo
      RETURN n
      LIMIT 0
      """
    Then the result should be, in any order:
      | n |
    And the side effects should be:
      | +labels | 1 |

  Scenario: [6] Skipping all results after adding a label on a node affects the result set but not the side effects
    Given an empty graph
    And having executed:
      """
      CREATE (:N {num: 42})
      """
    When executing query:
      """
      MATCH (n:N)
      SET n:Foo
      RETURN n
      SKIP 1
      """
    Then the result should be, in any order:
      | n |
    And the side effects should be:
      | +labels | 1 |

  Scenario: [7] Skipping and limiting to a few results after adding a label on a node affects the result set but not the side effects
    Given an empty graph
    And having executed:
      """
      CREATE (:N {num: 42})
      CREATE (:N {num: 42})
      CREATE (:N {num: 42})
      CREATE (:N {num: 42})
      CREATE (:N {num: 42})
      """
    When executing query:
      """
      MATCH (n:N)
      SET n:Foo
      RETURN n.num AS num
      SKIP 2 LIMIT 2
      """
    Then the result should be, in any order:
      | num |
      | 42  |
      | 42  |
    And the side effects should be:
      | +labels | 1 |

  Scenario: [8] Skipping zero result and limiting to all results after adding a label on a node does not affect the result set nor the side effects
    Given an empty graph
    And having executed:
      """
      CREATE (:N {num: 42})
      CREATE (:N {num: 42})
      CREATE (:N {num: 42})
      CREATE (:N {num: 42})
      CREATE (:N {num: 42})
      """
    When executing query:
      """
      MATCH (n:N)
      SET n:Foo
      RETURN n.num AS num
      SKIP 0 LIMIT 5
      """
    Then the result should be, in any order:
      | num |
      | 42  |
      | 42  |
      | 42  |
      | 42  |
      | 42  |
    And the side effects should be:
      | +labels | 1 |

  Scenario: [9] Limiting to zero results after setting a property on a relationship affects the result set but not the side effects
    Given an empty graph
    And having executed:
      """
      CREATE ()-[r:R {num: 42}]->()
      """
    When executing query:
      """
      MATCH ()-[r:R]->()
      SET r.num = 43
      RETURN r
      LIMIT 0
      """
    Then the result should be, in any order:
      | r |
    And the side effects should be:
      | +properties | 1 |
      | -properties | 1 |

  Scenario: [10] Skipping all results after setting a property on a relationship affects the result set but not the side effects
    Given an empty graph
    And having executed:
      """
      CREATE ()-[r:R {num: 42}]->()
      """
    When executing query:
      """
      MATCH ()-[r:R]->()
      SET r.num = 43
      RETURN r
      SKIP 1
      """
    Then the result should be, in any order:
      | r |
    And the side effects should be:
      | +properties | 1 |
      | -properties | 1 |

  Scenario: [11] Skipping and limiting to a few results after setting a property on a relationship affects the result set but not the side effects
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:R {num: 1}]->()
      CREATE ()-[:R {num: 2}]->()
      CREATE ()-[:R {num: 3}]->()
      CREATE ()-[:R {num: 4}]->()
      CREATE ()-[:R {num: 5}]->()
      """
    When executing query:
      """
      MATCH ()-[r:R]->()
      SET r.num = 42
      RETURN r.num AS num
      SKIP 2 LIMIT 2
      """
    Then the result should be, in any order:
      | num |
      | 42  |
      | 42  |
    And the side effects should be:
      | +properties | 5 |
      | -properties | 5 |

  Scenario: [12] Skipping zero result and limiting to all results after setting a property on a relationship does not affect the result set nor the side effects
    Given an empty graph
    And having executed:
      """
      CREATE ()-[:R {num: 1}]->()
      CREATE ()-[:R {num: 2}]->()
      CREATE ()-[:R {num: 3}]->()
      CREATE ()-[:R {num: 4}]->()
      CREATE ()-[:R {num: 5}]->()
      """
    When executing query:
      """
      MATCH ()-[r:R]->()
      SET r.num = 42
      RETURN r.num AS num
      SKIP 0 LIMIT 5
      """
    Then the result should be, in any order:
      | num |
      | 42  |
      | 42  |
      | 42  |
      | 42  |
      | 42  |
    And the side effects should be:
      | +properties | 5 |
      | -properties | 5 |
