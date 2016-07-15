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

Feature: TriadicSelectionAcceptance

  # TODO: Replace this with a named graph (or two)
  Background:
    Given an empty graph
    And having executed:
      """
      CREATE (a:A {name: 'a'}),
             (b1:X {name: 'b1'}),
             (b2:X {name: 'b2'}),
             (b3:X {name: 'b3'}),
             (b4:X {name: 'b4'}),
             (c11:X {name: 'c11'}),
             (c12:X:Y {name: 'c12'}),
             (c21:X {name: 'c21'}),
             (c22:X:Y {name: 'c22'}),
             (c31:X {name: 'c31'}),
             (c32:X:Y {name: 'c32'}),
             (c41:X {name: 'c41'}),
             (c42:X:Y {name: 'c42'})
      CREATE (a)-[:KNOWS]->(b1),
             (a)-[:KNOWS]->(b2),
             (a)-[:FOLLOWS]->(b3),
             (a)-[:FOLLOWS]->(b4)
      CREATE (b1)-[:FRIEND]->(c11),
             (b1)-[:FRIEND]->(c12),
             (b2)-[:FRIEND]->(c21),
             (b2)-[:FRIEND]->(c22),
             (b3)-[:FRIEND]->(c31),
             (b3)-[:FRIEND]->(c32),
             (b4)-[:FRIEND]->(c41),
             (b4)-[:FRIEND]->(c42)
      CREATE (b1)-[:FRIEND]->(b2),
             (b2)-[:FRIEND]->(b3),
             (b3)-[:FRIEND]->(b4),
             (b4)-[:FRIEND]->(b1)
      """

  Scenario: Handling triadic friend of a friend
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b)-->(c)
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b2'   |
      | 'b3'   |
      | 'c11'  |
      | 'c12'  |
      | 'c21'  |
      | 'c22'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is not a friend
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b)-->(c)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b3'   |
      | 'c11'  |
      | 'c12'  |
      | 'c21'  |
      | 'c22'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is not a friend with different relationship type
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b)-->(c)
      OPTIONAL MATCH (a)-[r:FOLLOWS]->(c)
      WITH c WHERE r IS NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b2'   |
      | 'c11'  |
      | 'c12'  |
      | 'c21'  |
      | 'c22'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is not a friend with superset of relationship type
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b)-->(c)
      OPTIONAL MATCH (a)-[r]->(c)
      WITH c WHERE r IS NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'c11'  |
      | 'c12'  |
      | 'c21'  |
      | 'c22'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is not a friend with implicit subset of relationship type
    When executing query:
      """
      MATCH (a:A)-->(b)-->(c)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b3'   |
      | 'b4'   |
      | 'c11'  |
      | 'c12'  |
      | 'c21'  |
      | 'c22'  |
      | 'c31'  |
      | 'c32'  |
      | 'c41'  |
      | 'c42'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is not a friend with explicit subset of relationship type
    When executing query:
      """
      MATCH (a:A)-[:KNOWS|FOLLOWS]->(b)-->(c)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b3'   |
      | 'b4'   |
      | 'c11'  |
      | 'c12'  |
      | 'c21'  |
      | 'c22'  |
      | 'c31'  |
      | 'c32'  |
      | 'c41'  |
      | 'c42'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is not a friend with same labels
    And having executed:
      """
      MATCH (y:Y)
      REMOVE y:X
      """
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b:X)-->(c:X)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b3'   |
      | 'c11'  |
      | 'c21'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is not a friend with different labels
    And having executed:
      """
      MATCH (y:Y)
      REMOVE y:X
      """
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b:X)-->(c:Y)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'c12'  |
      | 'c22'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is not a friend with implicit subset of labels
    And having executed:
      """
      MATCH (y:Y)
      REMOVE y:X
      """
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b)-->(c:X)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b3'   |
      | 'c11'  |
      | 'c21'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is not a friend with implicit superset of labels
    And having executed:
      """
      MATCH (y:Y)
      REMOVE y:X
      """
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b:X)-->(c)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b3'   |
      | 'c11'  |
      | 'c12'  |
      | 'c21'  |
      | 'c22'  |
    And no side effects

  Scenario: Handling triadic friend of a friend that is a friend
    And having executed:
      """
      MATCH (y:Y)
      REMOVE y:X
      """
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b)-->(c)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NOT NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b2'   |
    And no side effects

  Scenario: Handling triadic friend of a friend that is a friend with different relationship type
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b)-->(c)
      OPTIONAL MATCH (a)-[r:FOLLOWS]->(c)
      WITH c WHERE r IS NOT NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b3'   |
    And no side effects

  Scenario: Handling triadic friend of a friend that is a friend with superset of relationship type
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b)-->(c)
      OPTIONAL MATCH (a)-[r]->(c)
      WITH c WHERE r IS NOT NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b2'   |
      | 'b3'   |
    And no side effects

  Scenario: Handling triadic friend of a friend that is a friend with implicit subset of relationship type
    When executing query:
      """
      MATCH (a:A)-->(b)-->(c)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NOT NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b1'   |
      | 'b2'   |
    And no side effects

  Scenario: Handling triadic friend of a friend that is a friend with explicit subset of relationship type
    When executing query:
      """
      MATCH (a:A)-[:KNOWS|FOLLOWS]->(b)-->(c)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NOT NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b1'   |
      | 'b2'   |
    And no side effects

  Scenario: Handling triadic friend of a friend that is a friend with same labels
    And having executed:
      """
      MATCH (y:Y)
      REMOVE y:X
      """
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b:X)-->(c:X)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NOT NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b2'   |
    And no side effects

  Scenario: Handling triadic friend of a friend that is a friend with different labels
    And having executed:
      """
      MATCH (y:Y)
      REMOVE y:X
      """
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b:X)-->(c:Y)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NOT NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
    And no side effects

  Scenario: Handling triadic friend of a friend that is a friend with implicit subset of labels
    And having executed:
      """
      MATCH (y:Y)
      REMOVE y:X
      """
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b)-->(c:X)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NOT NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b2'   |
    And no side effects

  Scenario: Handling triadic friend of a friend that is a friend with implicit superset of labels
    And having executed:
      """
      MATCH (y:Y)
      REMOVE y:X
      """
    When executing query:
      """
      MATCH (a:A)-[:KNOWS]->(b:X)-->(c)
      OPTIONAL MATCH (a)-[r:KNOWS]->(c)
      WITH c WHERE r IS NOT NULL
      RETURN c.name
      """
    Then the result should be:
      | c.name |
      | 'b2'   |
    And no side effects
