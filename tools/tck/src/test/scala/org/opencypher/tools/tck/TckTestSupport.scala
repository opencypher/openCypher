/*
 * Copyright (c) 2015-2016 "Neo Technology,"
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
package org.opencypher.tools.tck

import cucumber.api.DataTable
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuiteLike, Matchers}

import scala.collection.convert.DecorateAsJava

@RunWith(classOf[JUnitRunner])
abstract class TckTestSupport extends FunSuiteLike with Matchers with DecorateAsJava {

  def tableOf(strings: Seq[String]*): DataTable = {
    DataTable.create(strings.map(_.toList.asJava).toList.asJava)
  }

}
