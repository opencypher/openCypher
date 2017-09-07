#
# Copyright 2017 "Neo Technology",
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

Feature: UnwindAcceptance

  Scenario: Creating nodes from an unwound parameter list
    Given an empty graph
    And having executed:
      """
      CREATE (:Year {year: 2016})
      """
    And parameters are:
      | events | [{year: 2016, id: 1}, {year: 2016, id: 2}] |
    When executing query:
      """
      UNWIND $events AS event
      MATCH (y:Year {year: event.year})
      MERGE (e:Event {id: event.id})
      MERGE (y)<-[:IN]-(e)
      RETURN e.id AS x
      ORDER BY x
      """
    Then the result should be, in order:
      | x |
      | 1 |
      | 2 |
    And the side effects should be:
      | +nodes         | 2 |
      | +relationships | 2 |
      | +labels        | 2 |
      | +properties    | 2 |
