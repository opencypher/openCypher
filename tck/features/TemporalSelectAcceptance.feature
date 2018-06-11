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
Feature: TemporalSelectAcceptance

  Scenario: Should select date
    Given any graph
    When executing query:
      """
      UNWIND [date({year:1984, month:11, day:11}),
              localdatetime({year:1984, month:11, day:11, hour:12, minute:31, second:14, nanosecond: 645876123}),
              datetime({year:1984, month:11, day:11, hour:12, timezone: '+01:00'})] AS dd
      RETURN date(dd) AS d1,
             date({date: dd}) AS d2,
             date({date: dd, year: 28}) AS d3,
             date({date: dd, day: 28}) AS d4,
             date({date: dd, week: 1}) AS d5,
             date({date: dd, ordinalDay: 28}) AS d6,
             date({date: dd, quarter: 3}) AS d7
      """
    Then the result should be, in order:
      | d1           | d2           | d3           | d4           | d5           | d6           | d7           |
      | '1984-11-11' | '1984-11-11' | '0028-11-11' | '1984-11-28' | '1984-01-08' | '1984-01-28' | '1984-08-11' |
      | '1984-11-11' | '1984-11-11' | '0028-11-11' | '1984-11-28' | '1984-01-08' | '1984-01-28' | '1984-08-11' |
      | '1984-11-11' | '1984-11-11' | '0028-11-11' | '1984-11-28' | '1984-01-08' | '1984-01-28' | '1984-08-11' |
    And no side effects

  Scenario: Should select local time
    Given any graph
    When executing query:
      """
      UNWIND [localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:12, minute:31, second:14, microsecond: 645876, timezone: '+01:00'}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: '+01:00'})] AS dd
      RETURN localtime(dd) AS d1,
             localtime({time:dd}) AS d2,
             localtime({time:dd, second: 42}) AS d3
      """
    Then the result should be, in order:
      | d1                   | d2                   | d3                   |
      | '12:31:14.645876123' | '12:31:14.645876123' | '12:31:42.645876123' |
      | '12:31:14.645876'    | '12:31:14.645876'    | '12:31:42.645876'    |
      | '12:31:14.645'       | '12:31:14.645'       | '12:31:42.645'       |
      | '12:00'              | '12:00'              | '12:00:42'           |
    And no side effects

  Scenario: Should select time
    Given any graph
    When executing query:
      """
      UNWIND [localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:12, minute:31, second:14, microsecond: 645876, timezone: '+01:00'}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: 'Europe/Stockholm'})] AS dd
      RETURN time(dd) AS d1,
             time({time:dd}) AS d2,
             time({time:dd, timezone:'+05:00'}) AS d3,
             time({time:dd, second: 42}) AS d4,
             time({time:dd, second: 42, timezone:'+05:00'}) AS d5
      """
    Then the result should be, in order:
      | d1                      | d2                      | d3                           | d4                      | d5                           |
      | '12:31:14.645876123Z'   | '12:31:14.645876123Z'   | '12:31:14.645876123+05:00'   | '12:31:42.645876123Z'   | '12:31:42.645876123+05:00'   |
      | '12:31:14.645876+01:00' | '12:31:14.645876+01:00' | '16:31:14.645876+05:00'      | '12:31:42.645876+01:00' | '16:31:42.645876+05:00'      |
      | '12:31:14.645Z'         | '12:31:14.645Z'         | '12:31:14.645+05:00'         | '12:31:42.645Z'         | '12:31:42.645+05:00'         |
      | '12:00+01:00'           | '12:00+01:00'           | '16:00+05:00'                | '12:00:42+01:00'        | '16:00:42+05:00'             |
    And no side effects

  Scenario: Should select date into local date time
    Given any graph
    When executing query:
      """
      UNWIND [date({year:1984, month:10, day:11}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: '+01:00'})] AS dd
      RETURN localdatetime({date:dd, hour: 10, minute: 10, second: 10}) AS d1,
             localdatetime({date:dd, day: 28, hour: 10, minute: 10, second: 10}) AS d2
      """
    Then the result should be, in order:
      | d1                    | d2                    |
      | '1984-10-11T10:10:10' | '1984-10-28T10:10:10' |
      | '1984-03-07T10:10:10' | '1984-03-28T10:10:10' |
      | '1984-10-11T10:10:10' | '1984-10-28T10:10:10' |
    And no side effects

  Scenario: Should select time into local date time
    Given any graph
    When executing query:
      """
      UNWIND [localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:12, minute:31, second:14, microsecond: 645876, timezone: '+01:00'}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: '+01:00'})] AS tt
      RETURN localdatetime({year:1984, month:10, day:11, time:tt}) AS d1,
             localdatetime({year:1984, month:10, day:11, time:tt, second: 42}) AS d2
      """
    Then the result should be, in order:
      | d1                              | d2 |
      | '1984-10-11T12:31:14.645876123' | '1984-10-11T12:31:42.645876123' |
      | '1984-10-11T12:31:14.645876'    | '1984-10-11T12:31:42.645876' |
      | '1984-10-11T12:31:14.645'       | '1984-10-11T12:31:42.645' |
      | '1984-10-11T12:00'              | '1984-10-11T12:00:42' |
    And no side effects

  Scenario: Should select date and time into local date time
    Given any graph
    When executing query:
      """
      UNWIND [date({year:1984, month:10, day:11}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: '+01:00'})] AS dd
      UNWIND [localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:12, minute:31, second:14, microsecond: 645876, timezone: '+01:00'}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: '+01:00'})] AS tt
      RETURN localdatetime({date:dd, time:tt}) AS d1,
             localdatetime({date:dd, time:tt, day: 28, second: 42}) AS d2
      """
    Then the result should be, in order:
      | d1                              | d2 |
      | '1984-10-11T12:31:14.645876123' | '1984-10-28T12:31:42.645876123' |
      | '1984-10-11T12:31:14.645876'    | '1984-10-28T12:31:42.645876' |
      | '1984-10-11T12:31:14.645'       | '1984-10-28T12:31:42.645' |
      | '1984-10-11T12:00'              | '1984-10-28T12:00:42' |
      | '1984-03-07T12:31:14.645876123' | '1984-03-28T12:31:42.645876123' |
      | '1984-03-07T12:31:14.645876'    | '1984-03-28T12:31:42.645876' |
      | '1984-03-07T12:31:14.645'       | '1984-03-28T12:31:42.645' |
      | '1984-03-07T12:00'              | '1984-03-28T12:00:42' |
      | '1984-10-11T12:31:14.645876123' | '1984-10-28T12:31:42.645876123' |
      | '1984-10-11T12:31:14.645876'    | '1984-10-28T12:31:42.645876' |
      | '1984-10-11T12:31:14.645'       | '1984-10-28T12:31:42.645' |
      | '1984-10-11T12:00'              | '1984-10-28T12:00:42' |
    And no side effects

  Scenario: Should select datetime into local date time
    Given any graph
    When executing query:
      """
      UNWIND [localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: '+01:00'})] AS dd
      RETURN localdatetime(dd) AS d1,
             localdatetime({datetime:dd}) AS d2,
             localdatetime({datetime:dd, day: 28, second: 42}) AS d3
      """
    Then the result should be, in order:
      | d1                        | d2                        | d3 |
      | '1984-03-07T12:31:14.645' | '1984-03-07T12:31:14.645' | '1984-03-28T12:31:42.645' |
      | '1984-10-11T12:00'        | '1984-10-11T12:00'        | '1984-10-28T12:00:42' |
    And no side effects

  Scenario: Should select date into date time
    Given any graph
    When executing query:
      """
      UNWIND [date({year:1984, month:10, day:11}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: '+01:00'})] AS dd
      RETURN datetime({date:dd, hour: 10, minute: 10, second: 10}) AS d1,
             datetime({date:dd, hour: 10, minute: 10, second: 10, timezone:'+05:00'}) AS d2,
             datetime({date:dd, day: 28, hour: 10, minute: 10, second: 10}) AS d3,
             datetime({date:dd, day: 28, hour: 10, minute: 10, second: 10, timezone:'Pacific/Honolulu'}) AS d4
      """
    Then the result should be, in order:
      | d1                     | d2                          | d3                     | d4                          |
      | '1984-10-11T10:10:10Z' | '1984-10-11T10:10:10+05:00' | '1984-10-28T10:10:10Z' | '1984-10-28T10:10:10-10:00[Pacific/Honolulu]' |
      | '1984-03-07T10:10:10Z' | '1984-03-07T10:10:10+05:00' | '1984-03-28T10:10:10Z' | '1984-03-28T10:10:10-10:00[Pacific/Honolulu]' |
      | '1984-10-11T10:10:10Z' | '1984-10-11T10:10:10+05:00' | '1984-10-28T10:10:10Z' | '1984-10-28T10:10:10-10:00[Pacific/Honolulu]' |
    And no side effects

  Scenario: Should select time into date time
    Given any graph
    When executing query:
      """
      UNWIND [localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:12, minute:31, second:14, microsecond: 645876, timezone: '+01:00'}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: 'Europe/Stockholm'})] AS tt
      RETURN datetime({year:1984, month:10, day:11, time:tt}) AS d1,
             datetime({year:1984, month:10, day:11, time:tt, timezone:'+05:00'}) AS d2,
             datetime({year:1984, month:10, day:11, time:tt, second: 42}) AS d3,
             datetime({year:1984, month:10, day:11, time:tt, second: 42, timezone:'Pacific/Honolulu'}) AS d4
      """
    Then the result should be, in order:
      | d1                                                  | d2                                    | d3                                                  | d4 |
      | '1984-10-11T12:31:14.645876123Z'                    | '1984-10-11T12:31:14.645876123+05:00' | '1984-10-11T12:31:42.645876123Z'                    | '1984-10-11T12:31:42.645876123-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:31:14.645876+01:00'                  | '1984-10-11T16:31:14.645876+05:00'    | '1984-10-11T12:31:42.645876+01:00'                  | '1984-10-11T01:31:42.645876-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:31:14.645Z'                          | '1984-10-11T12:31:14.645+05:00'       | '1984-10-11T12:31:42.645Z'                          | '1984-10-11T12:31:42.645-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:00+01:00[Europe/Stockholm]'          | '1984-10-11T16:00+05:00'              | '1984-10-11T12:00:42+01:00[Europe/Stockholm]'       | '1984-10-11T01:00:42-10:00[Pacific/Honolulu]' |
    And no side effects

  Scenario: Should select date and time into date time
    Given any graph
    When executing query:
      """
      UNWIND [date({year:1984, month:10, day:11}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: '+01:00'})] AS dd
      UNWIND [localtime({hour:12, minute:31, second:14, nanosecond: 645876123}),
              time({hour:12, minute:31, second:14, microsecond: 645876, timezone: '+01:00'}),
              localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: 'Europe/Stockholm'})] AS tt
      RETURN datetime({date:dd, time:tt}) AS d1,
             datetime({date:dd, time:tt, timezone:'+05:00'}) AS d2,
             datetime({date:dd, time:tt, day: 28, second: 42}) AS d3,
             datetime({date:dd, time:tt, day: 28, second: 42, timezone:'Pacific/Honolulu'}) AS d4
      """
    Then the result should be, in order:
      | d1                                         | d2                                    | d3                                            | d4 |
      | '1984-10-11T12:31:14.645876123Z'           | '1984-10-11T12:31:14.645876123+05:00' | '1984-10-28T12:31:42.645876123Z'              | '1984-10-28T12:31:42.645876123-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:31:14.645876+01:00'         | '1984-10-11T16:31:14.645876+05:00'    | '1984-10-28T12:31:42.645876+01:00'            | '1984-10-28T01:31:42.645876-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:31:14.645Z'                 | '1984-10-11T12:31:14.645+05:00'       | '1984-10-28T12:31:42.645Z'                    | '1984-10-28T12:31:42.645-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:00+01:00[Europe/Stockholm]' | '1984-10-11T16:00+05:00'              | '1984-10-28T12:00:42+01:00[Europe/Stockholm]' | '1984-10-28T01:00:42-10:00[Pacific/Honolulu]' |
      | '1984-03-07T12:31:14.645876123Z'           | '1984-03-07T12:31:14.645876123+05:00' | '1984-03-28T12:31:42.645876123Z'              | '1984-03-28T12:31:42.645876123-10:00[Pacific/Honolulu]' |
      | '1984-03-07T12:31:14.645876+01:00'         | '1984-03-07T16:31:14.645876+05:00'    | '1984-03-28T12:31:42.645876+01:00'            | '1984-03-28T01:31:42.645876-10:00[Pacific/Honolulu]' |
      | '1984-03-07T12:31:14.645Z'                 | '1984-03-07T12:31:14.645+05:00'       | '1984-03-28T12:31:42.645Z'                    | '1984-03-28T12:31:42.645-10:00[Pacific/Honolulu]' |
      | '1984-03-07T12:00+01:00[Europe/Stockholm]' | '1984-03-07T16:00+05:00'              | '1984-03-28T12:00:42+02:00[Europe/Stockholm]' | '1984-03-28T00:00:42-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:31:14.645876123Z'           | '1984-10-11T12:31:14.645876123+05:00' | '1984-10-28T12:31:42.645876123Z'              | '1984-10-28T12:31:42.645876123-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:31:14.645876+01:00'         | '1984-10-11T16:31:14.645876+05:00'    | '1984-10-28T12:31:42.645876+01:00'            | '1984-10-28T01:31:42.645876-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:31:14.645Z'                 | '1984-10-11T12:31:14.645+05:00'       | '1984-10-28T12:31:42.645Z'                    | '1984-10-28T12:31:42.645-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:00+01:00[Europe/Stockholm]' | '1984-10-11T16:00+05:00'              | '1984-10-28T12:00:42+01:00[Europe/Stockholm]' | '1984-10-28T01:00:42-10:00[Pacific/Honolulu]' |
    And no side effects

  Scenario: Should select datetime into date time
    Given any graph
    When executing query:
      """
      UNWIND [localdatetime({year:1984, week:10, dayOfWeek:3, hour:12, minute:31, second:14, millisecond: 645}),
              datetime({year:1984, month:10, day:11, hour:12, timezone: 'Europe/Stockholm'})] AS dd
      RETURN datetime(dd) AS d1,
             datetime({datetime:dd}) AS d2,
             datetime({datetime:dd, timezone:'+05:00'}) AS d3,
             datetime({datetime:dd, day: 28, second: 42}) AS d4,
             datetime({datetime:dd, day: 28, second: 42, timezone:'Pacific/Honolulu'}) AS d5

      """
    Then the result should be, in order:
      | d1                                         | d2                                         | d3                               | d4                                            | d5 |
      | '1984-03-07T12:31:14.645Z'                 | '1984-03-07T12:31:14.645Z'                 | '1984-03-07T12:31:14.645+05:00'  | '1984-03-28T12:31:42.645Z'                    | '1984-03-28T12:31:42.645-10:00[Pacific/Honolulu]' |
      | '1984-10-11T12:00+01:00[Europe/Stockholm]' | '1984-10-11T12:00+01:00[Europe/Stockholm]' | '1984-10-11T16:00+05:00'         | '1984-10-28T12:00:42+01:00[Europe/Stockholm]' | '1984-10-28T01:00:42-10:00[Pacific/Honolulu]' |
    And no side effects
