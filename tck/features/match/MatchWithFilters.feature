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

Feature: MatchWithFilters

  Scenario: Simple node property predicate
    Given an empty graph
    And having executed:
          """
          CREATE ({name: 'bar'})
          """
    When executing query:
          """
          MATCH (n)
          WHERE n.name = 'bar'
          RETURN n
          """
    Then the result should be, in any order:
      | n               |
      | ({name: 'bar'}) |
    And no side effects

  Scenario: Filter out based on node prop name
    Given an empty graph
    And having executed:
      """
            CREATE ({name: 'Someone'})<-[:X]-()-[:X]->({name: 'Andres'})
            """
    When executing query:
      """
            MATCH ()-[rel:X]-(a)
            WHERE a.name = 'Andres'
            RETURN a
            """
    Then the result should be, in any order:
      | a                  |
      | ({name: 'Andres'}) |
    And no side effects

  Scenario: Filter based on rel prop name
    Given an empty graph
    And having executed:
      """
            CREATE (:A)<-[:KNOWS {name: 'monkey'}]-()-[:KNOWS {name: 'woot'}]->(:B)
            """
    When executing query:
      """
            MATCH (node)-[r:KNOWS]->(a)
            WHERE r.name = 'monkey'
            RETURN a
            """
    Then the result should be, in any order:
      | a    |
      | (:A) |
    And no side effects

  Scenario: Handle comparison between node properties
    Given an empty graph
    And having executed:
      """
      CREATE (a:A {animal: 'monkey'}),
        (b:B {animal: 'cow'}),
        (c:C {animal: 'monkey'}),
        (d:D {animal: 'cow'}),
        (a)-[:KNOWS]->(b),
        (a)-[:KNOWS]->(c),
        (d)-[:KNOWS]->(b),
        (d)-[:KNOWS]->(c)
      """
    When executing query:
      """
      MATCH (n)-[rel]->(x)
      WHERE n.animal = x.animal
      RETURN n, x
      """
    Then the result should be, in any order:
      | n                       | x                       |
      | (:A {animal: 'monkey'}) | (:C {animal: 'monkey'}) |
      | (:D {animal: 'cow'})    | (:B {animal: 'cow'})    |
    And no side effects

  Scenario: Walk alternative relationships
    Given an empty graph
    And having executed:
      """
      CREATE (a {name: 'A'}),
        (b {name: 'B'}),
        (c {name: 'C'}),
        (a)-[:KNOWS]->(b),
        (a)-[:HATES]->(c),
        (a)-[:WONDERS]->(c)
      """
    When executing query:
      """
      MATCH (n)-[r]->(x)
      WHERE type(r) = 'KNOWS' OR type(r) = 'HATES'
      RETURN r
      """
    Then the result should be, in any order:
      | r        |
      | [:KNOWS] |
      | [:HATES] |
    And no side effects

  Scenario: Handle OR in the WHERE clause
    Given an empty graph
    And having executed:
      """
      CREATE (a:A {p1: 12}),
        (b:B {p2: 13}),
        (c:C)
      """
    When executing query:
      """
      MATCH (n)
      WHERE n.p1 = 12 OR n.p2 = 13
      RETURN n
      """
    Then the result should be, in any order:
      | n             |
      | (:A {p1: 12}) |
      | (:B {p2: 13}) |
    And no side effects

  Scenario: Rel type function works as expected
    Given an empty graph
    And having executed:
      """
      CREATE (a:A {name: 'A'}),
        (b:B {name: 'B'}),
        (c:C {name: 'C'}),
        (a)-[:KNOWS]->(b),
        (a)-[:HATES]->(c)
      """
    When executing query:
      """
      MATCH (n {name: 'A'})-[r]->(x)
      WHERE type(r) = 'KNOWS'
      RETURN x
      """
    Then the result should be, in any order:
      | x                |
      | (:B {name: 'B'}) |
    And no side effects

  Scenario: Comparing nodes for equality
    Given an empty graph
    And having executed:
      """
      CREATE (:A), (:B)
      """
    When executing query:
      """
      MATCH (a), (b)
      WHERE a <> b
      RETURN a, b
      """
    Then the result should be, in any order:
      | a    | b    |
      | (:A) | (:B) |
      | (:B) | (:A) |
    And no side effects

  Scenario: Matching with many predicates and larger pattern
    Given an empty graph
    And having executed:
      """
      CREATE (advertiser {name: 'advertiser1', id: 0}),
             (thing {name: 'Color', id: 1}),
             (red {name: 'red'}),
             (p1 {name: 'product1'}),
             (p2 {name: 'product4'})
      CREATE (advertiser)-[:ADV_HAS_PRODUCT]->(p1),
             (advertiser)-[:ADV_HAS_PRODUCT]->(p2),
             (thing)-[:AA_HAS_VALUE]->(red),
             (p1)-[:AP_HAS_VALUE]->(red),
             (p2)-[:AP_HAS_VALUE]->(red)
      """
    And parameters are:
      | 1 | 0 |
      | 2 | 1 |
    When executing query:
      """
      MATCH (advertiser)-[:ADV_HAS_PRODUCT]->(out)-[:AP_HAS_VALUE]->(red)<-[:AA_HAS_VALUE]-(a)
      WHERE advertiser.id = $1
        AND a.id = $2
        AND red.name = 'red'
        AND out.name = 'product1'
      RETURN out.name
      """
    Then the result should be, in any order:
      | out.name   |
      | 'product1' |
    And no side effects

  Scenario: Matching using a simple pattern with label predicate
    Given an empty graph
    And having executed:
      """
      CREATE (a:Person {name: 'Alice'}), (b:Person {name: 'Bob'}),
             (c), (d)
      CREATE (a)-[:T]->(c),
             (b)-[:T]->(d)
      """
    When executing query:
      """
      MATCH (n:Person)-->()
      WHERE n.name = 'Bob'
      RETURN n
      """
    Then the result should be, in any order:
      | n                       |
      | (:Person {name: 'Bob'}) |
    And no side effects

  Scenario: Variable length pattern checking labels on endnodes
    Given an empty graph
    And having executed:
      """
      CREATE (a:TheLabel {id: 0}), (b:TheLabel {id: 1}), (c:TheLabel {id: 2})
      CREATE (a)-[:T]->(b),
             (b)-[:T]->(c)
      """
    When executing query:
      """
      MATCH (a), (b)
      WHERE a.id = 0
        AND (a)-[:T]->(b:TheLabel)
        OR (a)-[:T*]->(b:MissingLabel)
      RETURN DISTINCT b
      """
    Then the result should be, in any order:
      | b                   |
      | (:TheLabel {id: 1}) |
    And no side effects

  Scenario: Non-optional matches should not return nulls
    Given an empty graph
    And having executed:
      """
            CREATE (a:A), (b:B {id: 1}), (c:C {id: 2}), (d:D)
            CREATE (a)-[:T]->(b),
                   (a)-[:T]->(c),
                   (a)-[:T]->(d),
                   (b)-[:T]->(c),
                   (b)-[:T]->(d),
                   (c)-[:T]->(d)
            """
    When executing query:
      """
            MATCH (a)--(b)--(c)--(d)--(a), (b)--(d)
            WHERE a.id = 1
              AND c.id = 2
            RETURN d
            """
    Then the result should be, in any order:
      | d    |
      | (:A) |
      | (:D) |


  Scenario: Matching using an undirected pattern
    Given an empty graph
    And having executed:
      """
            CREATE (:A {id: 0})-[:ADMIN]->(:B {id: 1})
            """
    When executing query:
      """
            MATCH (a)-[:ADMIN]-(b)
            WHERE a:A
            RETURN a.id, b.id
            """
    Then the result should be, in any order:
      | a.id | b.id |
      | 0    | 1    |
    And no side effects

  Scenario: Multiple anonymous nodes in a pattern
    Given an empty graph
    And having executed:
      """
            CREATE (:A)
            """
    When executing query:
      """
            MATCH (a)<--()<--(b)-->()-->(c)
            WHERE a:A
            RETURN c
            """
    Then the result should be, in any order:
      | c |
    And no side effects

  Scenario: Pass the path length test
    Given an empty graph
    And having executed:
      """
            CREATE (a:A {name: 'A'})-[:KNOWS]->(b:B {name: 'B'})
            """
    When executing query:
      """
            MATCH p = (n)-->(x)
            WHERE length(p) = 1
            RETURN x
            """
    Then the result should be, in any order:
      | x                |
      | (:B {name: 'B'}) |
    And no side effects


  Scenario: Do not return anything because path length does not match
    Given an empty graph
    And having executed:
      """
            CREATE (a:A {name: 'A'})-[:KNOWS]->(b:B {name: 'B'})
            """
    When executing query:
      """
            MATCH p = (n)-->(x)
            WHERE length(p) = 10
            RETURN x
            """
    Then the result should be, in any order:
      | x |
    And no side effects