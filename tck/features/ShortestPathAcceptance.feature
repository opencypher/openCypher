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

Feature: ShortestPathAcceptance

  Background:
    Given an empty graph

  Scenario: Find a shortest path among paths that fulfill a predicate on all nodes
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'}),
             (x:X {name: 'X'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d),
             (a)-[:REL]->(x),
             (x)-[:REL]->(d)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[*]->(dst:D))
      WHERE NONE(n in nodes(p) WHERE n:X)
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
      | 'A'  |
      | 'B'  |
      | 'C'  |
      | 'D'  |
    And no side effects

  Scenario: Find a shortest path among paths that fulfill a predicate on all relationships
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'}),
             (x:X {name: 'X'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d),
             (a)-[:X {blocked: true}]->(x),
             (x)-[:X {blocked: true}]->(d)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[*]->(dst:D))
      WHERE NONE(r in rels(p) WHERE exists(r.blocked))
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
      | 'A'  |
      | 'B'  |
      | 'C'  |
      | 'D'  |
    And no side effects

  Scenario: Find a shortest path among paths that fulfill a predicate on all relationships 2
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'}),
             (x:X {name: 'X'})
      CREATE (a)-[:X {blocked: false}]->(b),
             (b)-[:X {blocked: false}]->(c),
             (c)-[:X {blocked: false}]->(d),
             (a)-[:X {blocked: true}]->(x),
             (x)-[:X {blocked: true}]->(d)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[*]->(dst:D))
      WHERE NONE(r in rels(p) WHERE r.blocked)
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
      | 'A'  |
      | 'B'  |
      | 'C'  |
      | 'D'  |
    And no side effects

  Scenario: Find a shortest path among paths that fulfill a predicate
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'}),
             (x:X {name: 'X'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d),
             (a)-[:REL]->(x),
             (x)-[:REL]->(d)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[rs*]->(dst:D))
      WHERE length(p) % 2 = 1 // Only uneven paths wanted!
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
      | 'A'  |
      | 'B'  |
      | 'C'  |
      | 'D'  |
    And no side effects

  Scenario: Find a shortest path without loosing context information at runtime
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'}),
             (x:X {name: 'X'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d),
             (a)-[:REL]->(x),
             (x)-[:REL]->(d)
      """
    When executing query:
      """
      MATCH (src:A), (dest:D)
      MATCH p = shortestPath((src)-[rs*]->(dest))
      WHERE ALL(r in rs WHERE type(rs[0]) = type(r)) AND ALL(r in rs WHERE NOT EXISTS(r.blocked) OR r.blocked <> true)
      RETURN p AS path
      """
    Then the result should be:
      | path                                                                       |
      | <(:A {name: 'A'})-[:REL {}]->(:X {name: 'X'})-[:REL {}]->(:D {name: 'D'})> |
    And no side effects

  Scenario: Find a shortest path in an expression context
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'}),
             (x:X {name: 'X'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d),
             (a)-[:REL]->(x),
             (x)-[:REL]->(d)
      """
    When executing query:
      """
      MATCH (src:A), (dst:D)
      RETURN shortestPath((src:A)-[*]->(dst:D)) as path
      """
    Then the result should be, in order:
      | path                                                                       |
      | <(:A {name: 'A'})-[:REL {}]->(:X {name: 'X'})-[:REL {}]->(:D {name: 'D'})> |
    And no side effects

  Scenario: Find a shortest path among paths that fulfill a predicate on all relationships
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'}),
             (x:X {name: 'X'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d),
             (a)-[:ALT1]->(x),
             (x)-[:ALT2]->(d)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[rs*]->(dst:D))
      WHERE ALL(r in rs WHERE type(rs[0]) = type(r) )
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
      | 'A'  |
      | 'B'  |
      | 'C'  |
      | 'D'  |
    And no side effects

  Scenario: Finds shortest path
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d),
             (b)-[:REL]->(d)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[*]->(dst:D))
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
      | 'A'  |
      | 'B'  |
      | 'D'  |
    And no side effects

  Scenario: Optionally finds shortest path
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d),
             (b)-[:REL]->(d)
      """
    When executing query:
      """
      OPTIONAL MATCH p = shortestPath((src:A)-[*]->(dst:D))
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
      | 'A'  |
      | 'B'  |
      | 'D'  |
    And no side effects

  Scenario: Optionally finds shortest path using previously bound nodes
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d),
             (b)-[:REL]->(d)
      """
    When executing query:
      """
      MATCH (a:A), (d:D)
      OPTIONAL MATCH p = shortestPath((a)-[*]->(d))
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
      | 'A'  |
      | 'B'  |
      | 'D'  |
    And no side effects


  Scenario: Returns null when not finding a shortest path during an OPTIONAL MATCH
    And having executed:
      """
      CREATE (:A {name: 'A'}), (:B {name: 'B'}),
             (:C {name: 'C'}), (:D {name: 'D'})
      """
    When executing query:
      """
      MATCH (a:A), (d:D)
      OPTIONAL MATCH p = shortestPath((a)-[*]->(d))
      RETURN p AS path
      """
    Then the result should be, in order:
      | path |
      | null |
    And no side effects

  Scenario: Find relationships of a shortest path
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'})
      CREATE (a)-[:REL {id: 1}]->(b),
             (b)-[:REL {id: 2}]->(c),
             (c)-[:REL {id: 3}]->(d),
             (b)-[:REL {id: 4}]->(d)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[r*]->(dst:D))
      UNWIND extract(r IN rels(p) | r.id) AS rel
      RETURN rel
      """
    Then the result should be, in order:
      | rel |
      | 1   |
      | 4   |
    And no side effects

  Scenario: Find no shortest path when a length limit prunes all candidates
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[*..1]->(dst:D))
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
    And no side effects

  Scenario: Find no shortest path when the start node is null
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (c)-[:REL]->(d)
      """
    When executing query:
      """
      OPTIONAL MATCH (src:Y)
      WITH src
      MATCH p = shortestPath((src)-[*]->(dst:D))
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be, in order:
      | node |
    And no side effects

  Scenario: Find all shortest paths
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (d:D {name: 'D'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c),
             (a)-[:REL]->(d),
             (d)-[:REL]->(c)
      """
    When executing query:
      """
      MATCH p = allShortestPaths((src:A)-[*]->(dst:C))
      RETURN extract(n IN nodes(p) | n.name) AS nodes
      """
    Then the result should be:
      | nodes           |
      | ['A', 'B', 'C'] |
      | ['A', 'D', 'C'] |
    And no side effects

  Scenario: Find a shortest path among paths of length 1
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (:D {name: 'D'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[*..1]->(dst))
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be:
      | node |
      | 'A'  |
      | 'B'  |
    And no side effects

  Scenario: Find a shortest path of length 1 if requested to
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (:D {name: 'D'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[*1..1]->(dst))
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be:
      | node |
      | 'A'  |
      | 'B'  |
    And no side effects

  Scenario: Find a shortest path among paths matched using a non-variable length pattern
    And having executed:
      """
      CREATE (a:A {name: 'A'}), (b:B {name: 'B'}),
             (c:C {name: 'C'}), (:D {name: 'D'})
      CREATE (a)-[:REL]->(b),
             (b)-[:REL]->(c)
      """
    When executing query:
      """
      MATCH p = shortestPath((src:A)-[]->(dst))
      UNWIND extract(n IN nodes(p) | n.name) AS node
      RETURN node
      """
    Then the result should be:
      | node |
      | 'A'  |
      | 'B'  |
    And no side effects


  Scenario: Find a combination of a shortest path and a pattern expression
    And having executed:
    """
    CREATE (a:A {name: 'A'}),
           (b1:B {name: 'B'}),
           (b2:B {name: 'B'}),
           (c:C {name: 'C'})
    CREATE (a)-[:REL]->(b1),
           (b1)-[:REL]->(b2),
           (b2)-[:REL]->(c)
    """
    When executing query:
    """
    MATCH path = allShortestPaths((a:A)-[:REL*0..100]-(c:C))
    WITH nodes(path) AS pathNodes
    WITH pathNodes[0] AS p, pathNodes[3] as c
    RETURN length((c)-[:REL]-(:B)-[:REL]-(:B)-[:REL]-(p)) AS length
    """
    Then the result should be:
      | length |
      | 1      |
    And no side effects

