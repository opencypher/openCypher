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

Feature: MergeNodeAcceptance

  Scenario: Merge node when no nodes exist
    Given an empty graph
    When executing query: MERGE (a) RETURN count(*) AS n
    Then the result should be:
      | n |
      | 1 |
    And the side effects should be:
      | +nodes | 1 |

  Scenario: Merge node with label
    Given an empty graph
    When executing query: MERGE (a:Label) RETURN labels(a)
    Then the result should be:
      | labels(a) |
      | ['Label'] |
    And the side effects should be:
      | +nodes  | 1 |
      | +labels | 1 |

  Scenario: Merge node with label add label on create
    Given an empty graph
    When executing query: MERGE (a:Label) ON CREATE SET a:Foo RETURN labels(a)
    Then the result should be:
      | labels(a)        |
      | ['Label', 'Foo'] |
    And the side effects should be:
      | +nodes  | 1 |
      | +labels | 2 |

  Scenario: Merge node with label add property on create
    Given an empty graph
    When executing query: MERGE (a:Label) ON CREATE SET a.prop = 42 RETURN a.prop
    Then the result should be:
      | a.prop |
      | 42     |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 1 |

  Scenario: Merge node with label when it exists
    Given an empty graph
    And having executed: CREATE (:Label)
    When executing query: MERGE (a:Label) RETURN id(a)
    Then the result should be:
      | id(a) |
      | 0     |
    And no side effects

  Scenario: Merge node should create when it doesn't match, properties
    Given an empty graph
    And having executed: CREATE ({prop: 42})
    When executing query: MERGE (a {prop: 43}) RETURN a.prop
    Then the result should be:
      | a.prop |
      | 43     |
    And the side effects should be:
      | +nodes      | 1 |
      | +properties | 1 |

  Scenario: Merge node should create when it doesn't match, properties and label
    Given an empty graph
    And having executed: CREATE (:Label {prop: 42})
    When executing query: MERGE (a:Label {prop: 43}) RETURN a.prop
    Then the result should be:
      | a.prop |
      | 43     |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 1 |

  Scenario: Merge node with prop and label
    Given an empty graph
    And having executed: CREATE (:Label {prop: 42})
    When executing query: MERGE (a:Label {prop: 42}) RETURN a.prop
    Then the result should be:
      | a.prop |
      | 42     |
    And no side effects

  Scenario: Merge node with prop and label and unique index
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (n:Label) ASSERT n.prop IS UNIQUE
    And having executed: CREATE (:Label {prop: 42})
    When executing query: MERGE (a:Label {prop: 42}) RETURN a.prop
    Then the result should be:
      | a.prop |
      | 42     |
    And no side effects

  Scenario: Merge node with prop and label and unique index when no match
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (n:Label) ASSERT n.prop IS UNIQUE
    And having executed: CREATE (:Label {prop: 42})
    When executing query: MERGE (a:Label {prop: 11}) RETURN a.prop
    Then the result should be:
      | a.prop |
      | 11     |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 1 |

  Scenario: Merge node with label add label on match when it exists
    Given an empty graph
    And having executed: CREATE (:Label)
    When executing query: MERGE (a:Label) ON MATCH SET a:Foo RETURN labels(a)
    Then the result should be:
      | labels(a)        |
      | ['Label', 'Foo'] |
    And the side effects should be:
      | +labels | 1 |

  Scenario: Merge node with label add property on update when it exists
    Given an empty graph
    And having executed: CREATE (:Label)
    When executing query: MERGE (a:Label) ON CREATE SET a.prop = 42 RETURN a.prop
    Then the result should be:
      | a.prop |
      | null   |
    And no side effects

  Scenario: Merge node and set property on match
    Given an empty graph
    And having executed: CREATE (:Label)
    When executing query: MERGE (a:Label) ON MATCH SET a.prop = 42 RETURN a.prop
    Then the result should be:
      | a.prop |
      | 42     |
    And the side effects should be:
      | +properties | 1 |

  Scenario: Merge using unique constraint should update existing node
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE (:Person {id: 23, country: 'Sweden'})
    When executing query: MERGE (a:Person {id: 23, country: 'Sweden'}) ON MATCH SET a.name = 'Emil' RETURN a
    Then the result should be:
      | a                                                   |
      | (:Person {id: 23, country: 'Sweden', name: 'Emil'}) |
    And the side effects should be:
      | +properties | 1 |

  Scenario: Merge using unique constraint should create missing node
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    When executing query: MERGE (a:Person {id: 23, country: 'Sweden'}) ON CREATE SET a.name = 'Emil' RETURN a
    Then the result should be:
      | a                                                   |
      | (:Person {id: 23, country: 'Sweden', name: 'Emil'}) |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 3 |

  Scenario: Should match on merge using multiple unique indexes if only found single node for both indexes
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.email IS UNIQUE
    And having executed: CREATE (:Person {id: 23, email: 'smth@neo.com'})
    When executing query: MERGE (a:Person {id: 23, email: 'smth@neo.com'}) ON MATCH SET a.country = 'Sweden' RETURN a
    Then the result should be:
      | a                                                            |
      | (:Person {id: 23, country: 'Sweden', email: 'smth@neo.com'}) |
    And the side effects should be:
      | +properties | 1 |

  Scenario: Should match on merge using multiple unique indexes and labels if only found single node for both indexes
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (u:User) ASSERT u.email IS UNIQUE
    And having executed: CREATE (:Person:User {id: 23, email: 'smth@neo.com'})
    When executing query: MERGE (a:Person:User {id: 23, email: 'smth@neo.com'}) ON MATCH SET a.country = 'Sweden' RETURN a
    Then the result should be:
      | a                                                                 |
      | (:Person:User {id: 23, country: 'Sweden', email: 'smth@neo.com'}) |
    And the side effects should be:
      | +properties | 1 |

  Scenario: Should match on merge using multiple unique indexes using same key if only found single node for both indexes
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (u:User) ASSERT u.email IS UNIQUE
    And having executed: CREATE (:Person:User {id: 23})
    When executing query: MERGE (a:Person:User {id: 23}) ON MATCH SET a.country = 'Sweden' RETURN a
    Then the result should be:
      | a                                          |
      | (:Person:User {id: 23, country: 'Sweden'}) |
    And the side effects should be:
      | +properties | 1 |

  Scenario: Should create on merge using multiple unique indexes if found no nodes
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.email IS UNIQUE
    When executing query: MERGE (a:Person {id: 23, email: 'smth@neo.com'}) ON CREATE SET a.country = 'Sweden' RETURN a
    Then the result should be:
      | a                                                            |
      | (:Person {id: 23, email: 'smth@neo.com', country: 'Sweden'}) |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 3 |

  Scenario: Should create on merge using multiple unique indexes and labels if found no nodes
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (u:User) ASSERT u.email IS UNIQUE
    When executing query: MERGE (a:Person:User {id: 23, email: 'smth@neo.com'}) ON CREATE SET a.country = 'Sweden' RETURN a
    Then the result should be:
      | a                                                                 |
      | (:Person:User {id: 23, email: 'smth@neo.com', country: 'Sweden'}) |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 2 |
      | +properties | 3 |

  Scenario: Should fail on merge using multiple unique indexes using same key if found different nodes
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (u:User) ASSERT u.id IS UNIQUE
    And having executed: CREATE (:Person {id: 23}), (:User {id: 23})
    When executing query: MERGE (a:Person:User {id: 23})
    Then a ConstraintVerificationFailed should be raised at runtime: CreateBlockedByConstraint

  Scenario: Should fail on merge using multiple unique indexes if found different nodes
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.email IS UNIQUE
    And having executed: CREATE (:Person {id: 23}), (:Person {email: 'smth@neo.com'})
    When executing query: MERGE (a:Person {id: 23, email: 'smth@neo.com'})
    Then a ConstraintVerificationFailed should be raised at runtime: CreateBlockedByConstraint

  Scenario: Should fail on merge using multiple unique indexes if it found a node matching single property only
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.email IS UNIQUE
    And having executed: CREATE (:Person {id: 23})
    When executing query: MERGE (a:Person {id: 23, email: 'smth@neo.com'})
    Then a ConstraintVerificationFailed should be raised at runtime: CreateBlockedByConstraint

  Scenario: Should fail on merge using multiple unique indexes if it found a node matching single property only flipped order
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.email IS UNIQUE
    And having executed: CREATE (:Person {email: 'smth@neo.com'})
    When executing query: MERGE (a:Person {id: 23, email: 'smth@neo.com'})
    Then a ConstraintVerificationFailed should be raised at runtime: CreateBlockedByConstraint

  Scenario: Should fail on merge using multiple unique indexes and labels if found different nodes
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE CONSTRAINT ON (u:User) ASSERT u.email IS UNIQUE
    And having executed: CREATE (:Person {id: 23}), (:User {email: 'smth@neo.com'})
    When executing query: MERGE (a:Person:User {id: 23, email: 'smth@neo.com'})
    Then a ConstraintVerificationFailed should be raised at runtime: CreateBlockedByConstraint

  Scenario: Should handle running merge inside a foreach loop
    Given an empty graph
    When executing query: FOREACH(x IN [1,2,3] | MERGE ({property: x}))
    Then the result should be empty
    And the side effects should be:
      | +nodes      | 3 |
      | +properties | 3 |

  Scenario: Unrelated nodes with same property should not clash
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    And having executed: CREATE (a:Item {id: 1}), (b:Person {id: 1})
    When executing query: MERGE (a:Item {id: 1}) MERGE (b:Person {id: 1})
    Then the result should be empty
    And no side effects

  Scenario: Works fine with index
    Given an empty graph
    And having executed: CREATE INDEX ON :Person(name)
    When executing query: MERGE (person:Person {name: 'Lasse'}) RETURN person.name
    Then the result should be:
      | person.name |
      | 'Lasse'     |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 1 |

  Scenario: Works fine with index and constraint
    Given an empty graph
    And having executed: CREATE INDEX ON :Person(name)
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.id IS UNIQUE
    When executing query: MERGE (person:Person {name: 'Lasse', id: 42})
    Then the result should be empty
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 2 |

  Scenario: Works with indexed and unindexed property
    Given an empty graph
    And having executed: CREATE INDEX ON :Person(name)
    When executing query: MERGE (person:Person {name: 'Lasse', id: 42})
    Then the result should be empty
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 2 |

  Scenario: Works with two indexed properties
    Given an empty graph
    And having executed: CREATE INDEX ON :Person(name)
    And having executed: CREATE INDEX ON :Person(id)
    When executing query: MERGE (person:Person {name: 'Lasse', id: 42})
    Then the result should be empty
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 2 |

  Scenario: Works with property repeated in literal map in set
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.ssn IS UNIQUE
    When executing query: MERGE (person:Person {ssn: 42}) ON CREATE SET person = {ssn: 42, name: 'Robert Paulsen'} RETURN person.ssn
    Then the result should be:
      | person.ssn |
      | 42         |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 3 |

  Scenario: Works with property in map that gets set
    Given an empty graph
    And having executed: CREATE CONSTRAINT ON (p:Person) ASSERT p.ssn IS UNIQUE
    And parameters are:
      | p | {ssn: 42, name: 'Robert Paulsen'} |
    When executing query: MERGE (person:Person {ssn: {p}.ssn}) ON CREATE SET person = {p} RETURN person.ssn
    Then the result should be:
      | person.ssn |
      | 42         |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 3 |

  Scenario: Should work when finding multiple elements
    Given an empty graph
    When executing query: CREATE (:X) CREATE (:X) MERGE (:X)
    Then the result should be empty
    And the side effects should be:
      | +nodes  | 2 |
      | +labels | 2 |

  Scenario: Should handle argument properly
    Given an empty graph
    And having executed: CREATE ({x: 42}), ({x: 'not42'})
    When executing query: WITH 42 AS x MERGE (c:N {x: x})
    Then the result should be empty
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 1 |

  Scenario: Should handle arguments properly with only write clauses
    Given an empty graph
    When executing query: CREATE (a {p: 1}) MERGE ({v: a.p})
    Then the result should be empty
    And the side effects should be:
      | +nodes      | 2 |
      | +properties | 2 |

  Scenario: Should be able to merge using property from match
    Given an empty graph
    And having executed:
          """
          CREATE (:Person {name: 'A', bornIn: 'New York'})
          CREATE (:Person {name: 'B', bornIn: 'Ohio'})
          CREATE (:Person {name: 'C', bornIn: 'New Jersey'})
          CREATE (:Person {name: 'D', bornIn: 'New York'})
          CREATE (:Person {name: 'E', bornIn: 'Ohio'})
          CREATE (:Person {name: 'F', bornIn: 'New Jersey'})
          """
    When executing query: MATCH (person:Person) MERGE (city: City {name: person.bornIn})
    Then the result should be empty
    And the side effects should be:
      | +nodes      | 3 |
      | +labels     | 3 |
      | +properties | 3 |

  Scenario: Should be able to merge using property from match with index
    Given an empty graph
    And having executed: CREATE INDEX ON :City(name)
    And having executed:
          """
          CREATE (:Person {name: 'A', bornIn: 'New York'})
          CREATE (:Person {name: 'B', bornIn: 'Ohio'})
          CREATE (:Person {name: 'C', bornIn: 'New Jersey'})
          CREATE (:Person {name: 'D', bornIn: 'New York'})
          CREATE (:Person {name: 'E', bornIn: 'Ohio'})
          CREATE (:Person {name: 'F', bornIn: 'New Jersey'})
          """
    When executing query: MATCH (person:Person) MERGE (city: City {name: person.bornIn})
    Then the result should be empty
    And the side effects should be:
      | +nodes      | 3 |
      | +labels     | 3 |
      | +properties | 3 |

  Scenario: Should be able to use properties from match in ON CREATE
    Given an empty graph
    And having executed: CREATE (:Person {bornIn: 'New York'}), (:Person {bornIn: 'Ohio'})
    When executing query: MATCH (person:Person) MERGE (city: City) ON CREATE SET city.name = person.bornIn RETURN person.bornIn
    Then the result should be:
      | person.bornIn |
      | 'New York'    |
      | 'Ohio'        |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 1 |

  Scenario: Should be able to use properties from match in ON MATCH
    Given an empty graph
    And having executed: CREATE (:Person {bornIn: 'New York'}), (:Person {bornIn: 'Ohio'})
    When executing query: MATCH (person:Person) MERGE (city: City) ON MATCH SET city.name = person.bornIn RETURN person.bornIn
    Then the result should be:
      | person.bornIn |
      | 'New York'    |
      | 'Ohio'        |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 1 |

  Scenario: Should be able to use properties from match in ON MATCH and ON CREATE
    Given an empty graph
    And having executed: CREATE (:Person {bornIn: 'New York'}), (:Person {bornIn: 'Ohio'})
    When executing query:
        """
        MATCH (person:Person)
        MERGE (city: City)
          ON MATCH SET city.name = person.bornIn
          ON CREATE SET city.name = person.bornIn
        RETURN person.bornIn
        """
    Then the result should be:
      | person.bornIn |
      | 'New York'    |
      | 'Ohio'        |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 1 |
      | +properties | 2 |

  Scenario: Should be able to set labels on match
    Given an empty graph
    And having executed: CREATE ()
    When executing query: MERGE (a) ON MATCH SET a:L
    Then the result should be empty
    And the side effects should be:
      | +labels | 1 |

  Scenario: Should be able to set labels on match and on create
    Given an empty graph
    And having executed: CREATE (), ()
    When executing query: MATCH () MERGE (a:L) ON MATCH SET a:M1 ON CREATE SET a:M2
    Then the result should be empty
    And the side effects should be:
      | +nodes  | 1 |
      | +labels | 3 |

  Scenario: Should support updates while merging
    Given an empty graph
    And having executed: UNWIND [0, 1, 2] AS x UNWIND [0, 1, 2] AS y CREATE ({x: x, y: y})
    When executing query:
      """
      MATCH (foo)
      WITH foo.x AS x, foo.y AS y
      MERGE (:N {x: x, y: y + 1})
      MERGE (:N {x: x, y: y})
      MERGE (:N {x: x + 1, y: y})
      RETURN x, y
      """
    Then the result should be:
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
      | +labels     | 15 |
      | +properties | 30 |

  Scenario: Merge inside foreach should see variables introduced by update actions outside foreach
    Given an empty graph
    When executing query: CREATE (a {name: 'Start'}) FOREACH(x IN [1,2,3] | MERGE (a)-[:X]->({id: x})) RETURN a.name
    Then the result should be:
      | a.name  |
      | 'Start' |
    And the side effects should be:
      | +nodes         | 4 |
      | +relationships | 3 |
      | +properties    | 4 |

  Scenario: Merge must properly handle multiple labels
    Given an empty graph
    And having executed: CREATE (:L:A {prop: 42})
    When executing query: MERGE (test:L:B {prop : 42}) RETURN labels(test) AS labels
    Then the result should be:
      | labels     |
      | ['L', 'B'] |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 2 |
      | +properties | 1 |

  Scenario: Merge with an index must properly handle multiple labels
    Given an empty graph
    And having executed: CREATE INDEX ON :L(prop)
    And having executed: CREATE (:L:A {prop: 42})
    When executing query: MERGE (test:L:B {prop : 42}) RETURN labels(test) AS labels
    Then the result should be:
      | labels     |
      | ['L', 'B'] |
    And the side effects should be:
      | +nodes      | 1 |
      | +labels     | 2 |
      | +properties | 1 |

  Scenario: Merge followed by multiple creates
    Given an empty graph
    When executing query:
      """
      MERGE (t:T {id:42})
      CREATE (f:R)
      CREATE (t)-[:REL]->(f)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 1 |
      | +labels        | 2 |
      | +properties    | 1 |

  Scenario: Unwind combined with merge
    Given an empty graph
    When executing query:
      """
      UNWIND [1,2,3,4] AS int
      MERGE (n {id: int})
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 4        |
    And the side effects should be:
      | +nodes         | 4 |
      | +properties    | 4 |

  Scenario: Merges should not be able to match on deleted nodes
    Given an empty graph
    And having executed: CREATE (:A {value: 1}), (:A {value: 2})
    When executing query:
      """
      MATCH (a:A)
      DELETE a
      MERGE (a2:A)
      RETURN a2.value
      """
    Then the result should be:
      | a2.value |
      | null     |
      | null     |
    And the side effects should be:
      | +nodes         | 1 |
      | -nodes         | 2 |
      | +labels        | 1 |
