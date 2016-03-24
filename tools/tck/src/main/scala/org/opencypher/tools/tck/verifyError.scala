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

object verifyError extends ((String, String, String) => Option[String]) {

  override def apply(typ: String, phase: String, detail: String): Option[String] = {
    val msg = s"""${checkType(typ)}
                 |${checkPhase(phase)}
                 |${checkDetail(detail)}""".stripMargin

    if (msg.trim.isEmpty) None
    else Some(msg)
  }

  def checkType(typ: String): String = {
    if (TYPES(typ)) ""
    else s"Invalid error type: $typ"
  }

  def checkPhase(phase: String): String = {
    if (phase == "runtime" || phase == "compile time") ""
    else s"Invalid error phase: $phase"
  }

  def checkDetail(detail: String): String = {
    if (DETAILS(detail)) ""
    else s"Invalid error detail: $detail"
  }

  private val TYPES = Set("SyntaxError",
                          "SemanticError",
                          "ParameterMissing",
                          "ConstraintVerificationFailed",
                          "EntityNotFound",
                          "PropertyNotFound",
                          "LabelNotFound",
                          "TypeError",
                          "ArgumentError",
                          "ArithmeticError")

  private val DETAILS = Set("InvalidElementAccess",
                            "MapElementAccessByNonString",
                            "ListElementAccessByNonInteger",
                            "ConstraintVerificationFailed",
                            "EntityNotFound",
                            "PropertyNotFound",
                            "LabelNotFound",
                            "TypeError",
                            "ArgumentError",
                            "ArithmeticError")

}
