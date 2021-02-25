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

Feature: WithOrderBy1 - Order by a single variable
# LIMIT is used in the following scenarios to surface the effects or WITH ... ORDER BY ...
# which are otherwise lost after the WITH clause according to Cypher semantics

  Scenario: [1] Sort booleans in the expected order
    Given an empty graph
    When executing query:
      """
      UNWIND [true, false] AS bools
      WITH bools
        ORDER BY bools
        LIMIT 1
      RETURN bools
      """
    Then the result should be, in order:
      | bools |
      | false |
    And no side effects

  Scenario: [2] Sort booleans in the expected reverse order
    Given an empty graph
    When executing query:
      """
      UNWIND [true, false] AS bools
      WITH bools
        ORDER BY bools DESC
        LIMIT 1
      RETURN bools
      """
    Then the result should be, in order:
      | bools |
      | true  |
    And no side effects

  Scenario: [3] Sort integers in the expected order
    Given an empty graph
    When executing query:
      """
      UNWIND [1, 3, 2] AS ints
      WITH ints
        ORDER BY ints
        LIMIT 2
      RETURN ints
      """
    Then the result should be, in order:
      | ints |
      | 1    |
      | 2    |
    And no side effects

  Scenario: [4] Sort integers in the expected reverse order
    Given an empty graph
    When executing query:
      """
      UNWIND [1, 3, 2] AS ints
      WITH ints
        ORDER BY ints DESC
        LIMIT 2
      RETURN ints
      """
    Then the result should be, in order:
      | ints |
      | 3    |
      | 2    |
    And no side effects

  Scenario: [5] Sort floats in the expected order
    Given an empty graph
    When executing query:
      """
      UNWIND [1.5, 1.3, 999.99] AS floats
      WITH floats
        ORDER BY floats
        LIMIT 2
      RETURN floats
      """
    Then the result should be, in order:
      | floats |
      | 1.3    |
      | 1.5    |
    And no side effects

  Scenario: [6] Sort floats in the expected order
    Given an empty graph
    When executing query:
      """
      UNWIND [1.5, 1.3, 999.99] AS floats
      WITH floats
        ORDER BY floats DESC
        LIMIT 2
      RETURN floats
      """
    Then the result should be, in order:
      | floats |
      | 999.99 |
      | 1.5    |
    And no side effects

  Scenario: [7] Sort strings in the expected order
    Given an empty graph
    When executing query:
      """
      UNWIND ['.*', '', ' ', 'one'] AS strings
      WITH strings
        ORDER BY strings
        LIMIT 2
      RETURN strings
      """
    Then the result should be, in order:
      | strings |
      | ''      |
      | ' '     |
    And no side effects

  Scenario: [8] Sort strings in the expected reverse order
    Given an empty graph
    When executing query:
      """
      UNWIND ['.*', '', ' ', 'one'] AS strings
      WITH strings
        ORDER BY strings DESC
        LIMIT 2
      RETURN strings
      """
    Then the result should be, in order:
      | strings |
      | 'one'   |
      | '.*'    |
    And no side effects

  Scenario: [9] Sort lists in the expected order
    Given an empty graph
    When executing query:
      """
      UNWIND [date({year: 1910, month: 5, day: 6}),
              date({year: 1980, month: 12, day: 24}),
              date({year: 1984, month: 10, day: 12}),
              date({year: 1985, month: 5, day: 6}),
              date({year: 1980, month: 10, day: 24}),
              date({year: 1984, month: 10, day: 11})] AS dates
      WITH dates
        ORDER BY dates
        LIMIT 2
      RETURN dates
      """
    Then the result should be, in order:
      | dates        |
      | '1910-05-06' |
      | '1980-10-24' |
    And no side effects

  Scenario: [10] Sort order lists in the expected reverse order
    Given an empty graph
    When executing query:
      """
      UNWIND [date({year: 1910, month: 5, day: 6}),
              date({year: 1980, month: 12, day: 24}),
              date({year: 1984, month: 10, day: 12}),
              date({year: 1985, month: 5, day: 6}),
              date({year: 1980, month: 10, day: 24}),
              date({year: 1984, month: 10, day: 11})] AS dates
      WITH dates
        ORDER BY dates DESC
        LIMIT 2
      RETURN dates
      """
    Then the result should be, in order:
      | dates        |
      | '1985-05-06' |
      | '1984-10-12' |
    And no side effects

  Scenario: [11] Sort lists in the expected order
    Given an empty graph
    When executing query:
      """
      UNWIND [[], ['a'], ['a', 1], [1], [1, 'a'], [1, null], [null, 1], [null, 2]] AS lists
      WITH lists
        ORDER BY lists
        LIMIT 4
      RETURN lists
      """
    Then the result should be, in order:
      | lists     |
      | []        |
      | ['a']     |
      | ['a', 1]  |
      | [1]       |
    And no side effects

  Scenario: [12] Sort order lists in the expected reverse order
    Given an empty graph
    When executing query:
      """
      UNWIND [[], ['a'], ['a', 1], [1], [1, 'a'], [1, null], [null, 1], [null, 2]] AS lists
      WITH lists
        ORDER BY lists DESC
        LIMIT 4
      RETURN lists
      """
    Then the result should be, in order:
      | lists     |
      | [null, 2] |
      | [null, 1] |
      | [1, null] |
      | [1, 'a']  |
    And no side effects

  Scenario: [11] Sort distinct types in the expected order
    Given an empty graph
    And having executed:
      """
      CREATE (:N)-[:REL]->()
      """
    When executing query:
      """
      MATCH p = (n:N)-[r:REL]->()
      UNWIND [n, r, p, 1.5, ['list'], 'text', null, false, 0.0 / 0.0, {a: 'map'}] AS types
      WITH types
        ORDER BY types
        LIMIT 5
      RETURN types
      """
    Then the result should be, in any order:
      | types             |
      | {a: 'map'}        |
      | (:N)              |
      | [:REL]            |
      | ['list']          |
      | <(:N)-[:REL]->()> |
    And no side effects

  Scenario: [12] Sort distinct types in the expected reverse order
    Given an empty graph
    And having executed:
      """
      CREATE (:N)-[:REL]->()
      """
    When executing query:
      """
      MATCH p = (n:N)-[r:REL]->()
      UNWIND [n, r, p, 1.5, ['list'], 'text', null, false, 0.0 / 0.0, {a: 'map'}] AS types
      WITH types
        ORDER BY types DESC
        LIMIT 5
      RETURN types
      """
    Then the result should be, in any order:
      | types             |
      | null              |
      | NaN               |
      | 1.5               |
      | false             |
      | 'text'            |
    And no side effects

  Scenario Outline: [13] Sort binding table in ascending order by a boolean variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {bool: true}),
             (:B {bool: false}),
             (:C {bool: false}),
             (:D {bool: true}),
             (:E {bool: false})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.bool AS bool
      WITH a, bool
        ORDER BY <sort>
        LIMIT 3
      RETURN a, bool
      """
    Then the result should be, in any order:
      | a                  | bool  |
      | (:B {bool: false}) | false |
      | (:C {bool: false}) | false |
      | (:E {bool: false}) | false |
    And no side effects

    Examples:
      | sort           |
      | bool           |
      | bool ASC       |
      | bool ASCENDING |

  Scenario Outline: [14] Sort binding table in descending order by a boolean variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {bool: true}),
             (:B {bool: false}),
             (:C {bool: false}),
             (:D {bool: true}),
             (:E {bool: false})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.bool AS bool
      WITH a, bool
        ORDER BY <sort>
        LIMIT 2
      RETURN a, bool
      """
    Then the result should be, in any order:
      | a                 | bool |
      | (:A {bool: true}) | true |
      | (:D {bool: true}) | true |
    And no side effects

    Examples:
      | sort            |
      | bool DESC       |
      | bool DESCENDING |

  Scenario Outline: [15] Sort binding table in ascending order by an integer variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {num: 9}),
             (:B {num: 5}),
             (:C {num: 30}),
             (:D {num: -11}),
             (:E {num: 7054})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.num AS num
      WITH a, num
        ORDER BY <sort>
        LIMIT 3
      RETURN a, num
      """
    Then the result should be, in any order:
      | a               | num |
      | (:D {num: -11}) | -11 |
      | (:B {num: 5})   | 5   |
      | (:A {num: 9})   | 9   |
    And no side effects

    Examples:
      | sort          |
      | num           |
      | num ASC       |
      | num ASCENDING |

  Scenario Outline: [16] Sort binding table in descending order by an integer variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {num: 9}),
             (:B {num: 5}),
             (:C {num: 30}),
             (:D {num: -11}),
             (:E {num: 7054})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.num AS num
      WITH a, num
        ORDER BY <sort>
        LIMIT 3
      RETURN a, num
      """
    Then the result should be, in any order:
      | a                | num  |
      | (:A {num: 9})    | 9    |
      | (:C {num: 30})   | 30   |
      | (:E {num: 7054}) | 7054 |
    And no side effects

    Examples:
      | sort           |
      | num DESC       |
      | num DESCENDING |

  Scenario Outline: [17] Sort binding table in ascending order by a float variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {num: 5.025648}),
             (:B {num: 30.94857}),
             (:C {num: 30.94856}),
             (:D {num: -11.2943}),
             (:E {num: 7054.008})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.num AS num
      WITH a, num
        ORDER BY <sort>
        LIMIT 3
      RETURN a, num
      """
    Then the result should be, in any order:
      | a                    | num      |
      | (:D {num: -11.2943}) | -11.2943 |
      | (:A {num: 5.025648}) | 5.025648 |
      | (:C {num: 30.94856}) | 30.94856 |
    And no side effects

    Examples:
      | sort          |
      | num           |
      | num ASC       |
      | num ASCENDING |

  Scenario Outline: [18] Sort binding table in descending order by a float variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {num: 5.025648}),
             (:B {num: 30.94857}),
             (:C {num: 30.94856}),
             (:D {num: -11.2943}),
             (:E {num: 7054.008})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.num AS num
      WITH a, num
        ORDER BY <sort>
        LIMIT 3
      RETURN a, num
      """
    Then the result should be, in any order:
      | a                    | num      |
      | (:E {num: 7054.008}) | 7054.008 |
      | (:B {num: 30.94857}) | 30.94857 |
      | (:C {num: 30.94856}) | 30.94856 |
    And no side effects

    Examples:
      | sort           |
      | num DESC       |
      | num DESCENDING |

  Scenario Outline: [19] Sort binding table in ascending order by a string variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {name: 'lorem'}),
             (:B {name: 'ipsum'}),
             (:C {name: 'dolor'}),
             (:D {name: 'sit'}),
             (:E {name: 'amet'})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.name AS name
      WITH a, name
        ORDER BY <sort>
        LIMIT 3
      RETURN a, name
      """
    Then the result should be, in any order:
      | a                    | name    |
      | (:E {name: 'amet'})  | 'amet'  |
      | (:C {name: 'dolor'}) | 'dolor' |
      | (:B {name: 'ipsum'}) | 'ipsum' |
    And no side effects

    Examples:
      | sort          |
      | num           |
      | num ASC       |
      | num ASCENDING |

  Scenario Outline: [20] Sort binding table in descending order by a string variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {name: 'lorem'}),
             (:B {name: 'ipsum'}),
             (:C {name: 'dolor'}),
             (:D {name: 'sit'}),
             (:E {name: 'amet'})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.name AS name
      WITH a, name
        ORDER BY <sort>
        LIMIT 3
      RETURN a, name
      """
    Then the result should be, in any order:
      | a                    | name    |
      | (:D {name: 'sit'})   | 'sit'   |
      | (:A {name: 'lorem'}) | 'lorem' |
      | (:B {name: 'ipsum'}) | 'ipsum' |
    And no side effects

    Examples:
      | sort           |
      | num DESC       |
      | num DESCENDING |

  Scenario Outline: [21] Sort binding table in ascending order by a list variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {list: [2, 2]}),
             (:B {list: [2, -2]}),
             (:C {list: [1, 2]}),
             (:D {list: [300, 0]}),
             (:E {list: [1, -20]}),
             (:F {list: [2, -2, 100]})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.list AS list
      WITH a, list
        ORDER BY <sort>
        LIMIT 3
      RETURN a, list
      """
    Then the result should be, in any order:
      | a                      | list      |
      | (:C {list: [1, 2]})    | [1, 2]    |
      | (:E {list: [1, -20]})  | [1, -20]  |
      | (:B {list: [2, -2]})   | [2, -2]   |
    And no side effects

    Examples:
      | sort           |
      | list           |
      | list ASC       |
      | list ASCENDING |

  Scenario Outline: [22] Sort binding table in descending order by a list variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {list: [2, 2]}),
             (:B {list: [2, -2]}),
             (:C {list: [1, 2]}),
             (:D {list: [300, 0]}),
             (:E {list: [1, -20]}),
             (:F {list: [2, -2, 100]})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.list AS list
      WITH a, list
        ORDER BY <sort>
        LIMIT 3
      RETURN a, list
      """
    Then the result should be, in any order:
      | a                         | list         |
      | (:D {list: [300, 0]})     | [300, 0]     |
      | (:A {list: [2, 2]})       | [2, 2]       |
      | (:F {list: [2, -2, 100]}) | [2, -2, 100] |
    And no side effects

    Examples:
      | sort           |
      | list DESC       |
      | list DESCENDING |

  Scenario Outline: [23] Sort binding table in ascending order by a date variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {date: date({year: 1910, month: 5, day: 6})}),
             (:B {date: date({year: 1980, month: 12, day: 24})}),
             (:C {date: date({year: 1984, month: 10, day: 12})}),
             (:D {date: date({year: 1985, month: 5, day: 6})}),
             (:E {date: date({year: 1980, month: 10, day: 24})}),
             (:F {date: date({year: 1984, month: 10, day: 11})})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.date AS date
      WITH a, date
        ORDER BY <sort>
        LIMIT 2
      RETURN a, date
      """
    Then the result should be, in any order:
      | a                         | date         |
      | (:A {date: '1910-05-06'}) | '1910-05-06' |
      | (:E {date: '1980-10-24'}) | '1980-10-24' |
    And no side effects

    Examples:
      | sort           |
      | date           |
      | date ASC       |
      | date ASCENDING |

  Scenario Outline: [24] Sort binding table in descending order by a date variable projected from a node property
    Given an empty graph
    And having executed:
      """
      CREATE (:A {date: date({year: 1910, month: 5, day: 6})}),
             (:B {date: date({year: 1980, month: 12, day: 24})}),
             (:C {date: date({year: 1984, month: 10, day: 12})}),
             (:D {date: date({year: 1985, month: 5, day: 6})}),
             (:E {date: date({year: 1980, month: 10, day: 24})}),
             (:F {date: date({year: 1984, month: 10, day: 11})})
      """
    When executing query:
      """
      MATCH (a)
      WITH a, a.date AS date
      WITH a, date
        ORDER BY <sort>
        LIMIT 2
      RETURN a, date
      """
    Then the result should be, in any order:
      | a                         | date         |
      | (:D {date: '1985-05-06'}) | '1985-05-06' |
      | (:C {date: '1984-10-12'}) | '1984-10-12' |
    And no side effects

    Examples:
      | sort            |
      | date DESC       |
      | date DESCENDING |

