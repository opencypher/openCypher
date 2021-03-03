/* this is a very incomplete G4 grammar,
 * only coping with the constructs used 
 * in the SQL/PG ddl
 * 
 * Patterned on the BNF grammar
 * 
 * Peter Furniss, Neo4j, 11 October 2018
 */
 
grammar Gee4;

wholegrammar
	: header?  grammardef rulelist
	;

header : QUASI_COMMENT;

grammardef 
	: 'grammar' IDENTIFIER ';' ;
	
rulelist
	: (rule_ | specialRule | fragmentRule )* EOF
	;

	
rule_
	: description? ruleName ':' ruleElements ';' ;

description : QUASI_COMMENT;

ruleName
	: IDENTIFIER
	;
	
ruleElements 
	: ruleAlternative ( OR ruleAlternative)* ;
	
ruleAlternative
	: ruleItem*
	;
	
ruleItem
	: ( ruleComponent | '(' ruleElements ')' ) cardinality?
	;
	
ruleComponent
	: ruleReference | literal ;
	
cardinality 
	: QUESTION | PLUS | STAR ;
		
// reworked to split in parser
literal : quotedString | negatedQuotedString | charSet  | negatedCharSet | dotPattern ;

quotedString : QUOTED_STRING ;
negatedQuotedString : NEGATED_STRING ;
charSet : CHAR_SET;
negatedCharSet : NEGATED_CHAR_SET;
dotPattern : DOT_PATTERN;

QUOTED_STRING : '\'' ( '\\' . | ~'\'' )*? '\'' ;
NEGATED_STRING : '~' QUOTED_STRING ;
CHAR_SET : '[' ( ~']' )*? ']' ;
//  including ] in a charset is really difficult. this will swallow the end ] if \ the last character in the set
//CHAR_SET : '[' ( ~']' | '\\' ']')* ']' ;
NEGATED_CHAR_SET : '~' CHAR_SET ;
DOT_PATTERN : ( '.' '*'? '?'? ) ;

ruleReference
	: IDENTIFIER
	;
	
IDENTIFIER : [a-zA-Z] [a-zA-Z_0-9]* ;

OR : '|';
QUESTION : '?' ;
PLUS  : '+' ;
STAR : '*' ;

specialRule 
	: ruleName ':' (( IDENTIFIER  ACTION) | ( ruleElements '->' IDENTIFIER ( '(' IDENTIFIER ')' )?)) ';'
	;
	
fragmentRule
	: 'fragment' ruleName ':' literal ';' ;

ACTION :
	'{'  ~'}'* '}'
	;

// by making the header and description like javadocs, the are distinguished by the lexer
// with all the linefeeds included
QUASI_COMMENT
	: '/**' .*?  '*/' 
	;
	
// can we consider sql bnf "normal text" ?
NORMAL_LINE_COMMENT
	: '//!!' ~[\r\n]* -> channel(HIDDEN)
	;
	
SINGLE_LINE_COMMENT
	: '//' ~[\r\n]* -> skip
	;

MULTILINE_COMMENT
	: '/*' .*? ( '*/' | EOF ) -> skip
	;

WS
    : [ \r\n\t] -> skip
    ;
