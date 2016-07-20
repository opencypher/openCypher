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

Feature: PatternExpressionAcceptance

  Scenario: Returning a pattern expression
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)
      CREATE (a)-[:T]->(:B),
             (a)-[:T]->(:C)
      """
    When executing query:
      """
      MATCH (n)
      RETURN (n)-->() AS p
      """
    Then the result should be:
      | p                                      |
      | [<(:A)-[:T]->(:C)>, <(:A)-[:T]->(:B)>] |
      | []                                     |
      | []                                     |
    And no side effects

  Scenario: Returning a pattern expression with label predicate
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (b:B), (c:C), (d:D)
      CREATE (a)-[:T]->(b),
             (a)-[:T]->(c),
             (a)-[:T]->(d)
      """
    When executing query:
      """
      MATCH (n:A)
      RETURN (n)-->(:B)
      """
    Then the result should be:
      | (n)-->(:B)          |
      | [<(:A)-[:T]->(:B)>] |
    And no side effects

  Scenario: Returning a pattern expression with bound nodes
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (b:B)
      CREATE (a)-[:T]->(b)
      """
    When executing query:
      """
      MATCH (a:A), (b:B)
      RETURN (a)-[*]->(b) AS path
      """
    Then the result should be:
      | path                |
      | [<(:A)-[:T]->(:B)>] |
    And no side effects

  Scenario: Returning a list comprehension
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)
      CREATE (a)-[:T]->(:B),
             (a)-[:T]->(:C)
      """
    When executing query:
      """
      MATCH (n)
      RETURN [x IN (n)-->() | head(nodes(x))] AS p
      """
    Then the result should be:
      | p            |
      | [(:A), (:A)] |
      | []           |
      | []           |
    And no side effects

  Scenario: Using a pattern expression in a WITH
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)
      CREATE (a)-[:T]->(:B),
             (a)-[:T]->(:C)
      """
    When executing query:
      """
      MATCH (n)-->(b)
      WITH (n)-->() AS p, count(b) AS c
      RETURN p, c
      """
    Then the result should be:
      | p                                      | c |
      | [<(:A)-[:T]->(:C)>, <(:A)-[:T]->(:B)>] | 2 |
    And no side effects

  Scenario: Using a variable-length pattern expression in a WITH
    Given an empty graph
    And having executed:
      """
      CREATE (:A)-[:T]->(:B)
      """
    When executing query:
      """
      MATCH (a:A), (b:B)
      WITH (a)-[*]->(b) AS path, count(a) AS c
      RETURN path, c
      """
    Then the result should be:
      | path                | c |
      | [<(:A)-[:T]->(:B)>] | 1 |
    And no side effects

  Scenario: Using a list comprehension in a WITH
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)
      CREATE (a)-[:T]->(:B),
             (a)-[:T]->(:C)
      """
    When executing query:
      """
      MATCH (n:A)
      WITH [x IN (n)-->() | head(nodes(x))] AS p, count(n) AS c
      RETURN p, c
      """
    Then the result should be:
      | p            | c |
      | [(:A), (:A)] | 1 |
    And no side effects

  Scenario: Using a list comprehension in a WHERE
    Given an empty graph
    And having executed:
      """
      CREATE (a:A)
      CREATE (a)-[:T]->(:B),
             (a)-[:T]->(:C)
      """
    When executing query:
      """
      MATCH (n)
      WHERE n IN [x IN (n)-->() | head(nodes(x))]
      RETURN n
      """
    Then the result should be:
      | n    |
      | (:A) |
    And no side effects

  Scenario: Using pattern expression in RETURN
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (:A), (:A)
      CREATE (a)-[:HAS]->()
      """
    When executing query:
      """
      MATCH (n:A)
      RETURN (n)-[:HAS]->() AS p
      """
    Then the result should be:
      | p                   |
      | [<(:A)-[:HAS]->()>] |
      | []                  |
      | []                  |
    And no side effects

  Scenario: Aggregating on pattern expression
    Given an empty graph
    And having executed:
      """
      CREATE (a:A), (:A), (:A)
      CREATE (a)-[:HAS]->()
      """
    When executing query:
      """
      MATCH (n:A)
      RETURN count((n)-[:HAS]->()) AS c
      """
    Then the result should be:
      | c |
      | 3 |
    And no side effects

  Scenario: Using `length()` on outgoing pattern expression
    Given an empty graph
    And having executed:
      """
      CREATE (n1:X {n: 1}), (n2:X {n: 2})
      CREATE (n1)-[:T]->(),
             (n1)-[:T]->(),
             (n1)-[:T]->(),
             ()-[:T]->(n2),
             ()-[:T]->(n2),
             ()-[:T]->(n2)
      """
    When executing query:
      """
      MATCH (n:X)
      WHERE length((n)-->()) > 2
      RETURN n
      """
    Then the result should be:
      | n           |
      | (:X {n: 1}) |
    And no side effects

  Scenario: Using `length()` on incoming pattern expression
    Given an empty graph
    And having executed:
      """
      CREATE (n1:X {n: 1}), (n2:X {n: 2})
      CREATE (n1)-[:T]->(),
             (n1)-[:T]->(),
             (n1)-[:T]->(),
             ()-[:T]->(n2),
             ()-[:T]->(n2),
             ()-[:T]->(n2)
      """
    When executing query:
      """
      MATCH (n:X)
      WHERE length((n)<--()) > 2
      RETURN n
      """
    Then the result should be:
      | n           |
      | (:X {n: 2}) |
    And no side effects

  Scenario: Using `length()` on undirected pattern expression
    Given an empty graph
    And having executed:
      """
      CREATE (n1:X {n: 1}), (n2:X {n: 2})
      CREATE (n1)-[:T]->(),
             (n1)-[:T]->(),
             (n1)-[:T]->(),
             ()-[:T]->(n2),
             ()-[:T]->(n2),
             ()-[:T]->(n2)
      """
    When executing query:
      """
      MATCH (n:X)
      WHERE length((n)--()) > 2
      RETURN n
      """
    Then the result should be:
      | n           |
      | (:X {n: 1}) |
      | (:X {n: 2}) |
    And no side effects

  Scenario: Using `length()` on pattern expression with complex relationship predicate
    Given an empty graph
    And having executed:
      """
      CREATE (n1:X {n: 1}), (n2:X {n: 2})
      CREATE (n1)-[:T]->(),
             (n1)-[:T]->(),
             (n1)-[:T]->(),
             ()-[:T]->(n2),
             ()-[:T]->(n2),
             ()-[:T]->(n2)
      """
    When executing query:
      """
      MATCH (n)
      WHERE length((n)-[:X|Y]-()) > 2
      RETURN n
      """
    Then the result should be:
      | n |
    And no side effects

  Scenario: Returning pattern expression in `exists()`
    Given an empty graph
    And having executed:
      """
      CREATE (a:X {prop: 42}), (:X {prop: 43})
      CREATE (a)-[:T]->()
      """
    When executing query:
      """
      MATCH (n:X)
      RETURN n, exists((n)--()) AS b
      """
    Then the result should be:
      | n               | b     |
      | (:X {prop: 42}) | true  |
      | (:X {prop: 43}) | false |
    And no side effects

  Scenario: Returning pattern expression in `exists()`
    Given an empty graph
    And having executed:
      """
      CREATE (a:X {prop: 42}), (:X {prop: 43})
      CREATE (a)-[:T]->()
      """
    When executing query:
      """
      MATCH (n:X)
      RETURN n, exists((n)--()) AS b
      """
    Then the result should be:
      | n               | b     |
      | (:X {prop: 42}) | true  |
      | (:X {prop: 43}) | false |
    And no side effects

  Scenario: `exists()` is case insensitive
    Given an empty graph
    And having executed:
      """
      CREATE (a:X {prop: 42}), (:X {prop: 43})
      CREATE (a)-[:T]->()
      """
    When executing query:
      """
      MATCH (n:X)
      RETURN n, EXISTS((n)--()) AS b
      """
    Then the result should be:
      | n               | b     |
      | (:X {prop: 42}) | true  |
      | (:X {prop: 43}) | false |
    And no side effects

  Scenario: Pattern expression inside list comprehension
    Given an empty graph
    And having executed:
      """
      CREATE (n1:X {n: 1}), (m1:Y), (i1:Y), (i2:Y)
      CREATE (n1)-[:T]->(m1),
             (m1)-[:T]->(i1),
             (m1)-[:T]->(i2)
      CREATE (n2:X {n: 2}), (m2), (i3:L), (i4:Y)
      CREATE (n2)-[:T]->(m2),
             (m2)-[:T]->(i3),
             (m2)-[:T]->(i4)
      """
    When executing query:
      """
      MATCH p = (n:X)-->(b)
      RETURN n, [x IN nodes(p) | length((x)-->(:Y))] AS list
      """
    Then the result should be:
      | n           | list   |
      | (:X {n: 1}) | [1, 2] |
      | (:X {n: 2}) | [0, 1] |
    And no side effects
