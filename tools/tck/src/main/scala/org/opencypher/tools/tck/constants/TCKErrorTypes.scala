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
package org.opencypher.tools.tck.constants

object TCKErrorTypes {

  val SYNTAX_ERROR = "SyntaxError"
  val SEMANTIC_ERROR = "SemanticError"
  val PARAMETER_MISSING = "ParameterMissing"
  val CONSTRAINT_VERIFICATION_FAILED = "ConstraintVerificationFailed"
  val CONSTRAINT_VALIDATION_FAILED = "ConstraintValidationFailed"
  val ENTITY_NOT_FOUND = "EntityNotFound"
  val PROPERTY_NOT_FOUND = "PropertyNotFound"
  val LABEL_NOT_FOUND = "LabelNotFound"
  val TYPE_ERROR = "TypeError"
  val ARGUMENT_ERROR = "ArgumentError"
  val ARITHMETIC_ERROR = "ArithmeticError"

  val ALL = Set(SYNTAX_ERROR, SEMANTIC_ERROR, PARAMETER_MISSING,
                CONSTRAINT_VERIFICATION_FAILED, CONSTRAINT_VALIDATION_FAILED,
                ENTITY_NOT_FOUND, PROPERTY_NOT_FOUND, LABEL_NOT_FOUND, TYPE_ERROR,
                ARGUMENT_ERROR, ARITHMETIC_ERROR)
}

object TCKErrorPhases {
  val COMPILE_TIME = "compile time"
  val RUNTIME = "runtime"
}

object TCKErrorDetails {

  val INVALID_ELEMENT_ACCESS = "InvalidElementAccess"
  val MAP_ELEMENT_ACCESS_BY_NON_STRING = "MapElementAccessByNonString"
  val LIST_ELEMENT_ACCESS_BY_NON_INTEGER = "ListElementAccessByNonInteger"
  val CREATE_BLOCKED_BY_CONSTRAINT = "CreateBlockedByConstraint"

  val ALL = Set(INVALID_ELEMENT_ACCESS,
                MAP_ELEMENT_ACCESS_BY_NON_STRING,
                LIST_ELEMENT_ACCESS_BY_NON_INTEGER,
                CREATE_BLOCKED_BY_CONSTRAINT)

}
