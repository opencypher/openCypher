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
CHAR_SET : '[' ( ~']' | '\\]')* ']' ;
NEGATED_CHAR_SET : '~' CHAR_SET ;
DOT_PATTERN : ( '.' '*'? '?'? ) ;

//LITERAL is a possibly negated quoted string, or a possibly negated character set or dot
//   changed from original
	// had a lot of trouble with this - and I can't remember where i got this whole grammar from
	// it doesn't seem to be the usual antlr4 github page https://github.com/antlr/grammars-v4
	// and can't character sets only be frags
	// three alternative s
	//     possibly negated quoted string that may contain escaped characters or no quotes until the ending quote
	//     possibly negated character set enclosedin [] , escaping ] if need be
	//     dot with cardinality or optional markers
	//   that charset definition isn't enough - see https://github.com/antlr/antlr4/blob/master/doc/lexer-rules.md
LITERAL
	: ('~'? '\'' ( '\\' . | ~'\'' )*? '\'') | ( '~'? '[' ( ~']' | '\\]')* ']'  ) | ( '.' '*'? '?'? );
	
	
//  re-present that assuming we could use " for quoting and \ isn't escaping
//	: ('~'? "'" ( ~"'" | "\'" )*? "'")          | ( '~'? '[' ( ~']' | '\\]')* ']'  ) | ( '.' '*'? '?'? );
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
