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
    header? 
    rule_* EOF
;

header : description ;

description : descriptionLine+ ;
//	descriptionLine*
//	DESCRIPTION_END
//	;
//	
descriptionLine : NORMAL_TEXT ; 
//
//DESCRIPTION_START : '(*' ; // [\r]? [\n];
//DESCRIPTION_CONTENT  :  ~[\r\n] ;
//DESCRIPTION_END   : '*)'; //  ~[\r\n]* [\r\n] ;
//HEADER_LINEEND :  [\r]? [\n] ;

rule_
    : 
    description? 
    lhs ASSIGN rhs
    ;

lhs
    : LT ruleid GT
    ;


rhs
    :  bnfsymbols+ | alternatives
    ;

bnfsymbols : bnfsymbol+ ; 

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
    | characterset
    | normaltext
    ;

optionalitem
    : REND alternatives LEND ELLIPSIS?
    ;

requireditem
    : RBRACE alternatives LBRACE ELLIPSIS?
    ;

// this was ID,not WORD, but ID allows space
text
    : UNICODE_LITERAL | ID | CHARACTER_LITERAL | INTEGER_LITERAL
    ;

id
    : LT ruleref GT ELLIPSIS?
    ;

characterset : '$' ( namedcharacterset | exclusioncharacterset | listcharacterset) '$' ;

normaltext : NORMAL_TEXT ;

namedcharacterset : ID ;
exclusioncharacterset : '~'listcharacterset ;
listcharacterset :  '[' text+ ']' ;


ruleref 
	: ID
	;
	
ruleid
    : ID
    ;

bnfsymbol :
	ASSIGN | LBRACE | RBRACE | LEND | REND | BAR | GT | LT | ELLIPSIS | DOUBLE_EXCLAM | DOLLAR
	;


ASSIGN
    : '::='
    ;

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

// now used for charset delimitation
DOLLAR : '$' ;

// allow hyphens and underscore, but not space
//WORD
//    : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'-'|'_')*
//    ;

// and space
ID
    : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'-'|' '|'_')*
    ;

//  "normal english text" is a single line and an alternative
NORMAL_TEXT
	: '!!' ~[\r\n]*  
	;

// comments distinct from "normal text" ?
SINGLE_LINE_COMMENT
	: '//' ~[\r\n]* -> channel(HIDDEN)
	;

INTEGER_LITERAL : '0'..'9'+ ;

// this was
//  CHARACTER_LITERAL : [(),&.\*:=/%+!~;?_"\'`@\\'^-] ;
//  but \* and \' were disallowed at 4.7.2.  Not sure if it meant \ or *.  Assumi
CHARACTER_LITERAL : [(),&.*:=/%+!~;?_"'`@\\^-] ;

//	: '(' | ')' | ',' | '&' | '.' | '-' | '*' | ':' | '=' |  '/' | '%' | '+' | '!' 
//	| '~' | ';' | '?' | '_' | '"' | '\'' | '`' | '@' | '\\' 
// added for gql
//	| '^'
// added for cypher via bnf (for now)
//	| '..'
//	;
	
// modified from http://www.rpatk.net/rpatk/doc/doxygen/rpadoc/html/rpa_bnf.html
// (which had [ ] round it
UNICODE_LITERAL
	: '\\u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT  
	;

fragment HEX_DIGIT :  [0123456789ABCDEFabcdef] ;

// whitespace goes to hidden so we can handle block comments (i hope)
WS
    : [ \r\n\t] -> skip
    ;
    
//fragment MINUS : '-' ;
