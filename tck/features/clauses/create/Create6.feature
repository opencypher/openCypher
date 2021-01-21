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

Feature: Create6 - Create clause interoperation with other clauses

  Scenario: [1] Merge followed by multiple creates
    Given an empty graph
    When executing query:
      """
      MERGE (t:T {id: 42})
      CREATE (f:R)
      CREATE (t)-[:REL]->(f)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |
      | +labels        | 2 |
      | +properties    | 1 |

  Scenario: [2] Limiting to zero results after creating nodes affects the result set but not the side effects
    Given an empty graph
    When executing query:
      """
      CREATE (n:N {num: 42})
      RETURN n
      LIMIT 0
      """
    Then the result should be, in any order:
      | n |
    And the side effects should be:
      | +nodes         | 1 |
      | +labels        | 1 |
      | +properties    | 1 |

  Scenario: [3] Skipping all results after creating nodes affects the result set but not the side effects
    Given an empty graph
    When executing query:
      """
      CREATE (n:N {num: 42})
      RETURN n
      SKIP 1
      """
    Then the result should be, in any order:
      | n |
    And the side effects should be:
      | +nodes         | 1 |
      | +labels        | 1 |
      | +properties    | 1 |

  Scenario: [4] Skipping and limiting to a few results after creating nodes does not affect the result set nor the side effects
    Given an empty graph
    When executing query:
      """
      UNWIND [42,42,42,42,42] AS x
      CREATE (n:N {num: x})
      RETURN n.num AS num
      SKIP 2 LIMIT 2
      """
    Then the result should be, in any order:
      | num |
      | 42  |
      | 42  |
    And the side effects should be:
      | +nodes         | 5 |
      | +labels        | 1 |
      | +properties    | 5 |

  Scenario: [5] Skipping zero result and limiting to all results after creating nodes does not affect the result set nor the side effects
    Given an empty graph
    When executing query:
      """
      UNWIND [42,42,42,42,42] AS x
      CREATE (n:N {num: x})
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
      | +nodes         | 5 |
      | +labels        | 1 |
      | +properties    | 5 |

  Scenario: [6] Limiting to zero results after creating relationships affects the result set but not the side effects
    Given an empty graph
    When executing query:
      """
      CREATE ()-[r:R {num: 42}]->()
      RETURN r
      LIMIT 0
      """
    Then the result should be, in any order:
      | r |
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |
      | +properties    | 1 |

  Scenario: [7] Skipping all results after creating relationships affects the result set but not the side effects
    Given an empty graph
    When executing query:
      """
      CREATE ()-[r:R {num: 42}]->()
      RETURN r
      SKIP 1
      """
    Then the result should be, in any order:
      | r |
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |
      | +properties    | 1 |

  Scenario: [8] Skipping and limiting to a few results after creating relationships does not affect the result set nor the side effects
    Given an empty graph
    When executing query:
      """
      UNWIND [42,42,42,42,42] AS x
      CREATE ()-[r:R {num: x}]->()
      RETURN r.num AS num
      SKIP 2 LIMIT 2
      """
    Then the result should be, in any order:
      | num |
      | 42  |
      | 42  |
    And the side effects should be:
      | +nodes         | 10 |
      | +relationships | 5  |
      | +properties    | 5  |

  Scenario: [9] Skipping zero result and limiting to all results after creating relationships does not affect the result set nor the side effects
    Given an empty graph
    When executing query:
      """
      UNWIND [42,42,42,42,42] AS x
      CREATE ()-[r:R {num: x}]->()
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
      | +nodes         | 10 |
      | +relationships | 5  |
      | +properties    | 5  |
