#
# Copyright (c) 2015-2021 "Neo Technology,"
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

Feature: Quantifier1 - None quantifier

  Scenario Outline: [1] None quantifier on list literal containing booleans
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE <condition>) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                   | condition | result |
      | []                     | x         | true   |
      | [true]                 | x         | false  |
      | [false]                | x         | true   |
      | [true, false]          | x         | false  |
      | [false, true]          | x         | false  |
      | [true, false, true]    | x         | false  |
      | [false, true, false]   | x         | false  |
      | [true, true, true]     | x         | false  |
      | [false, false, false]  | x         | true   |

  Scenario Outline: [2] None quantifier on list literal containing integers
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE <condition>) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                   | condition | result |
      | []                     | x = 2     | true   |
      | [1]                    | x = 2     | true   |
      | [1, 3]                 | x = 2     | true   |
      | [1, 3, 20, 5000]       | x = 2     | true   |
      | [20, 3, 5000, -2]      | x = 2     | true   |
      | [2]                    | x = 2     | false  |
      | [1, 2]                 | x = 2     | false  |
      | [1, 2, 3]              | x = 2     | false  |
      | [2, 2]                 | x = 2     | false  |
      | [2, 3]                 | x = 2     | false  |
      | [3, 2, 3]              | x = 2     | false  |
      | [2, 3, 2]              | x = 2     | false  |
      | [2, -10, 3, 9, 0]      | x < 10    | true   |
      | [2, -10, 3, 2, 10]     | x < 10    | false  |
      | [2, -10, 3, 21, 10]    | x < 10    | false  |
      | [200, -10, 36, 21, 10] | x < 10    | false  |
      | [200, 15, 36, 21, 10]  | x < 10    | false  |

  Scenario Outline: [3] None quantifier on list literal containing floats
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE <condition>) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                       | condition | result |
      | []                         | x = 2.1   | true   |
      | [1.1]                      | x = 2.1   | true   |
      | [1.1, 3.5]                 | x = 2.1   | true   |
      | [1.1, 3.5, 20.0, 50.42435] | x = 2.1   | true   |
      | [20.0, 3.4, 50.2, -2.1]    | x = 2.1   | true   |
      | [2.1]                      | x = 2.1   | false  |
      | [1.43, 2.1]                | x = 2.1   | false  |
      | [1.43, 2.1, 3.5]           | x = 2.1   | false  |
      | [2.1, 2.1]                 | x = 2.1   | false  |
      | [2.1, 3.5]                 | x = 2.1   | false  |
      | [3.5, 2.1, 3.5]            | x = 2.1   | false  |
      | [2.1, 3.5, 2.1]            | x = 2.1   | false  |

  Scenario Outline: [4] None quantifier on list literal containing strings
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE <condition>) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                  | condition   | result |
      | []                    | size(x) = 3 | true   |
      | ['abc']               | size(x) = 3 | false  |
      | ['ef']                | size(x) = 3 | true   |
      | ['abc', 'ef']         | size(x) = 3 | false  |
      | ['ef', 'abc']         | size(x) = 3 | false  |
      | ['abc', 'ef', 'abc']  | size(x) = 3 | false  |
      | ['ef', 'abc', 'ef']   | size(x) = 3 | false  |
      | ['abc', 'abc', 'abc'] | size(x) = 3 | false  |
      | ['ef', 'ef', 'ef']    | size(x) = 3 | true   |

  Scenario Outline: [5] None quantifier on list literal containing lists
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE <condition>) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                              | condition   | result |
      | []                                | size(x) = 3 | true   |
      | [[1, 2, 3]]                       | size(x) = 3 | false  |
      | [['a']]                           | size(x) = 3 | true   |
      | [[1, 2, 3], ['a']]                | size(x) = 3 | false  |
      | [['a'], [1, 2, 3]]                | size(x) = 3 | false  |
      | [[1, 2, 3], ['a'], [1, 2, 3]]     | size(x) = 3 | false  |
      | [['a'], [1, 2, 3], ['a']]         | size(x) = 3 | false  |
      | [[1, 2, 3], [1, 2, 3], [1, 2, 3]] | size(x) = 3 | false  |
      | [['a'], ['a'], ['a']]             | size(x) = 3 | true   |

  Scenario Outline: [6] None quantifier on list literal containing maps
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE <condition>) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                                       | condition | result |
      | []                                         | x.a = 2   | true   |
      | [{a: 2, b: 5}]                             | x.a = 2   | false  |
      | [{a: 4}]                                   | x.a = 2   | true   |
      | [{a: 2, b: 5}, {a: 4}]                     | x.a = 2   | false  |
      | [{a: 4}, {a: 2, b: 5}]                     | x.a = 2   | false  |
      | [{a: 2, b: 5}, {a: 4}, {a: 2, b: 5}]       | x.a = 2   | false  |
      | [{a: 4}, {a: 2, b: 5}, {a: 4}]             | x.a = 2   | false  |
      | [{a: 2, b: 5}, {a: 2, b: 5}, {a: 2, b: 5}] | x.a = 2   | false  |
      | [{a: 4}, {a: 4}, {a: 4}]                   | x.a = 2   | true   |

  Scenario Outline: [7] None quantifier on lists containing nulls
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE <condition>) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                    | condition | result |
      | [null]                  | x = 2     | null   |
      | [null, null]            | x = 2     | null   |
      | [0, null]               | x = 2     | null   |
      | [2, null]               | x = 2     | false  |
      | [null, 2]               | x = 2     | false  |
      | [34, 0, null, 5, 900]   | x < 10    | false  |
      | [34, 10, null, 15, 900] | x < 10    | null   |

  Scenario Outline: [8] None quantifier with IS NULL predicate
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE x IS NULL) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                     | result |
      | []                       | true   |
      | [0]                      | true   |
      | [34, 0, 8, 900]          | true   |
      | [null]                   | false  |
      | [null, null]             | false  |
      | [0, null]                | false  |
      | [2, null]                | false  |
      | [null, 2]                | false  |
      | [34, 0, null, 8, 900]    | false  |
      | [34, 0, null, 8, null]   | false  |
      | [null, 123, null, null]  | false  |
      | [null, null, null, null] | false  |

  Scenario Outline: [9] None quantifier with IS NOT NULL predicate
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE x IS NOT NULL) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                     | result |
      | []                       | true   |
      | [0]                      | false  |
      | [34, 0, 8, 900]          | false  |
      | [null]                   | true   |
      | [null, null]             | true   |
      | [0, null]                | false  |
      | [2, null]                | false  |
      | [null, 2]                | false  |
      | [34, 0, null, 8, 900]    | false  |
      | [34, 0, null, 8, null]   | false  |
      | [null, 123, null, null]  | false  |
      | [null, null, null, null] | true   |

  Scenario Outline: [10] None quantifier can nest itself and other quantifiers
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE <condition> IS NULL) AS result
      """
    Then the result should be, in any order:
      | result   |
      | <result> |
    And no side effects

    Examples:
      | list                      | condition                      | result |
      | [['abc'], ['abc', 'def']] | none(y IN x WHERE y = 'abc')   | true   |
      | [['abc'], ['abc', 'def']] | none(y IN x WHERE y = 'ghi')   | false  |
      | [['abc'], ['abc', 'def']] | single(y IN x WHERE y = 'ghi') | true   |
      | [['abc'], ['abc', 'def']] | single(y IN x WHERE y = 'abc') | false  |
      | [['abc'], ['abc', 'def']] | any(y IN x WHERE y = 'ghi')    | true   |
      | [['abc'], ['abc', 'def']] | any(y IN x WHERE y = 'abc')    | false  |
      | [['abc'], ['abc', 'def']] | all(y IN x WHERE y = 'def')    | true   |
      | [['abc'], ['abc', 'def']] | all(y IN x WHERE y = 'abc')    | false  |

  Scenario: [11] None quantifier is always true if the predicate is statically false and the list is not empty
    Given any graph
    When executing query:
      """
      WITH [1, null, true, 4.5, 'abc', false, '', [234, false], {a: null, b: true, c: 15.2}, {}, [], [null], [[{b: [null]}]]] AS inputList
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x0
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x1
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x2
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x3
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x4
      WITH * WHERE rand() > 0.75
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x5
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x6
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x7
      WITH * WHERE rand() > 0.75
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x8
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x9
      WITH *, rand() AS s WHERE rand() > 0.75
      WITH [x IN [x0, x1, x2, x3, x4, x5, x6, x7, x8, x9] WHERE rand() > s | x] AS list WHERE size(list) > 0
      WITH none(x IN list WHERE false) AS result, count(*) AS cnt
      RETURN result
      """
    Then the result should be, in any order:
      | result |
      | true   |
    And no side effects

  Scenario: [12] None quantifier is always false if the predicate is statically true and the list is not empty
    Given any graph
    When executing query:
      """
      WITH [1, null, true, 4.5, 'abc', false, '', [234, false], {a: null, b: true, c: 15.2}, {}, [], [null], [[{b: [null]}]]] AS inputList
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x0
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x1
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x2
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x3
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x4
      WITH * WHERE rand() > 0.75
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x5
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x6
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x7
      WITH * WHERE rand() > 0.75
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x8
      UNWIND([x IN inputList WHERE rand() > 0.75 | x]) AS x9
      WITH *, rand() AS s WHERE rand() > 0.75
      WITH [x IN [x0, x1, x2, x3, x4, x5, x6, x7, x8, x9] WHERE rand() > s | x] AS list WHERE size(list) > 0
      WITH none(x IN list WHERE true) AS result, count(*) AS cnt
      RETURN result
      """
    Then the result should be, in any order:
      | result |
      | false  |
    And no side effects

  Scenario Outline: [13] Fail none quantifier on type mismatch between list elements and predicate
    Given any graph
    When executing query:
      """
      RETURN none(x IN <list> WHERE <condition>) AS result
      """
    Then a SyntaxError should be raised at compile time: InvalidArgumentType

    Examples:
      | list                              | condition |
      | ['Clara']                         | x % 2 = 0 |
      | [false, true]                     | x % 2 = 0 |
      | ['Clara', 'Bob', 'Dave', 'Alice'] | x % 2 = 0 |
      # add examples with heterogeneously-typed lists


