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

Feature: SimplePatternSemantics

  Scenario: Undirected match in self-relationship graph
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH p = ()--()
      RETURN p
      """
    Then the result should be:
      | p                    |
      | <(:A)-[:LOOP]->(:A)> |
      | <(:A)<-[:LOOP]-(:A)> |
    And no side effects

  Scenario: Undirected match in self-relationship graph, count
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH ()--()
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 2        |
    And no side effects

  Scenario: Undirected match of self-relationship in self-relationship graph
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH p = (n)--(n)
      RETURN p
      """
    Then the result should be:
      | p                    |
      | <(:A)-[:LOOP]->(:A)> |
      | <(:A)<-[:LOOP]-(:A)> |
    And no side effects

  Scenario: Undirected match of self-relationship in self-relationship graph, count
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH (n)--(n)
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 2        |
    And no side effects

  Scenario: Undirected match on simple relationship graph
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:LOOP]->(:B)
      """
    When executing query:
      """
      MATCH p = ()--()
      RETURN p
      """
    Then the result should be:
      | p                    |
      | <(:A)-[:LOOP]->(:B)> |
      | <(:B)<-[:LOOP]-(:A)> |
    And no side effects

  Scenario: Undirected match on simple relationship graph, count
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:LOOP]->(:B)
      """
    When executing query:
      """
      MATCH ()--()
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 2        |
    And no side effects

  Scenario: Directed match on self-relationship graph
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH p = ()-->()
      RETURN p
      """
    Then the result should be:
      | p                    |
      | <(:A)-[:LOOP]->(:A)> |
    And no side effects

  Scenario: Directed match on self-relationship graph, count
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH ()-->()
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 1        |
    And no side effects

  Scenario: Directed match of self-relationship on self-relationship graph
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH p = (n)-->(n)
      RETURN p
      """
    Then the result should be:
      | p                    |
      | <(:A)-[:LOOP]->(:A)> |
    And no side effects

  Scenario: Directed match of self-relationship on self-relationship graph, count
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH (n)-->(n)
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 1        |
    And no side effects

  Scenario: Counting undirected self-relationships in self-relationship graph
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH (n)-[r]-(n)
      RETURN count(r)
      """
    Then the result should be:
      | count(r) |
      | 2        |
    And no side effects

  Scenario: Counting distinct undirected self-relationships in self-relationship graph
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a)
      """
    When executing query:
      """
      MATCH (n)-[r]-(n)
      RETURN count(DISTINCT r)
      """
    Then the result should be:
      | count(r) |
      | 1        |
    And no side effects

  Scenario: Directed match of a simple relationship
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:LOOP]->(:B)
      """
    When executing query:
      """
      MATCH p = ()-->()
      RETURN p
      """
    Then the result should be:
      | p                    |
      | <(:A)-[:LOOP]->(:B)> |
    And no side effects

  Scenario: Directed match of a simple relationship, count
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:LOOP]->(:B)
      """
    When executing query:
      """
      MATCH ()-->()
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 1        |
    And no side effects

  Scenario: Direction of traversed relationship is significant for path equality, simple
    Given an empty graph
    And having executed:
      """
      CREATE (n:A)-[:LOOP]->(n)
      """
    When executing query:
      """
      MATCH p1 = (:A)-->()
      MATCH p2 = (:A)<--()
      RETURN p1 = p2
      """
    Then the result should be:
      | p1 = p2 |
      | false   |
    And no side effects

  Scenario: Direction of traversed relationship is significant for path equality
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T]->(l:Looper),
             (l)-[:LOOP]->(l),
             (l)-[:T]->(:B)
      """
    When executing query:
      """
      MATCH p = (:A)-->()--()
      RETURN p
      """
    Then the result should be:
      | p                                         |
      | <(:A)-[:T]->(:Looper)-[:LOOP]->(:Looper)> |
      | <(:A)-[:T]->(:Looper)<-[:LOOP]-(:Looper)> |
      | <(:A)-[:T]->(:Looper)-[:T]->(:B)>         |
    And no side effects

  Scenario: Direction of traversed relationship is significant for path equality, count
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T]->(l:Looper),
             (l)-[:LOOP]->(l),
             (l)-[:T]->(:B)
      """
    When executing query:
      """
      MATCH (:A)-->()--()
      RETURN count(*)
      """
    Then the result should be:
      | count(*) |
      | 3        |
    And no side effects

  Scenario: Direction of traversed relationship is significant for path equality, undirected
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T]->(l:Looper),
             (l)-[:LOOP]->(l),
             (l)-[:T]->(:B)
      """
    When executing query:
      """
      MATCH p = ()--()--()
      RETURN p
      """
    Then the result should be:
      | p                                         |
      | <(:A)-[:T]->(:Looper)-[:LOOP]->(:Looper)> |
      | <(:A)-[:T]->(:Looper)<-[:LOOP]-(:Looper)> |
      | <(:A)-[:T]->(:Looper)-[:T]->(:B)>         |
      | <(:Looper)-[:LOOP]->(:Looper)<-[:T]-(:A)> |
      | <(:Looper)-[:LOOP]->(:Looper)-[:T]->(:B)> |
      | <(:Looper)<-[:LOOP]-(:Looper)<-[:T]-(:A)> |
      | <(:Looper)<-[:LOOP]-(:Looper)-[:T]->(:B)> |
      | <(:B)<-[:T]-(:Looper)-[:LOOP]->(:Looper)> |
      | <(:B)<-[:T]-(:Looper)<-[:LOOP]-(:Looper)> |
      | <(:B)<-[:T]-(:Looper)<-[:T]-(:A)>         |
    And no side effects

  Scenario: Counting directed self-relationships
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)-[:LOOP]->(a),
             ()-[:T]->()
      """
    When executing query:
      """
      MATCH (n)-[r]->(n)
      RETURN count(r)
      """
    Then the result should be:
      | count(r) |
      | 1        |
    And no side effects
