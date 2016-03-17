#
# Copyright (c) 2002-2016 "Neo Technology,"
# Network Engine for Objects in Lund AB [http://neotechnology.com]
#
# This file is part of Neo4j.
#
# Neo4j is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

Feature: Expressions

  Background:
    Given any graph

  Scenario: n[0]
    When executing query: RETURN [1, 2, 3][0] AS value
    Then the result should be:
      | value |
      | 1     |

  Scenario: n['name'] in read queries
    And having executed: CREATE ({name: 'Apa'})
    When executing query: MATCH (n {name: 'Apa'}) RETURN n['nam' + 'e'] AS value
    Then the result should be:
      | value |
      | 'Apa' |

  Scenario: n['name'] in update queries
    When executing query: CREATE (n {name: 'Apa'}) RETURN n['nam' + 'e'] AS value
    Then the result should be:
      | value |
      | 'Apa' |

  Scenario: Uses dynamic property lookup based on parameters when there is no type information
    And parameters are:
      | expr | {name: 'Apa'} |
      | idx  | 'name'        |
    When executing query: WITH {expr} AS expr, {idx} AS idx RETURN expr[idx] AS value
    Then the result should be:
      | value |
      | 'Apa' |

  Scenario: Uses dynamic property lookup based on parameters when there is lhs type information
    And parameters are:
      | idx    |
      | 'name' |
    When executing query: CREATE (n {name: 'Apa'}) RETURN n[{idx}] AS value
    Then the result should be:
      | value |
      | 'Apa' |

  Scenario: Uses dynamic property lookup based on parameters when there is rhs type information
    And parameters are:
      | expr | {name: 'Apa'} |
      | idx  | 'name'        |
    When executing query: WITH {expr} AS expr, {idx} AS idx RETURN expr[toString(idx)] AS value
    Then the result should be:
      | value |
      | 'Apa' |

  Scenario: Uses collection lookup based on parameters when there is no type information
    And parameters are:
      | expr | ['Apa'] |
      | idx  | 0       |
    When executing query: WITH {expr} AS expr, {idx} AS idx RETURN expr[idx] AS value
    Then the result should be:
      | value |
      | 'Apa' |

  Scenario: Uses collection lookup based on parameters when there is lhs type information
    And parameters are:
      | idx | 0 |
    When executing query: WITH ['Apa'] AS expr RETURN expr[{idx}] AS value
    Then the result should be:
      | value |
      | 'Apa' |

  Scenario: Uses collection lookup based on parameters when there is rhs type information
    And parameters are:
      | expr | ['Apa'] |
      | idx  | 0       |
    When executing query: WITH {expr} AS expr, {idx} AS idx RETURN expr[toInt(idx)] AS value
    Then the result should be:
      | value |
      | 'Apa' |
