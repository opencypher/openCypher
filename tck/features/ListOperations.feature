#
# Copyright (c) 2015-2018 "Neo Technology,"
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

Feature: ListOperations

  Background:
    Given any graph

  # IN operator

  Scenario: IN should work with nested list subscripting
    When executing query:
      """
      WITH [[1, 2, 3]] AS list
      RETURN 3 IN list[0] AS r
      """
    Then the result should be:
      | r    |
      | true |
    And no side effects

  Scenario: IN should work with nested literal list subscripting
    When executing query:
      """
      RETURN 3 IN [[1, 2, 3]][0] AS r
      """
    Then the result should be:
      | r    |
      | true |
    And no side effects

  Scenario: IN should work with list slices
    When executing query:
      """
      WITH [1, 2, 3] AS list
      RETURN 3 IN list[0..1] AS r
      """
    Then the result should be:
      | r     |
      | false |
    And no side effects

  Scenario: IN should work with literal list slices
    When executing query:
      """
      RETURN 3 IN [1, 2, 3][0..1] AS r
      """
    Then the result should be:
      | r     |
      | false |
    And no side effects

  Scenario: IN should return false when matching a number with a string
    When executing query:
      """
      RETURN 1 IN ['1', 2] AS res
      """
    Then the result should be:
      | res   |
      | false |
    And no side effects

  Scenario: IN should return false when matching a number with a string - list version
    When executing query:
      """
      RETURN [1, 2] IN [1, [1, '2']] AS res
      """
    Then the result should be:
      | res   |
      | false |
    And no side effects

  Scenario: IN should return false when types of LHS and RHS don't match - singleton list
    When executing query:
      """
      RETURN [1] IN [1, 2] AS res
      """
    Then the result should be:
      | res   |
      | false |
    And no side effects

  Scenario: IN should return false when types of LHS and RHS don't match - list
    When executing query:
      """
      RETURN [1, 2] IN [1, 2] AS res
      """
    Then the result should be:
      | res   |
      | false |
    And no side effects

  Scenario: IN should return true when types of LHS and RHS match - singleton list
    When executing query:
      """
      RETURN [1] IN [1, 2, [1]] AS res
      """
    Then the result should be:
      | res   |
      | true |
    And no side effects

  Scenario: IN should return true when types of LHS and RHS match - list
    When executing query:
      """
      RETURN [1, 2] IN [1, [1, 2]] AS res
      """
    Then the result should be:
      | res   |
      | true |
    And no side effects

  Scenario: IN should return false when order of elements in LHS list and RHS list don't match
    When executing query:
      """
      RETURN [1, 2] IN [1, [2, 1]] AS res
      """
    Then the result should be:
      | res   |
      | false |
    And no side effects

  Scenario: IN with different length lists should return false
    When executing query:
      """
      RETURN [1, 2] IN [1, [1, 2, 3]] AS res
      """
    Then the result should be:
      | res   |
      | false |
    And no side effects

  Scenario: IN should return false when matching a list with a nested list with same elements
    When executing query:
      """
      RETURN [1, 2] IN [1, [[1, 2]]] AS res
      """
    Then the result should be:
      | res   |
      | false |
    And no side effects

  Scenario: IN should return true when both LHS and RHS contain nested lists
    When executing query:
      """
      RETURN [[1, 2], [3, 4]] IN [5, [[1, 2], [3, 4]]] AS res
      """
    Then the result should be:
      | res   |
      | true |
    And no side effects

  Scenario: IN should return true when both LHS and RHS contain a nested list alongside a scalar element
    When executing query:
      """
      RETURN [[1, 2], 3] IN [1, [[1, 2], 3]] AS res
      """
    Then the result should be:
      | res   |
      | true |
    And no side effects

  Scenario: IN should return null if LHS and RHS null
    When executing query:
      """
      RETURN null IN [null] AS res
      """
    Then the result should be:
      | res   |
      | null  |
    And no side effects

  Scenario: IN should return null if LHS and RHS null - list version
    When executing query:
      """
      RETURN [null] IN [[null]] AS res
      """
    Then the result should be:
      | res   |
      | null  |
    And no side effects

  Scenario: IN should return null when LHS and RHS both ultimately contain null, even if LHS and RHS are of different types (nested list and flat list)
    When executing query:
      """
      RETURN [null] IN [null] AS res
      """
    Then the result should be:
      | res   |
      | null  |
    And no side effects

  Scenario: IN with different length lists should return false despite nulls
    When executing query:
      """
      RETURN [1] IN [[1, null]] AS res
      """
    Then the result should be:
      | res   |
      | false |
    And no side effects

  Scenario: IN should return true if match despite nulls
    When executing query:
      """
      RETURN 3 IN [1, null, 3] AS res
      """
    Then the result should be:
      | res   |
      | true |
    And no side effects

  Scenario: IN should return null if comparison with null is required
    When executing query:
      """
      RETURN 4 IN [1, null, 3] AS res
      """
    Then the result should be:
      | res   |
      | null |
    And no side effects

  Scenario: IN should return true if correct list found despite other lists having nulls
    When executing query:
      """
      RETURN [1, 2] IN [[null, 'foo'], [1, 2]] AS res
      """
    Then the result should be:
      | res   |
      | true |
    And no side effects

  Scenario: IN should return true if correct list found despite null being another element within containing list
    When executing query:
      """
      RETURN [1, 2] IN [1, [1, 2], null] AS res
      """
    Then the result should be:
      | res   |
      | true |
    And no side effects

  Scenario: IN should return false if no match can be found, despite nulls
    When executing query:
      """
      RETURN [1, 2] IN [[null, 'foo']] AS res
      """
    Then the result should be:
      | res   |
      | false |
    And no side effects

  Scenario: IN should return null if comparison with null is required, list version
    When executing query:
      """
      RETURN [1, 2] IN [[null, 2]] AS res
      """
    Then the result should be:
      | res   |
      | null  |
    And no side effects

  Scenario: IN should return false if different length lists compared, even if the extra element is null
    When executing query:
      """
      RETURN [1, 2] IN [1, [1, 2, null]] AS res
      """
    Then the result should be:
      | res   |
      | false  |
    And no side effects

  Scenario: IN should return null when comparing two so-called identical lists where one element is null
    When executing query:
      """
      RETURN [1, 2, null] IN [1, [1, 2, null]] AS res
      """
    Then the result should be:
      | res   |
      | null  |
    And no side effects

  Scenario: IN should return true with previous null match, list version
    When executing query:
      """
      RETURN [1, 2] IN [[null, 2], [1, 2]] AS res
      """
    Then the result should be:
      | res   |
      | true  |
    And no side effects

  Scenario: IN should return false if different length lists with nested elements compared, even if the extra element is null
    When executing query:
      """
      RETURN [[1, 2], [3, 4]] IN [5, [[1, 2], [3, 4], null]] AS res
      """
    Then the result should be:
      | res   |
      | false  |
    And no side effects

  Scenario: IN should return null if comparison with null is required, list version 2
    When executing query:
      """
      RETURN [1, 2] IN [[null, 2], [1, 3]] AS res
      """
    Then the result should be:
      | res   |
      | null  |
    And no side effects

  # Equality

  Scenario: Equality between list and literal should return false
    When executing query:
      """
      RETURN [1, 2] = 'foo' AS res
      """
    Then the result should be:
      | res   |
      | false  |
    And no side effects

  Scenario: Equality of lists of different length should return false despite nulls
    When executing query:
      """
      RETURN [1] = [1, null] AS res
      """
    Then the result should be:
      | res   |
      | false  |
    And no side effects

  Scenario: Equality between different lists with null should return false
    When executing query:
      """
      RETURN [1, 2] = [null, 'foo'] AS res
      """
    Then the result should be:
      | res   |
      | false  |
    And no side effects

  Scenario: Equality between almost equal lists with null should return null
    When executing query:
      """
      RETURN [1, 2] = [null, 2] AS res
      """
    Then the result should be:
      | res   |
      | null  |
    And no side effects

  Scenario: Equality of nested lists of different length should return false despite nulls
    When executing query:
      """
      RETURN [[1]] = [[1], [null]] AS res
      """
    Then the result should be:
      | res    |
      | false  |
    And no side effects

  Scenario: Equality between different nested lists with null should return false
    When executing query:
      """
      RETURN [[1, 2], [1, 3]] = [[1, 2], [null, 'foo']] AS res
      """
    Then the result should be:
      | res    |
      | false  |
    And no side effects

  Scenario: Equality between almost equal nested lists with null should return null
    When executing query:
      """
      RETURN [[1, 2], ['foo', 'bar']] = [[1, 2], [null, 'bar']] AS res
      """
    Then the result should be:
      | res    |
      | null  |
    And no side effects

  # General

  Scenario: Return list size
    When executing query:
      """
      RETURN size([1, 2, 3]) AS n
      """
    Then the result should be:
      | n |
      | 3 |
    And no side effects

  Scenario: Setting and returning the size of a list property
    And having executed:
      """
      CREATE (:Label)
      """
    When executing query:
      """
      MATCH (n:Label)
      SET n.x = [1, 2, 3]
      RETURN size(n.x)
      """
    Then the result should be:
      | size(n.x) |
      | 3         |
    And the side effects should be:
      | +properties | 1 |

  Scenario: Concatenating and returning the size of literal lists
    When executing query:
      """
      RETURN size([[], []] + [[]]) AS l
      """
    Then the result should be:
      | l |
      | 3 |
    And no side effects

  Scenario: Returning nested expressions based on list property
    And having executed:
      """
      CREATE (:Label)
      """
    When executing query:
      """
      MATCH (n:Label)
      SET n.array = [1, 2, 3, 4, 5]
      RETURN tail(tail(n.array))
      """
    Then the result should be:
      | tail(tail(n.array)) |
      | [3, 4, 5]           |
    And the side effects should be:
      | +properties | 1 |

  Scenario: Indexing into nested literal lists
    When executing query:
      """
      RETURN [[1]][0][0]
      """
    Then the result should be:
      | [[1]][0][0] |
      | 1           |
    And no side effects

  Scenario: Concatenating lists of same type
    When executing query:
      """
      RETURN [1, 10, 100] + [4, 5] AS foo
      """
    Then the result should be:
      | foo                |
      | [1, 10, 100, 4, 5] |
    And no side effects

  Scenario: Appending lists of same type
    When executing query:
      """
      RETURN [false, true] + false AS foo
      """
    Then the result should be:
      | foo                  |
      | [false, true, false] |
    And no side effects

  Scenario: Execute n[0]
    When executing query:
      """
      RETURN [1, 2, 3][0] AS value
      """
    Then the result should be:
      | value |
      | 1     |
    And no side effects

  Scenario: Extract eagerly
    And having executed:
      """
      CREATE (:Label1 {name: 'original'})
      """
    When executing query:
      """
      MATCH (a:Label1)
      WITH collect(a) AS nodes
      WITH nodes, extract(x IN nodes | x.name) AS oldNames
      UNWIND nodes AS n
      SET n.name = 'newName'
      RETURN n.name, oldNames
      """
    Then the result should be:
      | n.name    | oldNames   |
      | 'newName' | ['original'] |
    And the side effects should be:
      | +properties | 1 |
      | -properties | 1 |

  Scenario: Filter eagerly
    And having executed:
      """
      CREATE (:Label1 {name: 'original'})
      """
    When executing query:
      """
      MATCH (a:Label1)
      WITH collect(a) AS nodes
      WITH nodes, filter(x IN nodes WHERE x.name = 'original') AS noopFiltered
      UNWIND nodes AS n
      SET n.name = 'newName'
      RETURN n.name, length(noopFiltered)
      """
    Then the result should be:
      | n.name    | length(noopFiltered)   |
      | 'newName' | 1                      |
    And the side effects should be:
      | +properties | 1 |
      | -properties | 1 |

  Scenario: Length on filter
    When executing query:
      """
      MATCH (n)
      OPTIONAL MATCH (n)-[r]->(m)
      RETURN length(filter(x IN collect(r) WHERE x <> null)) AS cn
      """
    Then the result should be:
      | cn |
      | 0  |
    And no side effects

  # List lookup based on parameters

  Scenario: Use list lookup based on parameters when there is no type information
    And parameters are:
      | expr | ['Apa'] |
      | idx  | 0       |
    When executing query:
      """
      WITH $expr AS expr, $idx AS idx
      RETURN expr[idx] AS value
      """
    Then the result should be:
      | value |
      | 'Apa' |
    And no side effects

  Scenario: Use list lookup based on parameters when there is lhs type information
    And parameters are:
      | idx | 0 |
    When executing query:
      """
      WITH ['Apa'] AS expr
      RETURN expr[$idx] AS value
      """
    Then the result should be:
      | value |
      | 'Apa' |
    And no side effects

  Scenario: Use list lookup based on parameters when there is rhs type information
    And parameters are:
      | expr | ['Apa'] |
      | idx  | 0       |
    When executing query:
      """
      WITH $expr AS expr, $idx AS idx
      RETURN expr[toInteger(idx)] AS value
      """
    Then the result should be:
      | value |
      | 'Apa' |
    And no side effects

  # Failures at runtime

  Scenario: Fail at runtime when attempting to index with a String into a List
    And parameters are:
      | expr | ['Apa'] |
      | idx  | 'name'  |
    When executing query:
      """
      WITH $expr AS expr, $idx AS idx
      RETURN expr[idx]
      """
    Then a TypeError should be raised at runtime: ListElementAccessByNonInteger

  Scenario: Fail at runtime when trying to index into a list with a list
    And parameters are:
      | expr | ['Apa'] |
      | idx  | ['Apa'] |
    When executing query:
      """
      WITH $expr AS expr, $idx AS idx
      RETURN expr[idx]
      """
    Then a TypeError should be raised at runtime: ListElementAccessByNonInteger

  Scenario: Fail at compile time when attempting to index with a non-integer into a list
    When executing query:
      """
      WITH [1, 2, 3, 4, 5] AS list, 3.14 AS idx
      RETURN list[idx]
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType
