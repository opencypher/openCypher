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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * non-alpha characters that are handled in different ways by the notations
 */
public enum CharLit  {
	
	COMMA(","),
	MINUS_SIGN("-"),
	PLUS_SIGN("+"),
	// supplying an xml form will cause xml serialisation to use <literal ... rather than in line
	DOUBLE_QUOTE("\"",  "&quot;"),
	LESS_THAN_OPERATOR("<",   "&lt;"),
	GREATER_THAN_OPERATOR(">",   "&gt;"),
	NOT_EQUALS_ANGLES("<>",  "&lt;&gt;"),
	GREATER_EQUALS(">=",  "&gt;="),
	LESS_EQUALS("<=",  "&lt;="),
	AMPERSAND("&", "&amp;"),
	EQUALS("="),
	RIGHT_BRACKET("]"),
	LEFT_BRACKET("["),
	VERTICAL_BAR("|"),
	RIGHT_BRACE("}"),
	LEFT_BRACE("{"),
	ELLIPSIS("..."),
	NEGATIVE_ELLIPSIS("!..."),
	SINGLE_QUOTE("'", "'","\\'"),
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
	
	static {
		charMap = new HashMap<>();
		nameMap = new HashMap<>();
		for (CharLit lit : CharLit.values()) {
			charMap.put(lit.actualCharacters, lit);
			nameMap.put(lit.name(), lit);
		}
	}
	
	
	public static CharLit getByValue(String characters) {
		return charMap.get(characters);
	}
	
	public static CharLit getByName(String suppliedName) {
		return nameMap.get(suppliedName.toUpperCase().replaceAll(" ", "_"));
	}
	
	private final String actualCharacters;
	private final String xmlForm;
	private final String bnfForm;
	private final String g4Form;
	private final String xmlName;
	private final String xmlLiteral;


	CharLit(String characters) {
		this(characters, "", "");
	}
	
	CharLit(String characters, String xml) {
		this(characters, xml, "");
	}
	
	CharLit(String characters, String xml, String escapedCharacters) {
		// can override the enum name
		
		this.actualCharacters = characters;
		if (xml != null) {
			this.xmlLiteral = "<literal value=\"" + xml + "\" case-sensitive=\"true\"/>";
			this.xmlForm = xmlLiteral;
		} else {
			this.xmlLiteral = "<literal value=\"" + characters + "\" case-sensitive=\"true\"/>";
			this.xmlForm = characters;
			
		}
		
			// bnfName is lower case with spaces in angles
		this.bnfForm = "<" + name().toLowerCase().replaceAll("_", " ") + ">";
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
