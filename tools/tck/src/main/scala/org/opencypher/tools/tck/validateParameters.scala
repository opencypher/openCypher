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

/**
  * This function will validate that a given DataTable from a TCK scenario contains parseable parameter representations.
  * If there are invalid parameter values in the table, a message describing them will be returned, otherwise None is
  * returned.
  */
object validateParameters extends (DataTable => Option[String]) {

  override def apply(table: DataTable): Option[String] = {
    // TODO: Specify constraints for parameter keys, and enforce these here
    // val keys = table.transpose().topCells().asScala
    val values = table.transpose().cells(1).asScala.head.asScala

    val badValues = values.filterNot(this (_))
    if (badValues.isEmpty) None
    else Some(s"${badValues.size} parameters had invalid format: ${badValues.mkString(", ")}")
  }

  def apply(value: String): Boolean = {
    new FormatListener().parseParameter(value)
  }
}
