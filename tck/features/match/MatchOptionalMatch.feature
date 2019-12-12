#
# Copyright (c) 2015-2019 "Neo Technology,"
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

Feature: MatchOptionalMatch

      Scenario: Simple OPTIONAL MATCH on empty graph
      Given an empty graph
      When executing query:
      """
            OPTIONAL MATCH (n)
            RETURN n
            """
      Then the result should be, in any order:
      | n    |
      | null |
      And no side effects

      Scenario: MATCH and OPTIONAL MATCH on same pattern
      Given an empty graph
      And having executed:
      """
            CREATE (a {name: 'A'}), (b:B {name: 'B'}), (c:C {name: 'C'})
            CREATE (a)-[:T]->(b),
                   (a)-[:T]->(c)
            """
      When executing query:
      """
            MATCH (a)-->(b)
            WHERE b:B
            OPTIONAL MATCH (a)-->(c)
            WHERE c:C
            RETURN a.name
            """
      Then the result should be, in any order:
      | a.name |
      | 'A'    |
      And no side effects

      Scenario: OPTIONAL MATCH with previously bound nodes
      Given an empty graph
      And having executed:
      """
            CREATE ()
            """
      When executing query:
      """
            MATCH (n)
            OPTIONAL MATCH (n)-[:NOT_EXIST]->(x)
            RETURN n, x
            """
      Then the result should be, in any order:
      | n  | x    |
      | () | null |
      And no side effects

      Scenario: MATCH with OPTIONAL MATCH in longer pattern
      Given an empty graph
      And having executed:
      """
            CREATE (a {name: 'A'}), (b {name: 'B'}), (c {name: 'C'})
            CREATE (a)-[:KNOWS]->(b),
                   (b)-[:KNOWS]->(c)
            """
      When executing query:
      """
            MATCH (a {name: 'A'})
            OPTIONAL MATCH (a)-[:KNOWS]->()-[:KNOWS]->(foo)
            RETURN foo
            """
      Then the result should be, in any order:
      | foo           |
      | ({name: 'C'}) |
      And no side effects

      Scenario: Variable length relationship in OPTIONAL MATCH
      Given an empty graph
      And having executed:
      """
            CREATE (:A), (:B)
            """
      When executing query:
      """
            MATCH (a:A), (b:B)
            OPTIONAL MATCH (a)-[r*]-(b)
            WHERE r IS NULL
              AND a <> b
            RETURN b
            """
      Then the result should be, in any order:
      | b    |
      | (:B) |
      And no side effects

      Scenario: Optionally matching named paths
      Given an empty graph
      And having executed:
      """
            CREATE (a {name: 'A'}), (b {name: 'B'}), (c {name: 'C'})
            CREATE (a)-[:X]->(b)
            """
      When executing query:
      """
            MATCH (a {name: 'A'}), (x)
            WHERE x.name IN ['B', 'C']
            OPTIONAL MATCH p = (a)-->(x)
            RETURN x, p
            """
      Then the result should be, in any order:
      | x             | p                                   |
      | ({name: 'B'}) | <({name: 'A'})-[:X]->({name: 'B'})> |
      | ({name: 'C'}) | null                                |
      And no side effects

      Scenario: Optionally matching named paths with single and variable length patterns
      Given an empty graph
      And having executed:
      """
            CREATE (a {name: 'A'}), (b {name: 'B'})
            CREATE (a)-[:X]->(b)
            """
      When executing query:
      """
            MATCH (a {name: 'A'})
            OPTIONAL MATCH p = (a)-->(b)-[*]->(c)
            RETURN p
            """
      Then the result should be, in any order:
      | p    |
      | null |
      And no side effects

      Scenario: Optionally matching named paths with variable length patterns
      Given an empty graph
      And having executed:
      """
            CREATE (a {name: 'A'}), (b {name: 'B'}), (c {name: 'C'})
            CREATE (a)-[:X]->(b)
            """
      When executing query:
      """
            MATCH (a {name: 'A'}), (x)
            WHERE x.name IN ['B', 'C']
            OPTIONAL MATCH p = (a)-[r*]->(x)
            RETURN r, x, p
            """
      Then the result should be, in any order:
      | r      | x             | p                                   |
      | [[:X]] | ({name: 'B'}) | <({name: 'A'})-[:X]->({name: 'B'})> |
      | null   | ({name: 'C'}) | null                                |
      And no side effects

      Scenario: Do not fail when predicates on optionally matched and missed nodes are invalid
      Given an empty graph
      And having executed:
      """
            CREATE (a), (b {name: 'Mark'})
            CREATE (a)-[:T]->(b)
            """
      When executing query:
      """
            MATCH (n)-->(x0)
            OPTIONAL MATCH (x0)-->(x1)
            WHERE x1.name = 'bar'
            RETURN x0.name
            """
      Then the result should be, in any order:
      | x0.name |
      | 'Mark'  |
      And no side effects


      Scenario: Matching from null nodes should return no results owing to finding no matches
      Given an empty graph
      When executing query:
      """
            OPTIONAL MATCH (a)
            WITH a
            MATCH (a)-->(b)
            RETURN b
            """
      Then the result should be, in any order:
      | b |
      And no side effects

      Scenario: Matching from null nodes should return no results owing to matches being filtered out
      Given an empty graph
      And having executed:
      """
            CREATE ()-[:T]->()
            """
      When executing query:
      """
            OPTIONAL MATCH (a:TheLabel)
            WITH a
            MATCH (a)-->(b)
            RETURN b
            """
      Then the result should be, in any order:
      | b |
      And no side effects

      Scenario: Optionally matching from null nodes should return null
      Given an empty graph
      When executing query:
      """
            OPTIONAL MATCH (a)
            WITH a
            OPTIONAL MATCH (a)-->(b)
            RETURN b
            """
      Then the result should be, in any order:
      | b    |
      | null |
      And no side effects

      Scenario: OPTIONAL MATCH returns null
      Given an empty graph
      When executing query:
      """
            OPTIONAL MATCH (a)
            RETURN a
            """
      Then the result should be, in any order:
      | a    |
      | null |
      And no side effects

      Scenario: Variable length patterns and nulls
      Given an empty graph
      And having executed:
      """
            CREATE (a:A), (b:B)
            """
      When executing query:
      """
            MATCH (a:A)
            OPTIONAL MATCH (a)-[:FOO]->(b:B)
            OPTIONAL MATCH (b)<-[:BAR*]-(c:B)
            RETURN a, b, c
            """
      Then the result should be, in any order:
      | a    | b    | c    |
      | (:A) | null | null |
      And no side effects


      Scenario: Matching and optionally matching with unbound nodes and equality predicate in reverse direction
      Given an empty graph
      And having executed:
      """
            CREATE (:A)-[:T]->(:B)
            """
      When executing query:
      """
            MATCH (a1)-[r]->()
            WITH r, a1
              LIMIT 1
            OPTIONAL MATCH (a2)<-[r]-(b2)
            WHERE a1 = a2
            RETURN a1, r, b2, a2
            """
      Then the result should be, in any order:
      | a1   | r    | b2   | a2   |
      | (:A) | [:T] | null | null |
      And no side effects

      Scenario: Matching and optionally matching with bound nodes in reverse direction
      Given an empty graph
      And having executed:
      """
            CREATE (:A)-[:T]->(:B)
            """
      When executing query:
      """
            MATCH (a1)-[r]->()
            WITH r, a1
              LIMIT 1
            OPTIONAL MATCH (a1)<-[r]-(b2)
            RETURN a1, r, b2
            """
      Then the result should be, in any order:
      | a1   | r    | b2   |
      | (:A) | [:T] | null |
      And no side effects

      Scenario: Excluding connected nodes
      Given an empty graph
      And having executed:
      """
            CREATE (a:A), (b:B {id: 1}), (:B {id: 2})
            CREATE (a)-[:T]->(b)
            """
      When executing query:
      """
            MATCH (a:A), (other:B)
            OPTIONAL MATCH (a)-[r]->(other)
            WITH other WHERE r IS NULL
            RETURN other
            """
      Then the result should be, in any order:
      | other        |
      | (:B {id: 2}) |
      And no side effects

      Scenario: Matching with LIMIT and optionally matching using a relationship that is already bound
      Given an empty graph
      And having executed:
      """
            CREATE (:A)-[:T]->(:B)
            """
      When executing query:
      """
            MATCH ()-[r]->()
            WITH r
              LIMIT 1
            OPTIONAL MATCH (a2)-[r]->(b2)
            RETURN a2, r, b2
            """
      Then the result should be, in any order:
      | a2   | r    | b2   |
      | (:A) | [:T] | (:B) |
      And no side effects

      Scenario: Matching with LIMIT and optionally matching using a relationship and node that are both already bound
      Given an empty graph
      And having executed:
      """
            CREATE (:A)-[:T]->(:B)
            """
      When executing query:
      """
            MATCH (a1)-[r]->()
            WITH r, a1
              LIMIT 1
            OPTIONAL MATCH (a1)-[r]->(b2)
            RETURN a1, r, b2
            """
      Then the result should be, in any order:
      | a1   | r    | b2   |
      | (:A) | [:T] | (:B) |
      And no side effects
