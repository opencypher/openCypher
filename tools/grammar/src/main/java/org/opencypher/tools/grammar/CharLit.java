/*
 * Copyright (c) 2015-2019 "Neo Technology,"
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
package org.opencypher.tools.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * non-alpha characters that are handled in different ways by the notations
 */
public enum CharLit  {
	
//	ASSIGN | LBRACE | RBRACE | LEND | REND | BAR | GT | LT | ELLIPSIS 

	ASSIGN("::=", true),
	COMMA(","),
	MINUS_SIGN("-"),
	PLUS_SIGN("+"),
	DOUBLE_EXCLAMATION("!!", "", true, "", false),
	// supplying an xml form will cause xml serialisation to use <literal ... rather than in line
	DOUBLE_QUOTE("\"",  "&quot;"),
	LESS_THAN_OPERATOR("<",   "&lt;", true, "", false),
	GREATER_THAN_OPERATOR(">",   "&gt;", true, "", false),
	NOT_EQUALS_ANGLES("<>",  "&lt;&gt;"),
	// these two aren't really bnf, but provided we don't get other mixe items, all is good
	GREATER_EQUALS(">=",  "&gt;=", true, "", true),
	LESS_EQUALS("<=",  "&lt;=", true, "", true),
	AMPERSAND("&", "&amp;"),
	EQUALS("="),
	RIGHT_BRACKET("]","", true, "\\]", false),
	LEFT_BRACKET("[","", true, "\\[", false),
	VERTICAL_BAR("|","", true, "\\|", false),
	RIGHT_BRACE("}", "", true, "\\}", false),
	LEFT_BRACE("{", "", true, "\\{", false),
	ELLIPSIS("...", true),
	NEGATIVE_ELLIPSIS("!..."),
	SINGLE_QUOTE("'", "'",false, "\\'", false),
	SEMI_COLON(";"),
	INCREMENT_OPERATOR("+="),
	ASTERISK("*"),
	LEFT_PAREN("("),
	RIGHT_PAREN(")"),
	COLON(":"),
	DOUBLE_DOT(".."),
	SOLIDUS("/"),
	REVERSE_SOLIDUS("\\"),
	PER_CENT("%"),
	CARET("^"),
	PERIOD("."),
	DOLLAR_SIGN("$"),
	BACK_TICK("`"),
	COMMENT_START("/*"),  // these will need thinking about
	COMMENT_END("*/"),
	DOUBLE_SOLIDUS("//")
	;
	
	private static final Map<String, CharLit> charMap;
	private static final Map<String, CharLit> nameMap;
	private static final Set<String> punctuation;
	private static final Pattern bnfPattern;
	private static final Map<String, CharLit> escapedMap;
	
	static {
		charMap = new HashMap<>();
		nameMap = new HashMap<>();
		escapedMap = new HashMap<>();
		punctuation = new HashSet<>();
		List<String> bnfSyms = new ArrayList<>();
		for (CharLit lit : CharLit.values()) {
			charMap.put(lit.actualCharacters, lit);
			nameMap.put(lit.name(), lit);
			escapedMap.put(lit.escaped,  lit);
			for (String character : lit.actualCharacters.split("")) {
				punctuation.add(character);
			}
			if (lit.isBnfSymbols()) {
				bnfSyms.add(lit.escaped);
			}
		}
		bnfPattern = Pattern.compile(bnfSyms.stream().collect(Collectors.joining("|")));
	}
	
	

	private final String actualCharacters;
	private final boolean bnfSymbols;
	private final String xmlForm;
	private final String bnfForm;
	private final boolean mixed;
	private final String g4Form;
	private final String xmlName;
	private final String xmlLiteral;
	private final String  escaped;
	private final String bnfName;
	

	CharLit(String characters) {
		this(characters, "", false, "", false);
	}

	CharLit(String characters, boolean bnfSymbol) {
		this(characters, "", bnfSymbol, "", false);
	}
	CharLit(String characters, String xml) {
		this(characters, xml, false, "", false);
	}
	
	CharLit(String characters, String xml, boolean bnfSymbol, String escapedCharacters, boolean mixed) {
		// can override the enum name
		
		this.actualCharacters = characters;
		this.escaped =  (escapedCharacters.length() > 0) ? escapedCharacters : characters;
			
		
		if (xml != null) {
			this.xmlLiteral = "<literal value=\"" + xml + "\" case-sensitive=\"true\"/>";
			this.xmlForm = xmlLiteral;
		} else {
			this.xmlLiteral = "<literal value=\"" + characters + "\" case-sensitive=\"true\"/>";
			this.xmlForm = characters;
			
		}
		this.bnfSymbols = bnfSymbol;
			// bnfName is lower case with spaces in angles
		this.bnfName = name().toLowerCase().replaceAll("_", " ");
		this.bnfForm = "<" + bnfName + ">";
		this.mixed = mixed;
		
		// g4name is as is (uppercase with _) - this is a lexer rule
		g4Form = name();

		// xmlname is camel with spaces removed (possibly not used)
		Matcher underMatch = Pattern.compile("(\\w)(\\w+)(_?)").matcher(name());
		StringBuilder b = new StringBuilder();
		while (underMatch.find()) {
			b.append(underMatch.group(1).toUpperCase());
			if (underMatch.group(2) != null) {
				b.append(underMatch.group(2).toLowerCase());
			}
		}
		this.xmlName = b.toString();
	}

	public static CharLit getByValue(String characters) {
		return charMap.get(characters);
	}
	
	public static CharLit getByName(String suppliedName) {
		return nameMap.get(suppliedName.toUpperCase().replaceAll(" ", "_"));
	}
	
	public static boolean allPunctuation(String subject) 
	{
		return Arrays.asList(subject.split("")).stream().allMatch(c -> punctuation.contains(c));
	}
	
	public static boolean allBnfSymbols(String subject) 
	{
		Matcher m = bnfPattern.matcher(subject);
		int start = 0;
		while (m.find()) {
			if (m.start() != start) {
				return false;
			}
			start = m.end();
		}
		return start == subject.length();
	}

	// TODO   haven't completed the case for partial bnf - where a production wants some thing
	// like +>
	
	private static Map<String, CharLit> initCharMap() {
		Map<String, CharLit> temp = new HashMap<>();
		for (CharLit lit : CharLit.values()) {
			temp.put(lit.actualCharacters, lit);
		}
		return temp;
	}
		
	public String getCharacters() {
		return actualCharacters;
	}
	
	public boolean isBnfSymbols() {
		return bnfSymbols;
	}

	/**
	 * @return true if a mixture of bnf and non-bnf characters
	 */
	public boolean isMixed() {
		return mixed;
	}

	public String getBnfName() {
		return bnfName;
	}

	public String getSQLBNF() {
		return bnfForm ;
	}

	public String getISO14977BNF() {
		return bnfForm ;
	}

	public String getG4() {
		return g4Form;
	}

	public String getXML() {
		// may be literal element, may be inline
		return xmlForm;
	}

	public String getXMLliteral() {
		// always the literal element
		return xmlLiteral;
	}

}
