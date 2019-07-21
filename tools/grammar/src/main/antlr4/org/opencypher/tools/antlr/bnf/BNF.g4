/*
 [The "BSD licence"]
 Copyright (c) 2013 Tom Everett
 All rights reserved.
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/* sql bnf
 * 	differs from bnf as known to antlr list of grammars in how multiplicity is shown
 *    ...  is +
 *    
 */
 

grammar BNF;

rulelist
    : 
//    header? 
    rule_* EOF
;

//header
//	: HEADER_START
//	  headerline*
//	  HEADER_END 
//	;
//
//headerline : '*' headerContent HEADER_LINEEND; 
//
//headerContent : HEADER_CONTENT ;
//
//HEADER_START : '(*'  [\r]? [\n];
//HEADER_CONTENT  :  ~[\r\n]* ;
//HEADER_END   : '*)'  ~[\r\n]* [\r\n] ;
//HEADER_LINEEND :  [\r]? [\n] ;

rule_
    : 
//    header? 
    lhs ASSIGN rhs
    ;

lhs
    : LT ruleid GT
    ;


rhs
    :  bnfsymbols+ | alternatives
    ;

alternatives
    : alternative (BAR alternative)*
    ;

alternative
    : element*
    ;

element
    : optionalitem
    | requireditem
    | text
    | id
    ;

optionalitem
    : REND alternatives LEND ELLIPSIS?
    ;

requireditem
    : RBRACE alternatives LBRACE ELLIPSIS?
    ;

// conceivable this could have ellipsis, but i doubt they do that
text
    : ID | CHARACTER_LITERAL | INTEGER_LITERAL| UNICODE_LITERAL
    ;

id
    : LT ruleref GT ELLIPSIS?
    ;

ruleref 
	: ID
	;
	
ruleid
    : ID
    ;

// include = to allow sql g4 to include <= >= and - to allow -
bnfsymbols :
	ASSIGN | LBRACE | RBRACE | LEND | REND | BAR | GT | LT | ELLIPSIS | DOUBLE_EXCLAM
	// these aren't really bnf - they are mixed, but it is complicated to cope with those
	| GE | LE
	//   <> will be ok, as it is all bnfsymbols
	// don't need these here because they are in character literal
//	 | EQUALS | EXCLAM 
// these were there but I'm not sure why. not in 9075-1	| '-' | '+'
//	| MINUS | PLUS
	;


ASSIGN
    : '::='
    ;

//LPAREN
//    : ')'
//    ;

//RPAREN
//    : '('
//    ;

LBRACE
    : '}'
    ;

RBRACE
    : '{'
    ;

// who chose these names ?
LEND
    : ']'
    ;

REND
    : '['
    ;

BAR
    : '|'
    ;

GT
    : '>'
    ;

LT
    : '<'
    ;

ELLIPSIS
	: '...'
	;

DOUBLE_EXCLAM : '!!' ;

GE : '>=' ;
LE : '<=' ;

// pseudo bnf to make sql/pg ddl work
//    not sure about these. 2 July 2019
//EQUALS
//	: '='
//	;
//	
//EXCLAM
//	: '!'
//	;
//
//PLUS 
//	: '+'
//	;
//	
//MINUS
//	: '-'
//	;
//	
//PLUS_MINUS : PLUS | MINUS ;

ID
    : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'-'|' '|'_')*
    ;

INTEGER_LITERAL : '0'..'9'+ ;

CHARACTER_LITERAL
	: '(' | ')' | ',' | '&' | '.' | '-' | '*' | ':' | '=' | '/' | '%' | '+' | '!' 
	| '~' | ';' | '?' | '_' | '"' | '\'' | '`' | '@' | '$' | '\\'
// added for gql
	| '^'
// added for cypher via bnf (for now)
	| '..'
	;
	
// modified from http://www.rpatk.net/rpatk/doc/doxygen/rpadoc/html/rpa_bnf.html
// (which had [ ] round it
UNICODE_LITERAL
	: '0x' [0123456789ABCDEFabcdef]+ 
	;

NORMAL_TEXT
	: '!!' ~[\r\n]* -> channel(HIDDEN)
	;

SINGLE_LINE_COMMENT
	: '//' ~[\r\n]* -> channel(HIDDEN)
	;

// whitespace goes to hidden so we can handle block comments (i hope)
WS
    : [ \r\n\t] -> skip
    ;