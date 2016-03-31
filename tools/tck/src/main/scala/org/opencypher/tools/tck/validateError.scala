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

/**
  * This function will validate a TCK error specification, which consists of three parts: type, phase and detail. Each
  * of these parts needs to be one of a pre-defined set of constants in order to be valid. If the triple is invalid, a
  * message will be returned explaining the invalid part(s), otherwise None will be returned.
  */
object validateError extends ((String, String, String) => Option[String]) {

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

  // TODO: Move these to become public constants
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
