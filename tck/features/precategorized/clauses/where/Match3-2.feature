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

Feature: Match3-2 - Match fixed length patterns WHERE clause scenarios

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

  Scenario: Match with relationship type predicate
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
    And no side effects

  Scenario: Do not fail when evaluating predicates with illegal operations if the AND'ed predicate evaluates to false
    Given an empty graph
    And having executed:
      """
      CREATE (root:Root {name: 'x'}),
             (child1:TextNode {var: 'text'}),
             (child2:IntNode {var: 0})
      CREATE (root)-[:T]->(child1),
             (root)-[:T]->(child2)
      """
    When executing query:
      """
      MATCH (:Root {name: 'x'})-->(i:TextNode)
      WHERE i.var > 'te'
      RETURN i
      """
    Then the result should be, in any order:
      | i                         |
      | (:TextNode {var: 'text'}) |
    And no side effects

  Scenario: Do not fail when evaluating predicates with illegal operations if the OR'd predicate evaluates to true
    Given an empty graph
    And having executed:
      """
      CREATE (root:Root {name: 'x'}),
             (child1:TextNode {var: 'text'}),
             (child2:IntNode {var: 0})
      CREATE (root)-[:T]->(child1),
             (root)-[:T]->(child2)
      """
    When executing query:
      """
      MATCH (:Root {name: 'x'})-->(i)
      WHERE exists(i.var) OR i.var > 'te'
      RETURN i
      """
    Then the result should be, in any order:
      | i                         |
      | (:TextNode {var: 'text'}) |
      | (:IntNode {var: 0})       |
    And no side effects

  Scenario: Use params in pattern matching predicates
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T {name: 'bar'}]->(:B {name: 'me'})
      """
    And parameters are:
      | param | 'bar' |
    When executing query:
      """
      MATCH (a)-[r]->(b)
      WHERE r.name = $param
      RETURN b
      """
    Then the result should be, in any order:
      | b                 |
      | (:B {name: 'me'}) |
    And no side effects
