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

Feature: TemporalTruncateAcceptance

  Scenario: Should truncate to millennium
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 2017, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 2017, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 2017, month: 10, day: 11})] AS d
      RETURN datetime.truncate('millennium', d) AS d1,
             datetime.truncate('millennium', d, {day: 2}) AS d2,
             localdatetime.truncate('millennium', d) AS d3,
             localdatetime.truncate('millennium', d, {day: 2}) AS d4,
             date.truncate('millennium', d) AS d5,
             date.truncate('millennium', d, {day: 2}) AS d6
      """
    Then the result should be, in order:
      | d1                       | d2                       | d3                 | d4                 | d5          | d6           |
      | '2000-01-01T00:00+01:00' | '2000-01-02T00:00+01:00' | '2000-01-01T00:00' | '2000-01-02T00:00' |'2000-01-01' | '2000-01-02' |
      | '2000-01-01T00:00Z'      | '2000-01-02T00:00Z'      | '2000-01-01T00:00' | '2000-01-02T00:00' |'2000-01-01' | '2000-01-02' |
      | '2000-01-01T00:00Z'      | '2000-01-02T00:00Z'      | '2000-01-01T00:00' | '2000-01-02T00:00' |'2000-01-01' | '2000-01-02' |

    And no side effects

  Scenario: Should truncate to millennium with time zone
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 2017, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 2017, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 2017, month: 10, day: 11})] AS d
      RETURN datetime.truncate('millennium', d, {timezone: 'Europe/Stockholm'}) AS d1
      """
    Then the result should be, in order:
      | d1                                         |
      | '2000-01-01T00:00+01:00[Europe/Stockholm]' |
      | '2000-01-01T00:00+01:00[Europe/Stockholm]' |
      | '2000-01-01T00:00+01:00[Europe/Stockholm]' |
    And no side effects

  Scenario: Should truncate to century
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('century', d) AS d1,
             datetime.truncate('century', d, {day: 2}) AS d2,
             localdatetime.truncate('century', d) AS d3,
             localdatetime.truncate('century', d, {day: 2}) AS d4,
             date.truncate('century', d) AS d5,
             date.truncate('century', d, {day: 2}) AS d6
      """
    Then the result should be, in order:
      | d1                       | d2                       | d3                 | d4                 | d5          | d6           |
      | '1900-01-01T00:00+01:00' | '1900-01-02T00:00+01:00' | '1900-01-01T00:00' | '1900-01-02T00:00' |'1900-01-01' | '1900-01-02' |
      | '1900-01-01T00:00Z'      | '1900-01-02T00:00Z'      | '1900-01-01T00:00' | '1900-01-02T00:00' |'1900-01-01' | '1900-01-02' |
      | '1900-01-01T00:00Z'      | '1900-01-02T00:00Z'      | '1900-01-01T00:00' | '1900-01-02T00:00' |'1900-01-01' | '1900-01-02' |
    And no side effects

  Scenario: Should truncate to century with time zone
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 2017, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 2017, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 2017, month: 10, day: 11})] AS d
      RETURN datetime.truncate('century', d, {timezone: 'Europe/Stockholm'}) AS d1
      """
    Then the result should be, in order:
      | d1                                         |
      | '2000-01-01T00:00+01:00[Europe/Stockholm]' |
      | '2000-01-01T00:00+01:00[Europe/Stockholm]' |
      | '2000-01-01T00:00+01:00[Europe/Stockholm]' |
    And no side effects

  Scenario: Should truncate to decade
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('decade', d) AS d1,
             datetime.truncate('decade', d, {day: 2}) AS d2,
             localdatetime.truncate('decade', d) AS d3,
             localdatetime.truncate('decade', d, {day: 2}) AS d4,
             date.truncate('decade', d) AS d5,
             date.truncate('decade', d, {day: 2}) AS d6
      """
    Then the result should be, in order:
      | d1                       | d2                       | d3                 | d4                 | d5          | d6           |
      | '1980-01-01T00:00+01:00' | '1980-01-02T00:00+01:00' | '1980-01-01T00:00' | '1980-01-02T00:00' |'1980-01-01' | '1980-01-02' |
      | '1980-01-01T00:00Z'      | '1980-01-02T00:00Z'      | '1980-01-01T00:00' | '1980-01-02T00:00' |'1980-01-01' | '1980-01-02' |
      | '1980-01-01T00:00Z'      | '1980-01-02T00:00Z'      | '1980-01-01T00:00' | '1980-01-02T00:00' |'1980-01-01' | '1980-01-02' |
    And no side effects

  Scenario: Should truncate to decade with time zone
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('decade', d, {timezone: 'Europe/Stockholm'}) AS d1
      """
    Then the result should be, in order:
      | d1                                         |
      | '1980-01-01T00:00+01:00[Europe/Stockholm]' |
      | '1980-01-01T00:00+01:00[Europe/Stockholm]' |
      | '1980-01-01T00:00+01:00[Europe/Stockholm]' |
    And no side effects

  Scenario: Should truncate to year
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('year', d) AS d1,
             datetime.truncate('year', d, {day: 2}) AS d2,
             localdatetime.truncate('year', d) AS d3,
             localdatetime.truncate('year', d, {day: 2}) AS d4,
             date.truncate('year', d) AS d5,
             date.truncate('year', d, {day: 2}) AS d6
      """
    Then the result should be, in order:
      | d1                       | d2                       | d3                 | d4                 | d5          | d6           |
      | '1984-01-01T00:00+01:00' | '1984-01-02T00:00+01:00' | '1984-01-01T00:00' | '1984-01-02T00:00' |'1984-01-01' | '1984-01-02' |
      | '1984-01-01T00:00Z'      | '1984-01-02T00:00Z'      | '1984-01-01T00:00' | '1984-01-02T00:00' |'1984-01-01' | '1984-01-02' |
      | '1984-01-01T00:00Z'      | '1984-01-02T00:00Z'      | '1984-01-01T00:00' | '1984-01-02T00:00' |'1984-01-01' | '1984-01-02' |
    And no side effects

  Scenario: Should truncate to year with time zone
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('year', d, {timezone: 'Europe/Stockholm'}) AS d1
      """
    Then the result should be, in order:
      | d1                                         |
      | '1984-01-01T00:00+01:00[Europe/Stockholm]' |
      | '1984-01-01T00:00+01:00[Europe/Stockholm]' |
      | '1984-01-01T00:00+01:00[Europe/Stockholm]' |
    And no side effects

  Scenario: Should truncate to weekYear
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 1, day: 1, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 1, day: 1, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 2, day: 1})] AS d
      RETURN datetime.truncate('weekYear', d) AS d1,
             datetime.truncate('weekYear', d, {day: 5}) AS d2,
             localdatetime.truncate('weekYear', d) AS d3,
             localdatetime.truncate('weekYear', d, {day: 5}) AS d4,
             date.truncate('weekYear', d) AS d5,
             date.truncate('weekYear', d, {day: 5}) AS d6
      """
    Then the result should be, in order:
      | d1                       | d2                       | d3                 | d4                 | d5          | d6           |
      | '1983-01-03T00:00+01:00' | '1983-01-05T00:00+01:00' | '1983-01-03T00:00' | '1983-01-05T00:00' |'1983-01-03' | '1983-01-05' |
      | '1983-01-03T00:00Z'      | '1983-01-05T00:00Z'      | '1983-01-03T00:00' | '1983-01-05T00:00' |'1983-01-03' | '1983-01-05' |
      | '1984-01-02T00:00Z'      | '1984-01-05T00:00Z'      | '1984-01-02T00:00' | '1984-01-05T00:00' |'1984-01-02' | '1984-01-05' |
    And no side effects

  Scenario: Should truncate to weekYear with time zone
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 1, day: 1, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 1, day: 1, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 2, day: 1})] AS d
      RETURN datetime.truncate('weekYear', d, {timezone: 'Europe/Stockholm'}) AS d1
      """
    Then the result should be, in order:
      | d1                                         |
      | '1983-01-03T00:00+01:00[Europe/Stockholm]' |
      | '1983-01-03T00:00+01:00[Europe/Stockholm]' |
      | '1984-01-02T00:00+01:00[Europe/Stockholm]' |
    And no side effects

  Scenario: Should truncate to quarter
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 11, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 11, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 11, day: 11})] AS d
      RETURN datetime.truncate('quarter', d) AS d1,
             datetime.truncate('quarter', d, {day: 2}) AS d2,
             localdatetime.truncate('quarter', d) AS d3,
             localdatetime.truncate('quarter', d, {day: 2}) AS d4,
             date.truncate('quarter', d) AS d5,
             date.truncate('quarter', d, {day: 2}) AS d6
      """
    Then the result should be, in order:
      | d1                       | d2                       | d3                 | d4                 | d5          | d6           |
      | '1984-10-01T00:00+01:00' | '1984-10-02T00:00+01:00' | '1984-10-01T00:00' | '1984-10-02T00:00' |'1984-10-01' | '1984-10-02' |
      | '1984-10-01T00:00Z'      | '1984-10-02T00:00Z'      | '1984-10-01T00:00' | '1984-10-02T00:00' |'1984-10-01' | '1984-10-02' |
      | '1984-10-01T00:00Z'      | '1984-10-02T00:00Z'      | '1984-10-01T00:00' | '1984-10-02T00:00' |'1984-10-01' | '1984-10-02' |
    And no side effects

  Scenario: Should truncate to quarter with time zone
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 11, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 11, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 11, day: 11})] AS d
      RETURN datetime.truncate('quarter', d, {timezone: 'Europe/Stockholm'}) AS d1
      """
    Then the result should be, in order:
      | d1                                         |
      | '1984-10-01T00:00+01:00[Europe/Stockholm]' |
      | '1984-10-01T00:00+01:00[Europe/Stockholm]' |
      | '1984-10-01T00:00+01:00[Europe/Stockholm]' |
    And no side effects

  Scenario: Should truncate to month
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('month', d) AS d1,
             datetime.truncate('month', d, {day: 2}) AS d2,
             localdatetime.truncate('month', d) AS d3,
             localdatetime.truncate('month', d, {day: 2}) AS d4,
             date.truncate('month', d) AS d5,
             date.truncate('month', d, {day: 2}) AS d6
      """
    Then the result should be, in order:
      | d1                       | d2                       | d3                 | d4                 | d5          | d6           |
      | '1984-10-01T00:00+01:00' | '1984-10-02T00:00+01:00' | '1984-10-01T00:00' | '1984-10-02T00:00' |'1984-10-01' | '1984-10-02' |
      | '1984-10-01T00:00Z'      | '1984-10-02T00:00Z'      | '1984-10-01T00:00' | '1984-10-02T00:00' |'1984-10-01' | '1984-10-02' |
      | '1984-10-01T00:00Z'      | '1984-10-02T00:00Z'      | '1984-10-01T00:00' | '1984-10-02T00:00' |'1984-10-01' | '1984-10-02' |
    And no side effects

  Scenario: Should truncate to month with time zone
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('month', d, {timezone: 'Europe/Stockholm'}) AS d1
      """
    Then the result should be, in order:
      | d1                                         |
      | '1984-10-01T00:00+01:00[Europe/Stockholm]' |
      | '1984-10-01T00:00+01:00[Europe/Stockholm]' |
      | '1984-10-01T00:00+01:00[Europe/Stockholm]' |
    And no side effects

 Scenario: Should truncate to week
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('week', d) AS d1,
             datetime.truncate('week', d, {dayOfWeek: 2}) AS d2,
             localdatetime.truncate('week', d) AS d3,
             localdatetime.truncate('week', d, {dayOfWeek: 2}) AS d4,
             date.truncate('week', d) AS d5,
             date.truncate('week', d, {dayOfWeek: 2}) AS d6
      """
    Then the result should be, in order:
      | d1                       | d2                       | d3                 | d4                 | d5          | d6           |
      | '1984-10-08T00:00+01:00' | '1984-10-09T00:00+01:00' | '1984-10-08T00:00' | '1984-10-09T00:00' |'1984-10-08' | '1984-10-09' |
      | '1984-10-08T00:00Z'      | '1984-10-09T00:00Z'      | '1984-10-08T00:00' | '1984-10-09T00:00' |'1984-10-08' | '1984-10-09' |
      | '1984-10-08T00:00Z'      | '1984-10-09T00:00Z'      | '1984-10-08T00:00' | '1984-10-09T00:00' |'1984-10-08' | '1984-10-09' |
    And no side effects

  Scenario: Should truncate to week with time zone
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('week', d, {timezone: 'Europe/Stockholm'}) AS d1
      """
    Then the result should be, in order:
      | d1                                         |
      | '1984-10-08T00:00+01:00[Europe/Stockholm]' |
      | '1984-10-08T00:00+01:00[Europe/Stockholm]' |
      | '1984-10-08T00:00+01:00[Europe/Stockholm]' |
    And no side effects

 Scenario: Should truncate to day
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('day', d) AS d1,
             datetime.truncate('day', d, {nanosecond: 2}) AS d2,
             localdatetime.truncate('day', d) AS d3,
             localdatetime.truncate('day', d, {nanosecond: 2}) AS d4,
             date.truncate('day', d) AS d5
      """
    Then the result should be, in order:
      | d1                       | d2                                    | d3                 | d4                              | d5          |
      | '1984-10-11T00:00+01:00' | '1984-10-11T00:00:00.000000002+01:00' | '1984-10-11T00:00' | '1984-10-11T00:00:00.000000002' |'1984-10-11' |
      | '1984-10-11T00:00Z'      | '1984-10-11T00:00:00.000000002Z'      | '1984-10-11T00:00' | '1984-10-11T00:00:00.000000002' |'1984-10-11' |
      | '1984-10-11T00:00Z'      | '1984-10-11T00:00:00.000000002Z'      | '1984-10-11T00:00' | '1984-10-11T00:00:00.000000002' |'1984-10-11' |
    And no side effects

  Scenario: Should truncate time to day
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN time.truncate('day', d) AS d1,
             time.truncate('day', d, {nanosecond: 2}) AS d2,
             localtime.truncate('day', d) AS d3,
             localtime.truncate('day', d, {nanosecond: 2}) AS d4
      """
    Then the result should be, in order:
      | d1            | d2                         | d3      | d4                   |
      | '00:00+01:00' | '00:00:00.000000002+01:00' | '00:00' | '00:00:00.000000002' |
      | '00:00Z'      | '00:00:00.000000002Z'      | '00:00' | '00:00:00.000000002' |

    And no side effects
  Scenario: Should truncate to day with time zone
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              date({year: 1984, month: 10, day: 11})] AS d
      RETURN datetime.truncate('day', d, {timezone: 'Europe/Stockholm'}) AS d1
      """
    Then the result should be, in order:
      | d1                                         |
      | '1984-10-11T00:00+01:00[Europe/Stockholm]' |
      | '1984-10-11T00:00+01:00[Europe/Stockholm]' |
      | '1984-10-11T00:00+01:00[Europe/Stockholm]' |
    And no side effects

 Scenario: Should truncate datetime to hour
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN datetime.truncate('hour', d) AS d1,
             datetime.truncate('hour', d, {nanosecond: 2}) AS d2,
             datetime.truncate('hour', d, {timezone: 'Europe/Stockholm'}) AS d3
      """
    Then the result should be, in order:
      | d1                       | d2                                    | d3                                          |
      | '1984-10-11T12:00-01:00' | '1984-10-11T12:00:00.000000002-01:00' |  '1984-10-11T12:00+01:00[Europe/Stockholm]' |
      | '1984-10-11T12:00Z'      | '1984-10-11T12:00:00.000000002Z'      |  '1984-10-11T12:00+01:00[Europe/Stockholm]' |
    And no side effects

 Scenario: Should truncate localdatetime to hour
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localdatetime.truncate('hour', d) AS d1,
             localdatetime.truncate('hour', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                      | d2                              |
      | '1984-10-11T12:00'      | '1984-10-11T12:00:00.000000002' |
      | '1984-10-11T12:00'      | '1984-10-11T12:00:00.000000002' |
    And no side effects

 Scenario: Should truncate time to hour
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN time.truncate('hour', d) AS d1,
             time.truncate('hour', d, {nanosecond: 2}) AS d2,
             time.truncate('hour', d, {timezone: '+01:00'}) AS d3
      """
    Then the result should be, in order:
      | d1            | d2                         | d3            |
      | '12:00-01:00' | '12:00:00.000000002-01:00' | '12:00+01:00' |
      | '12:00Z'      | '12:00:00.000000002Z'      | '12:00+01:00' |
      | '12:00-01:00' | '12:00:00.000000002-01:00' | '12:00+01:00' |
      | '12:00Z'      | '12:00:00.000000002Z'      | '12:00+01:00' |
    And no side effects

 Scenario: Should truncate localtime to hour
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localtime.truncate('hour', d) AS d1,
             localtime.truncate('hour', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1      | d2                   |
      | '12:00' | '12:00:00.000000002' |
      | '12:00' | '12:00:00.000000002' |
      | '12:00' | '12:00:00.000000002' |
      | '12:00' | '12:00:00.000000002' |
    And no side effects

 Scenario: Should truncate datetime to minute
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '-01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN datetime.truncate('minute', d) AS d1,
             datetime.truncate('minute', d, {nanosecond: 2}) AS d2,
             datetime.truncate('minute', d, {timezone: 'Europe/Stockholm'}) AS d3
      """
    Then the result should be, in order:
      | d1                       | d2                                    | d3                                         |
      | '1984-10-11T12:31-01:00' | '1984-10-11T12:31:00.000000002-01:00' | '1984-10-11T12:31+01:00[Europe/Stockholm]' |
      | '1984-10-11T12:31Z'      | '1984-10-11T12:31:00.000000002Z'      | '1984-10-11T12:31+01:00[Europe/Stockholm]' |
    And no side effects

 Scenario: Should truncate localdatetime to minute
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localdatetime.truncate('minute', d) AS d1,
             localdatetime.truncate('minute', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                 | d2                              |
      | '1984-10-11T12:31' | '1984-10-11T12:31:00.000000002' |
      | '1984-10-11T12:31' | '1984-10-11T12:31:00.000000002' |
    And no side effects

 Scenario: Should truncate time to minute
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN time.truncate('minute', d) AS d1,
             time.truncate('minute', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1            | d2                         |
      | '12:31+01:00' | '12:31:00.000000002+01:00' |
      | '12:31Z'      | '12:31:00.000000002Z'      |
      | '12:31+01:00' | '12:31:00.000000002+01:00' |
      | '12:31Z'      | '12:31:00.000000002Z'      |
    And no side effects

 Scenario: Should truncate localtime to minute
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localtime.truncate('minute', d) AS d1,
             localtime.truncate('minute', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1      | d2                   |
      | '12:31' | '12:31:00.000000002' |
      | '12:31' | '12:31:00.000000002' |
      | '12:31' | '12:31:00.000000002' |
      | '12:31' | '12:31:00.000000002' |
   And no side effects

 Scenario: Should truncate datetime to second
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN datetime.truncate('second', d) AS d1,
             datetime.truncate('second', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                          | d2                                    |
      | '1984-10-11T12:31:14+01:00' | '1984-10-11T12:31:14.000000002+01:00' |
      | '1984-10-11T12:31:14Z'      | '1984-10-11T12:31:14.000000002Z'      |
    And no side effects

 Scenario: Should truncate localdatetime to second
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localdatetime.truncate('second', d) AS d1,
             localdatetime.truncate('second', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                    | d2                              |
      | '1984-10-11T12:31:14' | '1984-10-11T12:31:14.000000002' |
      | '1984-10-11T12:31:14' | '1984-10-11T12:31:14.000000002' |
    And no side effects

 Scenario: Should truncate time to second
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN time.truncate('second', d) AS d1,
             time.truncate('second', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1               | d2                         |
      | '12:31:14+01:00' | '12:31:14.000000002+01:00' |
      | '12:31:14Z'      | '12:31:14.000000002Z'      |
      | '12:31:14+01:00' | '12:31:14.000000002+01:00' |
      | '12:31:14Z'      | '12:31:14.000000002Z'      |
    And no side effects

 Scenario: Should truncate localtime to second
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localtime.truncate('second', d) AS d1,
             localtime.truncate('second', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1         | d2                   |
      | '12:31:14' | '12:31:14.000000002' |
      | '12:31:14' | '12:31:14.000000002' |
      | '12:31:14' | '12:31:14.000000002' |
      | '12:31:14' | '12:31:14.000000002' |
    And no side effects

 Scenario: Should truncate datetime to millisecond
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN datetime.truncate('millisecond', d) AS d1,
             datetime.truncate('millisecond', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                              | d2                                    |
      | '1984-10-11T12:31:14.645+01:00' | '1984-10-11T12:31:14.645000002+01:00' |
      | '1984-10-11T12:31:14.645Z'      | '1984-10-11T12:31:14.645000002Z'      |
    And no side effects

 Scenario: Should truncate localdatetime to millisecond
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localdatetime.truncate('millisecond', d) AS d1,
             localdatetime.truncate('millisecond', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                        | d2                              |
      | '1984-10-11T12:31:14.645' | '1984-10-11T12:31:14.645000002' |
      | '1984-10-11T12:31:14.645' | '1984-10-11T12:31:14.645000002' |
    And no side effects

 Scenario: Should truncate time to millisecond
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN time.truncate('millisecond', d) AS d1,
             time.truncate('millisecond', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                   | d2                         |
      | '12:31:14.645+01:00' | '12:31:14.645000002+01:00' |
      | '12:31:14.645Z'      | '12:31:14.645000002Z'      |
      | '12:31:14.645+01:00' | '12:31:14.645000002+01:00' |
      | '12:31:14.645Z'      | '12:31:14.645000002Z'      |
    And no side effects

 Scenario: Should truncate localtime to millisecond
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localtime.truncate('millisecond', d) AS d1,
             localtime.truncate('millisecond', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1             | d2                   |
      | '12:31:14.645' | '12:31:14.645000002' |
      | '12:31:14.645' | '12:31:14.645000002' |
      | '12:31:14.645' | '12:31:14.645000002' |
      | '12:31:14.645' | '12:31:14.645000002' |
    And no side effects

 Scenario: Should truncate datetime to microsecond
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN datetime.truncate('microsecond', d) AS d1,
             datetime.truncate('microsecond', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                                 | d2                                    |
      | '1984-10-11T12:31:14.645876+01:00' | '1984-10-11T12:31:14.645876002+01:00' |
      | '1984-10-11T12:31:14.645876Z'      | '1984-10-11T12:31:14.645876002Z'      |
    And no side effects

 Scenario: Should truncate localdatetime to microsecond
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localdatetime.truncate('microsecond', d) AS d1,
             localdatetime.truncate('microsecond', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                           | d2                              |
      | '1984-10-11T12:31:14.645876' | '1984-10-11T12:31:14.645876002' |
      | '1984-10-11T12:31:14.645876' | '1984-10-11T12:31:14.645876002' |
    And no side effects

 Scenario: Should truncate time to microsecond
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN time.truncate('microsecond', d) AS d1,
             time.truncate('microsecond', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                      | d2                         |
      | '12:31:14.645876+01:00' | '12:31:14.645876002+01:00' |
      | '12:31:14.645876Z'      | '12:31:14.645876002Z'      |
      | '12:31:14.645876+01:00' | '12:31:14.645876002+01:00' |
      | '12:31:14.645876Z'      | '12:31:14.645876002Z'      |
    And no side effects

 Scenario: Should truncate localtime to microsecond
    Given any graph
    When executing query:
      """
      UNWIND [datetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localdatetime({year: 1984, month: 10, day: 11, hour: 12, minute: 31, second: 14, nanosecond: 645876123}),
              time({hour: 12, minute: 31, second: 14, nanosecond: 645876123, timezone: '+01:00'}),
              localtime({hour: 12, minute: 31, second: 14, nanosecond: 645876123})] AS d
      RETURN localtime.truncate('microsecond', d) AS d1,
             localtime.truncate('microsecond', d, {nanosecond: 2}) AS d2
      """
    Then the result should be, in order:
      | d1                | d2                   |
      | '12:31:14.645876' | '12:31:14.645876002' |
      | '12:31:14.645876' | '12:31:14.645876002' |
      | '12:31:14.645876' | '12:31:14.645876002' |
      | '12:31:14.645876' | '12:31:14.645876002' |
    And no side effects
