/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
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
  val PROCEDURE_ERROR = "ProcedureError"
  // wildcard error type
  val ERROR = "Error"

  val ALL = Set(SYNTAX_ERROR, SEMANTIC_ERROR, PARAMETER_MISSING,
                CONSTRAINT_VERIFICATION_FAILED, CONSTRAINT_VALIDATION_FAILED,
                ENTITY_NOT_FOUND, PROPERTY_NOT_FOUND, LABEL_NOT_FOUND, TYPE_ERROR,
                ARGUMENT_ERROR, ARITHMETIC_ERROR, PROCEDURE_ERROR, ERROR)
}

object TCKErrorPhases {
  val COMPILE_TIME = "compile time"
  val RUNTIME = "runtime"
  // wildcard error phase
  val ANY_TIME = "any time"

  val ALL = Set(COMPILE_TIME, RUNTIME, ANY_TIME)
}

object TCKErrorDetails {

  val INVALID_ELEMENT_ACCESS = "InvalidElementAccess"
  val MAP_ELEMENT_ACCESS_BY_NON_STRING = "MapElementAccessByNonString"
  val LIST_ELEMENT_ACCESS_BY_NON_INTEGER = "ListElementAccessByNonInteger"
  val NESTED_AGGREGATION = "NestedAggregation"
  val NEGATIVE_INTEGER_ARGUMENT = "NegativeIntegerArgument"
  val DELETE_CONNECTED_NODE = "DeleteConnectedNode"
  val REQUIRES_DIRECTED_RELATIONSHIP = "RequiresDirectedRelationship"
  val INVALID_RELATIONSHIP_PATTERN = "InvalidRelationshipPattern"
  val VARIABLE_ALREADY_BOUND = "VariableAlreadyBound"
  val INVALID_ARGUMENT_TYPE = "InvalidArgumentType"
  val INVALID_ARGUMENT_VALUE = "InvalidArgumentValue"
  val NUMBER_OUT_OF_RANGE = "NumberOutOfRange"
  val UNDEFINED_VARIABLE = "UndefinedVariable"
  val VARIABLE_TYPE_CONFLICT = "VariableTypeConflict"
  val RELATIONSHIP_UNIQUENESS_VIOLATION = "RelationshipUniquenessViolation"
  val CREATING_VAR_LENGTH = "CreatingVarLength"
  val INVALID_PARAMETER_USE = "InvalidParameterUse"
  val INVALID_CLAUSE_COMPOSITION = "InvalidClauseComposition"
  val FLOATING_POINT_OVERFLOW = "FloatingPointOverflow"
  val PROPERTY_ACCESS_ON_NON_MAP = "PropertyAccessOnNonMap"
  val INVALID_ARGUMENT_EXPRESSION = "InvalidArgumentExpression"
  val INVALID_UNICODE_CHARACTER = "InvalidUnicodeCharacter"
  val NON_CONSTANT_EXPRESSION = "NonConstantExpression"
  val NO_SINGLE_RELATIONSHIP_TYPE = "NoSingleRelationshipType"
  val INVALID_AGGREGATION = "InvalidAggregation"
  val UNKNOWN_FUNCTION = "UnknownFunction"
  val INVALID_NUMBER_LITERAL = "InvalidNumberLiteral"
  val INVALID_UNICODE_LITERAL = "InvalidUnicodeLiteral"
  val MERGE_READ_OWN_WRITES = "MergeReadOwnWrites"
  val NO_EXPRESSION_ALIAS = "NoExpressionAlias"
  val DIFFERENT_COLUMNS_IN_UNION = "DifferentColumnsInUnion"
  val INVALID_DELETE = "InvalidDelete"
  val INVALID_PROPERTY_TYPE = "InvalidPropertyType"
  val COLUMN_NAME_CONFLICT = "ColumnNameConflict"
  val NO_VARIABLES_IN_SCOPE = "NoVariablesInScope"
  val DELETED_ENTITY_ACCESS = "DeletedEntityAccess"
  val INVALID_ARGUMENT_PASSING_MODE = "InvalidArgumentPassingMode"
  val INVALID_NUMBER_OF_ARGUMENTS = "InvalidNumberOfArguments"
  val MISSING_PARAMETER = "MissingParameter"
  val PROCEDURE_NOT_FOUND = "ProcedureNotFound"
  val UNEXPECTED_SYNTAX = "UnexpectedSyntax"
  val INTEGER_OVERFLOW = "IntegerOverflow"
  val AMBIGUOUS_AGGREGATION_EXPRESSION = "AmbiguousAggregationExpression"
  // wildcard error detail
  val ANY = "*"

  val ALL = Set(INVALID_ELEMENT_ACCESS,
                MAP_ELEMENT_ACCESS_BY_NON_STRING,
                LIST_ELEMENT_ACCESS_BY_NON_INTEGER,
                NESTED_AGGREGATION,
                NEGATIVE_INTEGER_ARGUMENT,
                REQUIRES_DIRECTED_RELATIONSHIP,
                DELETE_CONNECTED_NODE,
                INVALID_RELATIONSHIP_PATTERN,
                VARIABLE_ALREADY_BOUND,
                INVALID_ARGUMENT_TYPE,
                INVALID_ARGUMENT_VALUE,
                NUMBER_OUT_OF_RANGE,
                UNDEFINED_VARIABLE,
                VARIABLE_TYPE_CONFLICT,
                RELATIONSHIP_UNIQUENESS_VIOLATION,
                CREATING_VAR_LENGTH,
                INVALID_PARAMETER_USE,
                INVALID_CLAUSE_COMPOSITION,
                FLOATING_POINT_OVERFLOW,
                PROPERTY_ACCESS_ON_NON_MAP,
                INVALID_ARGUMENT_EXPRESSION,
                INVALID_UNICODE_CHARACTER,
                NON_CONSTANT_EXPRESSION,
                NO_SINGLE_RELATIONSHIP_TYPE,
                INVALID_AGGREGATION,
                UNKNOWN_FUNCTION,
                INVALID_NUMBER_LITERAL,
                INVALID_UNICODE_LITERAL,
                MERGE_READ_OWN_WRITES,
                NO_EXPRESSION_ALIAS,
                DIFFERENT_COLUMNS_IN_UNION,
                INVALID_DELETE,
                INVALID_PROPERTY_TYPE,
                COLUMN_NAME_CONFLICT,
                NO_VARIABLES_IN_SCOPE,
                INVALID_ARGUMENT_PASSING_MODE,
                INVALID_NUMBER_OF_ARGUMENTS,
                MISSING_PARAMETER,
                PROCEDURE_NOT_FOUND,
                DELETED_ENTITY_ACCESS,
                UNEXPECTED_SYNTAX,
                INTEGER_OVERFLOW,
                AMBIGUOUS_AGGREGATION_EXPRESSION,
                ANY)
}
