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
import org.opencypher.tools.tck.parsing.FormatListener

import scala.collection.JavaConverters._

object verifyResults extends (DataTable => Option[String]) {

  override def apply(table: DataTable): Option[String] = {
    // TODO: Specify constraints for column names, and enforce these here
    val keys = table.topCells().asScala
    val cells = table.cells(1).asScala

    val badValues = cells.flatMap { list =>
      list.asScala.filterNot(this (_))
    }

    if (badValues.isEmpty) None
    else Some(s"${badValues.size} expected result values had invalid format: ${badValues.mkString(", ")}")
  }

  def apply(value: String): Boolean = {
    new FormatListener().parseResults(value)
  }
}
