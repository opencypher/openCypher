#
# Copyright 2016 "Neo Technology",
# Network Engine for Objects in Lund AB (http://neotechnology.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

Feature: CreateAcceptance

  Scenario: Create a single node
    Given an empty graph
    When executing query:
      """
      CREATE ()
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes  | 1 |

  Scenario: Create a single node with a single label
    Given an empty graph
    When executing query:
      """
      CREATE (:A)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes  | 1 |
      | +labels | 1 |

  Scenario: Create a single node with multiple labels
    Given an empty graph
    When executing query:
      """
      CREATE (:A:B:C:D)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes  | 1 |
      | +labels | 4 |

  Scenario: Combine MATCH and CREATE
    Given an empty graph
    And having executed:
      """
      CREATE (), ()
      """
    When executing query:
      """
      MATCH ()
      CREATE ()
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes  | 2 |

  Scenario: Combine MATCH, WITH and CREATE
    Given an empty graph
    And having executed:
      """
      CREATE (), ()
      """
    When executing query:
      """
      MATCH ()
      CREATE ()
      WITH *
      MATCH ()
      CREATE ()
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes  | 10 |

  Scenario: Newly-created nodes not visible to preceding MATCH
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH ()
      CREATE ()
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes  | 1 |

  Scenario: Create a single node with properties
    Given an empty graph
    When executing query:
      """
      CREATE (n {prop: 'foo'})
      RETURN n.prop AS p
      """
    Then the result should be:
      | p     |
      | 'foo' |
    And the side effects should be:
      | +nodes      | 1 |
      | +properties | 1 |

  Scenario: Creating a node with null properties should not return those properties
    Given an empty graph
    When executing query:
      """
      CREATE (n {id: 12, property: NULL})
      RETURN n.id AS id
      """
    Then the result should be:
      | id |
      | 12 |
    And the side effects should be:
      | +nodes      | 1 |
      | +properties | 1 |

  Scenario: Creating a relationship with null properties should not return those properties
    Given an empty graph
    When executing query:
      """
      CREATE ()-[r:X {id: 12, property: NULL}]->()
      RETURN r.id
      """
    Then the result should be:
      | r.id |
      | 12   |
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |
      | +properties    | 1 |

  Scenario: Create a simple pattern
    Given an empty graph
    When executing query:
      """
      CREATE ()-[:R]->()
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |

  Scenario: Create a self loop
    Given an empty graph
    When executing query:
      """
      CREATE (root:R)-[:LINK]->(root)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 1 |
      | +relationships | 1 |
      | +labels        | 1 |

  Scenario: Create a self loop using MATCH
    Given an empty graph
    And having executed:
      """
      CREATE (:R)
      """
    When executing query:
      """
      MATCH (root:R)
      CREATE (root)-[:LINK]->(root)
      """
    Then the result should be empty
    And the side effects should be:
      | +relationships | 1 |

  Scenario: Create nodes and relationships
    Given an empty graph
    When executing query:
      """
      CREATE (a), (b),
             (a)-[:R]->(b)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |

  Scenario: Create a relationship with a property
    Given an empty graph
    When executing query:
      """
      CREATE ()-[:R {prop: 42}]->()
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |
      | +properties    | 1 |

  Scenario: Create a relationship with the correct direction - 1
    Given an empty graph
    And having executed:
      """
      CREATE (:X)
      CREATE (:Y)
      """
    When executing query:
      """
      MATCH (x:X), (y:Y)
      CREATE (x)<-[:TYPE]-(y)
      """
    Then the result should be empty
    And the side effects should be:
      | +relationships | 1 |

  Scenario: Create a relationship with the correct direction - 2
    Given an empty graph
    And having executed:
      """
      CREATE (:X)
      CREATE (:Y)
      """
    When executing query:
      """
      MATCH (x:X), (y:Y)
      CREATE (x)<-[:TYPE]-(y)
      """
    And having executed:
      """
      MATCH (x:X)<-[:TYPE]-(y:Y)
      RETURN x, y
      """
    Then the result should be:
      | x    |  y   |
      | (:X) | (:Y) |
    And no side effects

  Scenario: Create a relationship and an end node from a matched starting node - 1
    Given an empty graph
    And having executed:
      """
      CREATE (:Begin)
      """
    When executing query:
      """
      MATCH (x:Begin)
      CREATE (x)-[:TYPE]->(:End)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 1 |
      | +relationships | 1 |
      | +labels        | 1 |

  Scenario: Create a relationship and an end node from a matched starting node - 2
    Given an empty graph
    And having executed:
      """
      CREATE (:Begin)
      """
    When executing query:
      """
      MATCH (x:Begin)
      CREATE (x)-[:TYPE]->(:End)
      """
    And having executed:
      """
      MATCH (x:Begin)-[:TYPE]->()
      RETURN x
      """
    Then the result should be:
      | x        |
      | (:Begin) |
    And no side effects

  Scenario: Create a single node after a WITH
    Given an empty graph
    And having executed:
      """
      CREATE (), ()
      """
    When executing query:
      """
      MATCH ()
      CREATE ()
      WITH *
      CREATE ()
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes | 4 |

  Scenario: Create a relationship with a reversed direction - 1
    Given an empty graph
    When executing query:
      """
      CREATE (:A)<-[:R]-(:B)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |
      | +labels        | 2 |

  Scenario: Create a relationship with a reversed direction - 2
    Given an empty graph
    When executing query:
      """
      CREATE (:A)<-[:R]-(:B)
      """
    And having executed:
      """
      MATCH (a:A)<-[:R]-(b:B)
      RETURN a, b
      """
    Then the result should be:
      | a    | b    |
      | (:A) | (:B) |
    And no side effects

  Scenario: Create a pattern with multiple hops - 1
    Given an empty graph
    When executing query:
      """
      CREATE (:A)-[:R]->(:B)-[:R]->(:C)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 3 |
      | +relationships | 2 |
      | +labels        | 3 |

  Scenario: Create a pattern with multiple hops - 2
    Given an empty graph
    When executing query:
      """
      CREATE (:A)-[:R]->(:B)-[:R]->(:C)
      """
    And having executed:
      """
      MATCH (a:A)-[:R]->(b:B)-[:R]->(c:C)
      RETURN a, b, c
      """
    Then the result should be:
      | a    | b    | c    |
      | (:A) | (:B) | (:C) |
    And no side effects

  Scenario: Create a pattern with multiple hops in the reverse direction - 1
    Given an empty graph
    When executing query:
      """
      CREATE (:A)<-[:R]-(:B)<-[:R]-(:C)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 3 |
      | +relationships | 2 |
      | +labels        | 3 |

  Scenario: Create a pattern with multiple hops in the reverse direction - 2
    Given an empty graph
    When executing query:
      """
      CREATE (:A)<-[:R]-(:B)<-[:R]-(:C)
      """
    And having executed:
      """
      MATCH (a)<-[:R]-(b)<-[:R]-(c)
      RETURN a, b, c
      """
    Then the result should be:
      | a    | b    | c    |
      | (:A) | (:B) | (:C) |
    And no side effects

  Scenario: Create a pattern with multiple hops in varying directions - 1
    Given an empty graph
    When executing query:
      """
      CREATE (:A)-[:R]->(:B)<-[:R]-(:C)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 3 |
      | +relationships | 2 |
      | +labels        | 3 |

  Scenario: Create a pattern with multiple hops in varying directions - 2
    Given an empty graph
    When executing query:
      """
      CREATE (:A)-[:R]->(:B)<-[:R]-(:C)
      """
    And having executed:
      """
      MATCH (a:A)-[r1:R]->(b:B)<-[r2:R]-(c:C)
      RETURN a, b, c
      """
    Then the result should be:
      | a    | b    | c    |
      | (:A) | (:B) | (:C) |
    And no side effects

  Scenario: Create a pattern with multiple hops with multiple types and varying directions - 1
    Given an empty graph
    When executing query:
      """
      CREATE ()-[:R1]->()<-[:R2]-()-[:R3]->()
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 4 |
      | +relationships | 3 |

  Scenario: Create a pattern with multiple hops with multiple types and varying directions - 2
    Given an empty graph
    When executing query:
      """
      CREATE ()-[:R1]->()<-[:R2]-()-[:R3]->()
      """
    And having executed:
      """
      MATCH ()-[r1:R1]->()<-[r2:R2]-()-[r3:R3]->()
      RETURN r1, r2, r3
      """
    Then the result should be:
      | r1    | r2    | r3    |
      | [:R1] | [:R2] | [:R3] |
    And no side effects

  Scenario: Nodes are not created when aliases are applied to variable names
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH (n)
      MATCH (m)
      WITH n AS a, m AS b
      CREATE (a)-[:T]->(b)
      RETURN id(a) AS a, id(b) AS b
      """
    Then the result should be:
      | a | b |
      | 0 | 0 |
    And the side effects should be:
      | +relationships | 1 |

  Scenario: Only a single node is created when an alias is applied to a variable name
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH (n)
      WITH n AS a
      CREATE (a)-[:T]->()
      RETURN id(a) AS a
      """
    Then the result should be:
      | a |
      | 0 |
    And the side effects should be:
      | +nodes         | 1 |
      | +relationships | 1 |

  Scenario: Nodes are not created when aliases are applied to variable names multiple times
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH (n)
      MATCH (m)
      WITH n AS a, m AS b
      CREATE (a)-[:T]->(b)
      WITH a AS x, b AS y
      CREATE (x)-[:T]->(y)
      RETURN id(x) AS x, id(y) AS y
      """
    Then the result should be:
      | x | y |
      | 0 | 0 |
    And the side effects should be:
      | +relationships | 2 |

  Scenario: Only a single node is created when an alias is applied to a variable name multiple times
    Given an empty graph
    And having executed:
      """
      CREATE ()
      """
    When executing query:
      """
      MATCH (n)
      WITH n AS a
      CREATE (a)-[:T]->()
      WITH a AS x
      CREATE (x)-[:T]->()
      RETURN id(x) AS x
      """
    Then the result should be:
      | x |
      | 0 |
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 2 |

  Scenario: A bound node should be recognized after projection with WITH + WITH
    Given an empty graph
    When executing query:
      """
      CREATE (a)
      WITH a
      WITH *
      CREATE (b)
      CREATE (a)<-[:T]-(b)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |

  Scenario: A bound node should be recognized after projection with WITH + UNWIND
    Given an empty graph
    When executing query:
      """
      CREATE (a)
      WITH a
      UNWIND [0] AS i
      CREATE (b)
      CREATE (a)<-[:T]-(b)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |

  Scenario: A bound node should be recognized after projection with WITH + MERGE node
    Given an empty graph
    When executing query:
      """
      CREATE (a)
      WITH a
      MERGE ()
      CREATE (b)
      CREATE (a)<-[:T]-(b)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |

  Scenario: A bound node should be recognized after projection with WITH + MERGE pattern
    Given an empty graph
    When executing query:
      """
      CREATE (a)
      WITH a
      MERGE ()-[:T]->()
      CREATE (b)
      CREATE (a)<-[:T]-(b)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 4 |
      | +relationships | 2 |


  Scenario: Fail when trying to create using an undirected relationship pattern
    Given an empty graph
    When executing query:
      """
      CREATE ({id: 2})-[r:KNOWS]-({id: 1})
      RETURN r
      """
    Then a SyntaxError should be raised at compile time: RequiresDirectedRelationship

