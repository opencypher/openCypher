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

Feature: ReturnAcceptanceTest

  Scenario: should limit to two hits
    Given an empty graph
      And having executed: CREATE ({name: 'A'}), ({name: 'B'}), ({name: 'C'}), ({name: 'D'}), ({name: 'E'})
    When executing query: MATCH (n) RETURN n LIMIT 2
    Then the result should be:
      | n             |
      | ({name: 'A'}) |
      | ({name: 'B'}) |

  Scenario: should start the result from second row
    Given an empty graph
      And having executed: CREATE ({name: 'A'}), ({name: 'B'}), ({name: 'C'}), ({name: 'D'}), ({name: 'E'})
    When executing query: MATCH (n) RETURN n ORDER BY n.name ASC SKIP 2
    Then the result should be, in order:
      | n             |
      | ({name: 'C'}) |
      | ({name: 'D'}) |
      | ({name: 'E'}) |

  Scenario: should start the result from second row by param
    Given an empty graph
      And having executed: CREATE ({name: 'A'}), ({name: 'B'}), ({name: 'C'}), ({name: 'D'}), ({name: 'E'})
      And parameters are:
        | skipAmount | 2 |
    When executing query: MATCH (n) RETURN n ORDER BY n.name ASC SKIP { skipAmount }
    Then the result should be, in order:
      | n             |
      | ({name: 'C'}) |
      | ({name: 'D'}) |
      | ({name: 'E'}) |

  Scenario: should get stuff in the middle
    Given an empty graph
      And having executed: CREATE ({name: 'A'}), ({name: 'B'}), ({name: 'C'}), ({name: 'D'}), ({name: 'E'})
    When executing query: MATCH (n) WHERE id(n) IN [0,1,2,3,4] RETURN n ORDER BY n.name ASC SKIP 2 LIMIT 2
    Then the result should be, in order:
      | n             |
      | ({name: 'C'}) |
      | ({name: 'D'}) |

  Scenario: should get stuff in the middle by param
    Given an empty graph
      And having executed: CREATE ({name: 'A'}), ({name: 'B'}), ({name: 'C'}), ({name: 'D'}), ({name: 'E'})
      And parameters are:
        | s | 2 |
        | l | 2 |
    When executing query: MATCH (n) WHERE id(n) IN [0,1,2,3,4] RETURN n ORDER BY n.name ASC SKIP { s } LIMIT { l }
    Then the result should be, in order:
      | n             |
      | ({name: 'C'}) |
      | ({name: 'D'}) |

  Scenario: should sort on aggregated function
    Given an empty graph
      And having executed: CREATE ({division: 'A', age: 22}), ({division: 'B', age: 33}), ({division: 'B', age: 44}), ({division: 'C', age: 55})
    When executing query: MATCH (n) WHERE id(n) IN [0,1,2,3] RETURN n.division, max(n.age) ORDER BY max(n.age)
    Then the result should be, in order:
      | n.division | max(n.age) |
      | 'A'        | 22         |
      | 'B'        | 44         |
      | 'C'        | 55         |

  Scenario: should support sort and distinct
    Given an empty graph
      And having executed: CREATE ({name: 'A'}), ({name: 'B'}), ({name: 'C'})
    When executing query: MATCH (a) WHERE id(a) IN [0,1,2,0] RETURN DISTINCT a ORDER BY a.name
    Then the result should be, in order:
      | a             |
      | ({name: 'A'}) |
      | ({name: 'B'}) |
      | ({name: 'C'}) |

  Scenario: should support column renaming
    Given an empty graph
      And having executed: CREATE (:Singleton)
    When executing query: MATCH (a) WHERE id(a) = 0 RETURN a AS ColumnName
    Then the result should be:
      | ColumnName   |
      | (:Singleton) |

  Scenario: should support ordering by a property after being distinctified
    Given an empty graph
      And having executed: CREATE (:A)-[:T]->(:B)
    When executing query: MATCH (a)-->(b) WHERE id(a) = 0 RETURN DISTINCT b ORDER BY b.name
    Then the result should be, in order:
      | b    |
      | (:B) |

  Scenario: arithmetic precedence test
    Given any graph
    When executing query: RETURN 12 / 4 * 3 - 2 * 4
    Then the result should be:
      | 12 / 4 * 3 - 2 * 4 |
      | 1                  |

  Scenario: arithmetic precedence with parenthesis test
    Given any graph
    When executing query: RETURN 12 / 4 * (3 - 2 * 4)
    Then the result should be:
      | 12 / 4 * (3 - 2 * 4) |
      | -15                  |

  Scenario: count star should count everything in scope
    Given an empty graph
      And having executed: CREATE (:l1), (:l2), (:l3)
    When executing query: MATCH (a) RETURN a, count(*) ORDER BY count(*)
    Then the result should be:
      | a     | count(*) |
      | (:l1) | 1        |
      | (:l2) | 1        |
      | (:l3) | 1        |

  Scenario: filter should work
    Given an empty graph
      And having executed: CREATE (a {foo: 1})-[:T]->({foo: 1}), (a)-[:T]->({foo: 2}), (a)-[:T]->({foo: 3})
    When executing query: MATCH (a {foo: 1}) MATCH p=(a)-->() RETURN filter(x IN nodes(p) WHERE x.foo > 2) AS n
    Then the result should be:
      | n            |
      | [({foo: 3})] |
      | []           |
      | []           |

  Scenario: should allow absolute function
    Given any graph
    When executing query: RETURN abs(-1)
    Then the result should be:
      | abs(-1) |
      | 1       |

  Scenario: should return collection size
    Given any graph
    When executing query: RETURN size([1,2,3]) AS n
    Then the result should be:
      | n |
      | 3 |
