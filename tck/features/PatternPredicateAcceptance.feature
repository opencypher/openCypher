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

Feature: PatternPredicateAcceptanceTest

  Background:
    Given an empty graph

  Scenario: Filter relationships with properties using pattern predicate
    And having executed:
      """
      CREATE (n1 {node: 1}), (n2 {node: 2}), (n3 {node: 3}), (n4 {node: 4})
      CREATE (n1)-[:X {rel: 1}]->(n2),
             (n3)-[:X {rel: 2}]->(n4)
      """
    When executing query:
      """
      MATCH (n)
      WHERE (n)-[{rel: 1}]->()
      RETURN n.node AS id
      """
    Then the result should be:
      | id |
      | 1  |
    And no side effects


  Scenario: Filter using negated pattern predicate
    And having executed:
      """
      CREATE (n1 {node: 1}), (n2 {node: 2}), (n3 {node: 3}), (n4 {node: 4})
      CREATE (n1)-[:X {rel: 1}]->(n2),
             (n3)-[:X {rel: 2}]->(n4)
      """
    When executing query:
      """
      MATCH (n)
      WHERE NOT (n)-[{rel: 1}]->()
      RETURN n.node AS id
      """
    Then the result should be:
      | id |
      | 2  |
      | 3  |
      | 4  |
    And no side effects

  Scenario: Filter using a variable length relationship pattern predicate with properties
    And having executed:
      """
      UNWIND [{node: 12, rel: 42}, {node: 324234, rel: 666}] AS props
      CREATE (start:Start {p: props.node}), (middle), (end)
      CREATE (start)-[:X {prop: props.rel}]->(middle),
             (middle)-[:X {prop: props.rel}]->(end)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE (n)-[*2 {prop: 42}]->()
      RETURN n.p AS p
      """
    Then the result should be:
      | p  |
      | 12 |
    And no side effects

  Scenario: Filter using a pattern predicate that is a logical OR between an expression and a subquery
    And having executed:
      """
      UNWIND [{node: 33, rel: 42}, {node: 12, rel: 666}, {node: 55555, rel: 7777}] AS props
      CREATE (start:Start {p: props.node}), (middle), (end)
      CREATE (start)-[:X {prop: props.rel}]->(middle),
             (middle)-[:X {prop: props.rel}]->(end)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE n.p = 12 OR (n)-[*2 {prop: 42}]->()
      RETURN n.p AS p
      """
    Then the result should be:
      | p  |
      | 33 |
      | 12 |
    And no side effects

  Scenario: Filter using a pattern predicate that is a logical OR between two expressions and a subquery
    And having executed:
      """
      UNWIND [{node: 33, rel: 42}, {node: 12, rel: 666}, {node: 25, rel: 444}, {node: 55555, rel: 7777}] AS props
      CREATE (start:Start {p: props.node}), (middle), (end)
      CREATE (start)-[:X {prop: props.rel}]->(middle),
             (middle)-[:X {prop: props.rel}]->(end)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE n.p = 12 OR (n)-[*2 {prop: 42}]->() OR n.p = 25
      RETURN n.p AS p
      """
    Then the result should be:
      | p  |
      | 33 |
      | 12 |
      | 25 |
    And no side effects

  Scenario: Filter using a pattern predicate that is a logical OR between one expression and a negated subquery
    And having executed:
      """
      UNWIND [{node: 25, rel: 444}, {node: 12, rel: 42}, {node: 25, rel: 42}] AS props
      CREATE (start:Start {p: props.node}), (middle), (end)
      CREATE (start)-[:X {prop: props.rel}]->(middle),
             (middle)-[:X {prop: props.rel}]->(end)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE n.p = 12 OR NOT (n)-[*2 {prop: 42}]->()
      RETURN n.p AS p
      """
    Then the result should be:
      | p  |
      | 25 |
      | 12 |
    And no side effects

  Scenario: Filter using a pattern predicate that is a logical OR between one subquery and a negated subquery
    And having executed:
      """
      CREATE (s1:Start {id: 1}), (e1 {prop: 42}),
             (s2:Start {id: 2}), (e2 {prop: 411}), (:Start {id: 3})
      CREATE (s1)-[:X]->(e1),
             (s2)-[:X]->(e2)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE (n)-->({prop: 42}) OR NOT (n)-->()
      RETURN n.id AS id
      """
    Then the result should be:
      | id |
      | 1  |
      | 3  |
    And no side effects

  Scenario: Filter using a pattern predicate that is a logical OR between one negated subquery and a subquery
    And having executed:
      """
      CREATE (s1:Start {id: 1}), (e1 {prop: 42}),
             (s2:Start {id: 2}), (e2 {prop: 411}), (:Start {id: 3})
      CREATE (s1)-[:X]->(e1),
             (s2)-[:X]->(e2)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE NOT (n)-->() OR (n)-->({prop: 42})
      RETURN n.id AS id
      """
    Then the result should be:
      | id |
      | 1  |
      | 3  |
    And no side effects

  Scenario: Filter using a pattern predicate that is a logical OR between two subqueries
    And having executed:
      """
      CREATE (s1:Start {id: 1}), (e1 {prop: 42}),
             (s2:Start {id: 2}), (e2 {prop: 411}), (:Start {id: 3})
      CREATE (s1)-[:X]->(e1),
             (s2)-[:X]->(e2)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE (n)-->({prop: 42}) OR (n)-->({prop: 411})
      RETURN n.id AS id
      """
    Then the result should be:
      | id |
      | 1  |
      | 2  |
    And no side effects

  Scenario: Filter using a pattern predicate that is a logical OR between one negated subquery, a subquery, and an equality expression
    And having executed:
      """
      CREATE (s1:Start {id: 1}), (e1 {prop: 42}),
             (s2:Start {id: 2}), (e2 {prop: 411}), (:Start {id: 3, prop: 21})
      CREATE (s1)-[:X]->(e1),
             (s2)-[:X]->(e2)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE n.prop = 21 OR NOT (n)-->() OR (n)-->({prop: 42})
      RETURN n.id AS id
      """
    Then the result should be:
      | id |
      | 1  |
      | 3  |
    And no side effects

  Scenario: Filter using a pattern predicate that is a logical OR between one negated subquery, two subqueries, and an equality expression
    And having executed:
      """
      CREATE (s1:Start {id: 1}), (e1 {prop: 42}),
             (s2:Start {id: 2}), (e2 {prop: 411}),
             (:Start {id: 3, prop: 21}),
             (s4:Start {id: 4}), (e4 {prop: 1})
      CREATE (s1)-[:X]->(e1),
             (s2)-[:X]->(e2),
             (s4)-[:X]->(e4)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE n.prop = 21 OR NOT (n)-->() OR (n)-->({prop: 42}) OR (n)-->({prop: 1})
      RETURN n.id AS id
      """
    Then the result should be:
      | id |
      | 1  |
      | 3  |
      | 4  |
    And no side effects

  Scenario: Filter using a pattern predicate that is a logical OR between one negated subquery, two subqueries, and an equality expression 2
    And having executed:
      """
      CREATE (s1:Start {id: 1}), (e1 {prop: 42}),
             (s2:Start {id: 2}), (e2 {prop: 411}),
             (:Start {id: 3, prop: 21}),
             (s4:Start {id: 4}), (e4 {prop: 1})
      CREATE (s1)-[:X]->(e1),
             (s2)-[:X]->(e2),
             (s4)-[:X]->(e4)
      """
    When executing query:
      """
      MATCH (n:Start)
      WHERE n.prop = 21 OR (n)-->({prop: 42}) OR NOT (n)-->() OR (n)-->({prop: 1})
      RETURN n.id AS id
      """
    Then the result should be:
      | id |
      | 1  |
      | 3  |
      | 4  |
    And no side effects
