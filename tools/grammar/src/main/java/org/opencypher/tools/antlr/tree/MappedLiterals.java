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

import java.util.HashMap;
import java.util.Map;

// utility class to convert between G4 character literals and BNF rules
public class MappedLiterals
{
	private static final Map<String, String> nameToLiteral = new HashMap<>();
	private static final Map<String, String> literalToName = new HashMap<>();
	
	static {
		nameToLiteral.put("percent","%");
		nameToLiteral.put("ampersand","&");
		nameToLiteral.put("left paren","(");
		nameToLiteral.put("right paren",")");
		nameToLiteral.put("asterisk","*");
		nameToLiteral.put("plus sign","+");
		nameToLiteral.put("comma",",");
		nameToLiteral.put("minus sign","-");
		nameToLiteral.put("period",".");
		nameToLiteral.put("solidus","/");
		nameToLiteral.put("colon",":");
		nameToLiteral.put("semicolon",";");
		nameToLiteral.put("less than operator","<");
		nameToLiteral.put("equals operator","=");
		nameToLiteral.put("greater than operator",">");
		nameToLiteral.put("question mark","?");
		nameToLiteral.put("left bracket","[");
		nameToLiteral.put("left bracket trigraph","??(");
		nameToLiteral.put("right bracket","]");
		nameToLiteral.put("right bracket trigraph","??)");
		nameToLiteral.put("vertical bar","|");
		nameToLiteral.put("left brace","{");
		nameToLiteral.put("right brace","}");
		nameToLiteral.put("right arrow", "->");
		nameToLiteral.put("right arrow", "->");
		nameToLiteral.put("double lt", "<<");
		nameToLiteral.put("less than equal", "<=");
		nameToLiteral.put("double gt", ">>");
		nameToLiteral.put("greater or equal", ">=");
		nameToLiteral.put("not equal angles", "<>");
		nameToLiteral.put("exactly equal", "==");
		nameToLiteral.put("not equal exclam", "!=");
		nameToLiteral.put("increment operator", "+=");
		for (Map.Entry<String, String> entry : nameToLiteral.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			literalToName.put(value, key);
		}
	}
	
	public static GrammarItem getFromBNFid(String ruleName) {
		if (nameToLiteral.containsKey(ruleName)) {
			return new MappedLiteral(ruleName, nameToLiteral.get(ruleName));
		} else {
			if ( ruleName.equals(EOFreference.BNF_NAME)) {
				return new EOFreference();
			}
			return new RuleId(ruleName);
		}
	}
	
	public static GrammarItem fromG4rule(String ruleName) {
		// this will be delivered upper-case with underscores
		String ruleNameLower = ruleName.toLowerCase().replaceAll("_", " ");
		if (nameToLiteral.containsKey(ruleNameLower)) {
			// it's something special
			return new MappedLiteral(ruleNameLower, literalToName.get(ruleNameLower));
		} else {
			return new RuleId(ruleName);
		}
	}
	
	public static GrammarItem getFromG4literal(String content) {
		if (literalToName.containsKey(content)) {
			return new MappedLiteral(literalToName.get(content), content);
		} else {
			return new InLiteral(content);
		}
	}
}
