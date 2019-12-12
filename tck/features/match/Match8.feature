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

Feature: Match8

  Scenario: Use multiple MATCH clauses to do a Cartesian product
    Given an empty graph
    And having executed:
      """
            CREATE ({num: 1}),
              ({num: 2}),
              ({num: 3})
            """
    When executing query:
      """
            MATCH (n), (m)
            RETURN n.num AS n, m.num AS m
            """
    Then the result should be, in any order:
      | n | m |
      | 1 | 1 |
      | 1 | 2 |
      | 1 | 3 |
      | 2 | 1 |
      | 2 | 2 |
      | 2 | 3 |
      | 3 | 3 |
      | 3 | 1 |
      | 3 | 2 |
    And no side effects

  Scenario: Honour the column name for RETURN items
    Given an empty graph
    And having executed:
      """
            CREATE ({name: 'Someone'})
            """
    When executing query:
      """
            MATCH (a)
            WITH a.name AS a
            RETURN a
            """
    Then the result should be, in any order:
      | a         |
      | 'Someone' |
    And no side effects

  Scenario: Cope with shadowed variables
    Given an empty graph
    And having executed:
      """
            CREATE ({num: 1, name: 'King Kong'}),
              ({num: 2, name: 'Ann Darrow'})
            """
    When executing query:
      """
            MATCH (n)
            WITH n.name AS n
            RETURN n
            """
    Then the result should be, in any order:
      | n            |
      | 'Ann Darrow' |
      | 'King Kong'  |
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

  Scenario: Accept skip zero
    Given any graph
    When executing query:
      """
            MATCH (n)
            WHERE 1 = 0
            RETURN n SKIP 0
            """
    Then the result should be, in any order:
      | n |
    And no side effects

  Scenario: ORDER BY with LIMIT
    Given an empty graph
    And having executed:
      """
            CREATE (a:A), (n1 {num: 1}), (n2 {num: 2}),
                   (m1), (m2)
            CREATE (a)-[:T]->(n1),
                   (n1)-[:T]->(m1),
                   (a)-[:T]->(n2),
                   (n2)-[:T]->(m2)
            """
    When executing query:
      """
            MATCH (a:A)-->(n)-->(m)
            RETURN n.num, count(*)
              ORDER BY n.num
              LIMIT 1000
            """
    Then the result should be, in order:
      | n.num | count(*) |
      | 1     | 1        |
      | 2     | 1        |
    And no side effects


  Scenario: `collect()` filtering nulls
    Given an empty graph
    And having executed:
      """
            CREATE ()
            """
    When executing query:
      """
            MATCH (n)
            OPTIONAL MATCH (n)-[:NOT_EXIST]->(x)
            RETURN n, collect(x)
            """
    Then the result should be, in any order:
      | n  | collect(x) |
      | () | []         |
    And no side effects

  Scenario: Counting rows after MATCH, MERGE, OPTIONAL MATCH
    Given an empty graph
    And having executed:
      """
            CREATE (a:A), (b:B)
            CREATE (a)-[:T1]->(b),
                   (b)-[:T2]->(a)
            """
    When executing query:
      """
            MATCH (a)
            MERGE (b)
            WITH *
            OPTIONAL MATCH (a)--(b)
            RETURN count(*)
            """
    Then the result should be, in any order:
      | count(*) |
      | 6        |
    And no side effects

  Scenario: Projecting a list of nodes and relationships
    Given an empty graph
    And having executed:
      """
            CREATE (a:A), (b:B)
            CREATE (a)-[:T]->(b)
            """
    When executing query:
      """
            MATCH (n)-[r]->(m)
            RETURN [n, r, m] AS r
            """
    Then the result should be, in any order:
      | r                  |
      | [(:A), [:T], (:B)] |

  Scenario: Projecting a map of nodes and relationships
    Given an empty graph
    And having executed:
      """
            CREATE (a:A), (b:B)
            CREATE (a)-[:T]->(b)
            """
    When executing query:
      """
            MATCH (n)-[r]->(m)
            RETURN {node1: n, rel: r, node2: m} AS m
            """
    Then the result should be, in any order:
      | m                                     |
      | {node1: (:A), rel: [:T], node2: (:B)} |
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