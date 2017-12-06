/*
 * Copyright (c) 2015-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.tools.tck.values

import org.opencypher.tools.tck.TckTestSupport

class CypherValueTest extends TckTestSupport {

  test("list comparisons") {
    val oList1 = CypherOrderedList(List(CypherInteger(1), CypherInteger(2)))
    val oList2 = CypherOrderedList(List(CypherInteger(2), CypherInteger(1)))
    val uList1 = CypherUnorderedList(List(CypherInteger(2), CypherInteger(1)).sorted(CypherValue.ordering))
    val uList2 = CypherUnorderedList(List(CypherInteger(1), CypherInteger(2)).sorted(CypherValue.ordering))

    oList1 should equal(oList1)
    oList2 should equal(oList2)
    uList1 should equal(uList1)
    uList2 should equal(uList2)

    oList1 should not equal oList2
    oList2 should not equal oList1

    uList1 should equal(oList1)
    uList1 should equal(oList2)
    uList1 should equal(uList2)
    uList2 should equal(oList1)
    uList2 should equal(oList2)
    uList2 should equal(uList1)

    oList1 should equal(uList1)
    oList2 should equal(uList1)
    uList2 should equal(uList1)
    oList1 should equal(uList2)
    oList2 should equal(uList2)
    uList1 should equal(uList2)
  }
}
