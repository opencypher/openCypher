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
package org.opencypher.tools.antlr.tree;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencypher.tools.antlr.tree.GrammarItem.ItemType;

public enum CharacterLiteral implements GrammarItem {
	COMMA(","),
	MINUS_SIGN("-"),
	DOUBLE_QUOTE("\"",  "&quot;"),
	LESS_THAN_OPERATOR("<",   "&lt;"),
	GREATER_THAN_OPERATOR(">",   "&gt;"),
	NOT_EQUALS_ANGLES("<>",  "&lt;&gt;"),
	GREATER_EQUALS(">=",  "&gt;="),
	LESS_EQUALS("<=",  "&lt;="),
	
	RIGHT_BRACKET("]"),
	LEFT_BRACKET("["),
	VERTICAL_BAR("|"),
	RIGHT_BRACE("}"),
	LEFT_BRACE("{"),
	ELLIPSIS("..."),
	NEGATIVE_ELLIPSIS("!..."),
	SINGLE_QUOTE("'", "'","","\\'");
	
	
	private final String actualCharacters;
	private final String xmlName;
	private final String bnfName;
	private final String g4Name;

	CharacterLiteral(String content) {
		this(content, content, "", content);
	}
	
	CharacterLiteral(String content, String xml) {
		this(content, xml, "", content);
	}
	
	CharacterLiteral(String content, String xml, String givenName, String escapedCharacters) {
		// can override the enum name
		String baseName = givenName.length() > 0 ? givenName : name();
		this.actualCharacters = content;
		// bnfName is lower case with spaces
		bnfName = baseName.toLowerCase().replaceAll("_", " ");
		// g4name is as is (uppercase with _) - this is a lexer rule
		g4Name = baseName;
		// xmlname is camel with spaces
		Matcher underMatch = Pattern.compile("(\\w)(\\w+)(_?)").matcher(baseName);
		StringBuilder b = new StringBuilder();
		while (underMatch.find()) {
			b.append(underMatch.group(1).toUpperCase());
			if (underMatch.group(2) != null) {
				b.append(underMatch.group(2).toLowerCase());
			}
			if (underMatch.group(3) != null) {
				b.append(" ");
			}
		}
		xmlName = b.toString();
	}
		

	@Override
	public ItemType getType() {
		return ItemType.LITERAL;
	}
//	public static String getXmlLiteral(String content) {
//			if (specialLiterals.containsKey(content)) {
//				return "<literal value=\"" + specialLiterals.get(content) + "\" case-sensitive=\"true\"/>";
//			}
//			if (TransformationControl.useXmlOpenLiteral() && content.matches("[A-Z ]+")) {
//				return content + " ";
//			}
//			return "<literal value=\"" + content + "\" case-sensitive=\"false\"/>";
//
//		}
//	}


	@Override
	public String getStructure(String indent) {
		return "CharacterLiteral : " + actualCharacters + " (" + g4Name + ")";
	}


	@Override
	public boolean isPlural() {
		return false;
	}

	@Override
	public GrammarItem reachThrough() {
		return this;
	}

	@Override
	public boolean isKeywordPart() {
		return false;
	}

	@Override
	public List<GrammarItem> getChildren() {
		return Collections.emptyList();
	}

}
