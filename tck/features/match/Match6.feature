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

Feature: Match6

  Scenario: Zero-length named path
    Given an empty graph
    And having executed:
      """
            CREATE ()
            """
    When executing query:
      """
            MATCH p = (a)
            RETURN p
            """
    Then the result should be, in any order:
      | p    |
      | <()> |
    And no side effects

  Scenario: Return a simple path
    Given an empty graph
    And having executed:
      """
            CREATE (a:A {name: 'A'})-[:KNOWS]->(b:B {name: 'B'})
            """
    When executing query:
      """
            MATCH p = (a {name: 'A'})-->(b)
            RETURN p
            """
    Then the result should be, in any order:
      | p                                             |
      | <(:A {name: 'A'})-[:KNOWS]->(:B {name: 'B'})> |
    And no side effects


  Scenario: Return a three node path
    Given an empty graph
    And having executed:
      """
            CREATE (a:A {name: 'A'})-[:KNOWS]->(b:B {name: 'B'})-[:KNOWS]->(c:C {name: 'C'})
            """
    When executing query:
      """
            MATCH p = (a {name: 'A'})-[rel1]->(b)-[rel2]->(c)
            RETURN p
            """
    Then the result should be, in any order:
      | p                                                                        |
      | <(:A {name: 'A'})-[:KNOWS]->(:B {name: 'B'})-[:KNOWS]->(:C {name: 'C'})> |
    And no side effects

  Scenario: Respecting direction when matching non-existent path
    Given an empty graph
    And having executed:
      """
            CREATE (a {name: 'a'}), (b {name: 'b'})
            CREATE (a)-[:T]->(b)
            """
    When executing query:
      """
            MATCH p = ({name: 'a'})<--({name: 'b'})
            RETURN p
            """
    Then the result should be, in any order:
      | p |
    And no side effects

  Scenario: Path query should return results in written order
    Given an empty graph
    And having executed:
      """
            CREATE (:Label1)<-[:TYPE]-(:Label2)
            """
    When executing query:
      """
            MATCH p = (a:Label1)<--(:Label2)
            RETURN p
            """
    Then the result should be, in any order:
      | p                              |
      | <(:Label1)<-[:TYPE]-(:Label2)> |
    And no side effects

  Scenario: Handling direction of named paths
    Given an empty graph
    And having executed:
      """
            CREATE (a:A)-[:T]->(b:B)
            """
    When executing query:
      """
            MATCH p = (b)<--(a)
            RETURN p
            """
    Then the result should be, in any order:
      | p                 |
      | <(:B)<-[:T]-(:A)> |
    And no side effects

  Scenario: Respecting direction when matching existing path
    Given an empty graph
    And having executed:
      """
            CREATE (a {name: 'a'}), (b {name: 'b'})
            CREATE (a)-[:T]->(b)
            """
    When executing query:
      """
            MATCH p = ({name: 'a'})-->({name: 'b'})
            RETURN p
            """
    Then the result should be, in any order:
      | p                                   |
      | <({name: 'a'})-[:T]->({name: 'b'})> |
    And no side effects

  Scenario: Respecting direction when matching non-existent path with multiple directions
    Given an empty graph
    And having executed:
      """
            CREATE (a), (b)
            CREATE (a)-[:T]->(b),
                   (b)-[:T]->(a)
            """
    When executing query:
      """
            MATCH p = (n)-->(k)<--(n)
            RETURN p
            """
    Then the result should be, in any order:
      | p |
    And no side effects

  Scenario: Longer path query should return results in written order
    Given an empty graph
    And having executed:
      """
            CREATE (:Label1)<-[:T1]-(:Label2)-[:T2]->(:Label3)
            """
    When executing query:
      """
            MATCH p = (a:Label1)<--(:Label2)--()
            RETURN p
            """
    Then the result should be, in any order:
      | p                                             |
      | <(:Label1)<-[:T1]-(:Label2)-[:T2]->(:Label3)> |
    And no side effects


  Scenario: Named path with alternating directed/undirected relationships
    Given an empty graph
    And having executed:
      """
            CREATE (a:A), (b:B), (c:C)
            CREATE (b)-[:T]->(a),
                   (c)-[:T]->(b)
            """
    When executing query:
      """
            MATCH p = (n)-->(m)--(o)
            RETURN p
            """
    Then the result should be, in any order:
      | p                            |
      | <(:C)-[:T]->(:B)-[:T]->(:A)> |
    And no side effects

  Scenario: Named path with multiple alternating directed/undirected relationships
    Given an empty graph
    And having executed:
      """
            CREATE (a:A), (b:B), (c:C), (d:D)
            CREATE (b)-[:T]->(a),
                   (c)-[:T]->(b),
                   (d)-[:T]->(c)
            """
    When executing query:
      """
            MATCH path = (n)-->(m)--(o)--(p)
            RETURN path
            """
    Then the result should be, in any order:
      | path                                    |
      | <(:D)-[:T]->(:C)-[:T]->(:B)-[:T]->(:A)> |
    And no side effects

  Scenario: Matching path with multiple bidirectional relationships
    Given an empty graph
    And having executed:
      """
            CREATE (a:A), (b:B)
            CREATE (a)-[:T1]->(b),
                   (b)-[:T2]->(a)
            """
    When executing query:
      """
            MATCH p=(n)<-->(k)<-->(n)
            RETURN p
            """
    Then the result should be, in any order:
      | p                              |
      | <(:A)<-[:T2]-(:B)<-[:T1]-(:A)> |
      | <(:A)-[:T1]->(:B)-[:T2]->(:A)> |
      | <(:B)<-[:T1]-(:A)<-[:T2]-(:B)> |
      | <(:B)-[:T2]->(:A)-[:T1]->(:B)> |
    And no side effects

  Scenario: Matching path with both directions should respect other directions
    Given an empty graph
    And having executed:
      """
            CREATE (a:A), (b:B)
            CREATE (a)-[:T1]->(b),
                   (b)-[:T2]->(a)
            """
    When executing query:
      """
            MATCH p = (n)<-->(k)<--(n)
            RETURN p
            """
    Then the result should be, in any order:
      | p                              |
      | <(:A)<-[:T2]-(:B)<-[:T1]-(:A)> |
      | <(:B)<-[:T1]-(:A)<-[:T2]-(:B)> |
    And no side effects

  Scenario: Named path with undirected fixed variable length pattern
    Given an empty graph
    And having executed:
      """
            CREATE (db1:Start), (db2:End), (mid), (other)
            CREATE (mid)-[:CONNECTED_TO]->(db1),
                   (mid)-[:CONNECTED_TO]->(db2),
                   (mid)-[:CONNECTED_TO]->(db2),
                   (mid)-[:CONNECTED_TO]->(other),
                   (mid)-[:CONNECTED_TO]->(other)
            """
    When executing query:
      """
            MATCH topRoute = (:Start)<-[:CONNECTED_TO]-()-[:CONNECTED_TO*3..3]-(:End)
            RETURN topRoute
            """
    Then the result should be, in any order:
      | topRoute                                                                                       |
      | <(:Start)<-[:CONNECTED_TO]-()-[:CONNECTED_TO]->()<-[:CONNECTED_TO]-()-[:CONNECTED_TO]->(:End)> |
      | <(:Start)<-[:CONNECTED_TO]-()-[:CONNECTED_TO]->()<-[:CONNECTED_TO]-()-[:CONNECTED_TO]->(:End)> |
      | <(:Start)<-[:CONNECTED_TO]-()-[:CONNECTED_TO]->()<-[:CONNECTED_TO]-()-[:CONNECTED_TO]->(:End)> |
      | <(:Start)<-[:CONNECTED_TO]-()-[:CONNECTED_TO]->()<-[:CONNECTED_TO]-()-[:CONNECTED_TO]->(:End)> |
    And no side effects

  Scenario: Variable-length named path
    Given an empty graph
    And having executed:
      """
            CREATE ()
            """
    When executing query:
      """
            MATCH p = ()-[*0..]->()
            RETURN p
            """
    Then the result should be, in any order:
      | p    |
      | <()> |
    And no side effects

  Scenario: Return a var length path
    Given an empty graph
    And having executed:
      """
            CREATE (a:A {name: 'A'})-[:KNOWS {num: 1}]->(b:B {name: 'B'})-[:KNOWS {num: 2}]->(c:C {name: 'C'})
            """
    When executing query:
      """
            MATCH p = (n {name: 'A'})-[:KNOWS*1..2]->(x)
            RETURN p
            """
    Then the result should be, in any order:
      | p                                                                                          |
      | <(:A {name: 'A'})-[:KNOWS {num: 1}]->(:B {name: 'B'})>                                     |
      | <(:A {name: 'A'})-[:KNOWS {num: 1}]->(:B {name: 'B'})-[:KNOWS {num: 2}]->(:C {name: 'C'})> |
    And no side effects

  Scenario: Return a named var length path of length zero
    Given an empty graph
    And having executed:
      """
            CREATE (a:A {name: 'A'})-[:KNOWS]->(b:B {name: 'B'})-[:FRIEND]->(c:C {name: 'C'})
            """
    When executing query:
      """
            MATCH p = (a {name: 'A'})-[:KNOWS*0..1]->(b)-[:FRIEND*0..1]->(c)
            RETURN p
            """
    Then the result should be, in any order:
      | p                                                                         |
      | <(:A {name: 'A'})>                                                        |
      | <(:A {name: 'A'})-[:KNOWS]->(:B {name: 'B'})>                             |
      | <(:A {name: 'A'})-[:KNOWS]->(:B {name: 'B'})-[:FRIEND]->(:C {name: 'C'})> |
    And no side effects

  Scenario: Return a var length path of length zero
    Given an empty graph
    And having executed:
      """
            CREATE (a:A)-[:REL]->(b:B)
            """
    When executing query:
      """
            MATCH p = (a)-[*0..1]->(b)
            RETURN a, b, length(p) AS l
            """
    Then the result should be, in any order:
      | a    | b    | l |
      | (:A) | (:A) | 0 |
      | (:B) | (:B) | 0 |
      | (:A) | (:B) | 1 |
    And no side effects

  Scenario: Return relationships by fetching them from the path
    Given an empty graph
    And having executed:
      """
            CREATE (s:Start)-[:REL {num: 1}]->(b:B)-[:REL {num: 2}]->(c:C)
            """
    When executing query:
      """
            MATCH p = (a:Start)-[:REL*2..2]->(b)
            RETURN relationships(p)
            """
    Then the result should be, in any order:
      | relationships(p)                   |
      | [[:REL {num: 1}], [:REL {num: 2}]] |
    And no side effects

  Scenario: Return relationships by fetching them from the path - starting from the end
    Given an empty graph
    And having executed:
      """
            CREATE (a:A)-[:REL {num: 1}]->(b:B)-[:REL {num: 2}]->(e:End)
            """
    When executing query:
      """
            MATCH p = (a)-[:REL*2..2]->(b:End)
            RETURN relationships(p)
            """
    Then the result should be, in any order:
      | relationships(p)                   |
      | [[:REL {num: 1}], [:REL {num: 2}]] |
    And no side effects

  Scenario: Aggregation with named paths
    Given an empty graph
    And having executed:
      """
            CREATE (n1 {num: 1}), (n2 {num: 2}),
                   (n3 {num: 3}), (n4 {num: 4})
            CREATE (n1)-[:T]->(n2),
                   (n3)-[:T]->(n4)
            """
    When executing query:
      """
            MATCH p = ()-[*]->()
            WITH count(*) AS count, p AS p
            WITH nodes(p) AS nodes
            RETURN *
            """
    Then the result should be, in any order:
      | nodes                    |
      | [({num: 1}), ({num: 2})] |
      | [({num: 3}), ({num: 4})] |
    And no side effects

  Scenario: Undirected named path
    Given an empty graph
    And having executed:
      """
            CREATE (a:Movie), (b)
            CREATE (b)-[:T]->(a)
            """
    When executing query:
      """
            MATCH p = (n:Movie)--(m)
            RETURN p
              LIMIT 1
            """
    Then the result should be, in any order:
      | p                   |
      | <(:Movie)<-[:T]-()> |
    And no side effects

  Scenario: Named path with WITH
    Given an empty graph
    And having executed:
      """
            CREATE ()
            """
    When executing query:
      """
            MATCH p = (a)
            WITH p
            RETURN p
            """
    Then the result should be, in any order:
      | p    |
      | <()> |
    And no side effects

  Scenario: Handling relationships that are already bound in variable length paths
    Given an empty graph
    And having executed:
      """
      CREATE (n0:Node),
             (n1:Node),
             (n2:Node),
             (n3:Node),
             (n0)-[:EDGE]->(n1),
             (n1)-[:EDGE]->(n2),
             (n2)-[:EDGE]->(n3)
      """
    When executing query:
      """
      MATCH ()-[r:EDGE]-()
      MATCH p = (n)-[*0..1]-()-[r]-()-[*0..1]-(m)
      RETURN count(p) AS c
      """
    Then the result should be, in any order:
      | c  |
      | 32 |
    And no side effects

