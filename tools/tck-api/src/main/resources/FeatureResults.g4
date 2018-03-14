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
grammar FeatureResults;

value : node
      | relationship
      | path
      | integer
      | floatingPoint
      | string
      | bool
      | nullValue
      | list
      | map
      ;

node : nodeDesc ;

nodeDesc : '(' (label)* WS? (propertyMap)? ')' ;

relationship : relationshipDesc ;

relationshipDesc : '[' relationshipType WS? (propertyMap)? ']' ;

path : '<' pathBody '>' ;

pathBody : nodeDesc (pathLink)* ;

pathLink : (forwardsRelationship | backwardsRelationship) nodeDesc ;

forwardsRelationship : '-' relationshipDesc '->' ;

backwardsRelationship : '<-' relationshipDesc '-' ;

integer : INTEGER_LITERAL ;

floatingPoint : FLOAT_LITERAL
              | INFINITY ;

bool : 'true'
     | 'false'
     ;

nullValue : 'null' ;

list : '[' (listContents)? ']' ;

listContents : listElement (', ' listElement)* ;

listElement : value ;

map : propertyMap ;

propertyMap : '{' (mapContents)? '}' ;

mapContents : keyValuePair (', ' keyValuePair)* ;

keyValuePair: propertyKey ':' WS? propertyValue ;

propertyKey : SYMBOLIC_NAME ;

propertyValue : value ;

relationshipType : ':' relationshipTypeName ;

relationshipTypeName : SYMBOLIC_NAME ;

label : ':' labelName ;

labelName : SYMBOLIC_NAME ;

INTEGER_LITERAL : ('-')? DECIMAL_LITERAL ;

DECIMAL_LITERAL : '0'
                | NONZERODIGIT DIGIT*
                ;

DIGIT : '0'
      | NONZERODIGIT
      ;

NONZERODIGIT : [1-9] ;

INFINITY : '-'? 'Inf' ;

FLOAT_LITERAL : '-'? FLOAT_REPR ;

FLOAT_REPR : DIGIT+ '.' DIGIT+ EXPONENTPART?
           | '.' DIGIT+ EXPONENTPART?
           | DIGIT EXPONENTPART
           | DIGIT+ EXPONENTPART?
           ;

EXPONENTPART :  ('E' | 'e') ('+' | '-')? DIGIT+ ;

SYMBOLIC_NAME : IDENTIFIER ;

WS : ' ' ;

IDENTIFIER : [a-zA-Z0-9$_]+ ;

// The string rule should ideally not include the apostrophes in the parsed value,
// but a lexer rule may not match the empty string, so I haven't found a way
// to define that quite well yet.

string : STRING_LITERAL ;

STRING_LITERAL : '\'' STRING_BODY* '\'' ;

STRING_BODY : '\u0000' .. '\u0026' // \u0027 is the string delimiter character (')
            | '\u0028' .. '\u01FF'
            | ESCAPED_APOSTROPHE
            ;

ESCAPED_APOSTROPHE : '\\\'' ;
