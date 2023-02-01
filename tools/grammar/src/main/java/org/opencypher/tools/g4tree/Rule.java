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
    package org.opencypher.tools.g4tree;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencypher.tools.g4tree.GrammarItem.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Rule implements GrammarItem
{
    public enum RuleType { NORMAL(true), KEYWORD(false), KEYWORD_LITERAL(false), LETTER(false), BNF(false),
            FRAGMENT(false);
        private final boolean keep;
        private RuleType(boolean keep) {
            this.keep = keep;
        }
        public boolean keep() {
            return keep;
        }
    }
    
    private static final int LINE_WIDTH = 85;
    private static final String BNFINDENT = "    ";
    private static final int INDENT_WIDTH = BNFINDENT.length();
    
    private static final int G4LINE_WIDTH = 100;
    private static final String G4INDENT = "         ";
    private static final int G4INDENT_WIDTH = G4INDENT.length();
    
    private final GrammarItem ruleName;
    private final GrammarItem rhs;
    private final boolean keyWordRule;
    private final String description;
    // not final
    private RuleType ruleType;
    
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(Rule.class.getName());
    
    public Rule(GrammarItem ruleName, GrammarItem rhs, String description) {
        this(ruleName, rhs, false, RuleType.NORMAL, description);
    }
    
    public Rule(GrammarItem ruleName, GrammarItem rhs, boolean keyWordRule, RuleType ruleType, String description)
    {
        super();
        this.ruleName = ruleName;
        this.rhs = rhs;
        this.keyWordRule = keyWordRule;
        this.ruleType = ruleType;
        this.description = description;
    }

    
    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    @Override
    public ItemType getType() {
        // this is probably never called
        return ItemType.RULE;
    }
    
    public String getRuleName() {
        return ruleName.toString();
    }

    public String getDescription() {
        return description;
    }

    public GrammarItem getRhs() {
        return rhs;
    }

    @Override
    public List<GrammarItem> getChildren() {
        return Collections.singletonList(rhs);
    }

    @Override
    public boolean isPlural()
    {
        return false;
    }
    
//    private String breakBNFrule(String bnfText) {
//
//        StringBuilder sb = new StringBuilder();
//        // split lines so "normaltext" can be handled
//        String[] lines = bnfText.split("\n");
//        breakBNFline(lines[0], sb, LINE_WIDTH);
//        for (int i= 1; i <lines.length; i++) {
//            if (lines[i].matches("\\s*!!.*")) {
//                sb.append("\n").append(BNFINDENT).append(lines[i]).append("\n").append(BNFINDENT);
//            } else {
//                breakBNFline(lines[i], sb, LINE_WIDTH-INDENT_WIDTH);
//            }
//        }
//;
//        return sb.toString();
//    }
//
//    private void breakBNFline(String bnfText, StringBuilder sb, int firstLineSize)
//    {
//        int lineSize = firstLineSize;
//        int remaining = bnfText.length();
//        int offset = 0;
//        while (remaining > lineSize) {
//            // if in the middle of rulename, go back to the start of it
//            int chunkLength = lineSize;
//            String beyondChunk = bnfText.substring(offset + chunkLength);
//            int firstBra = beyondChunk.indexOf('<');
//            int firstKet =beyondChunk.indexOf('>');
//            String candidate = bnfText.substring(offset, offset + chunkLength);
//            if (firstKet >= 0 && ( firstBra < 0 || firstKet < firstBra)) {
//                chunkLength = candidate.lastIndexOf('<') - 1;
//            } else {
//                // split at last space
//                int lastSpace = candidate.lastIndexOf(' ');
//                // make sure that hasn't gone back into a rule name
//                if (bnfText.substring(offset + lastSpace, offset + chunkLength).indexOf('>') >= 0) {
//                    chunkLength = candidate.lastIndexOf("<") - 1;
//                } else {
//                    chunkLength = lastSpace;
//                }
//            }
//            sb.append(bnfText.substring(offset, offset + chunkLength)).append("\n").append(BNFINDENT);
//            lineSize = LINE_WIDTH - INDENT_WIDTH;
//            offset += chunkLength;
//            remaining -= chunkLength;
//        }
//        sb.append(bnfText.substring(offset));
//    }
//
//    private String breakG4rhs(String ruleName, String g4Text)
//    {
//        StringBuilder sb = new StringBuilder(ruleName);
//        if (ruleName.equals(ruleName.toUpperCase()))
//        {
//            // lexer/fragment - just wrap with : ;
//            sb.append(" : ").append(g4Text).append(";");
//        } else
//        {
//            sb.append("\n").append(G4INDENT).append(": ");
//            int lineSize = G4LINE_WIDTH - G4INDENT_WIDTH;
//            // cope with normal text lines
//            String [] lines = g4Text.split("\n");
//            for (String line : lines)
//            {
//                if (line.matches("\\s*//.*")) {
//                    sb.append("\n").append(G4INDENT).append(line).append("\n").append(G4INDENT);
//                } else {
//                    breakG4line(line, sb, lineSize);
//                }
//            }
//            sb.append(";");
//        }
//        return sb.toString();
//    }
//
//
//    private void breakG4line(String originalLine, StringBuilder sb, int lineSize)
//    {
//        int remaining = originalLine.length();
//        int offset = 0;
//        while (remaining > lineSize)
//        {
//            int chunkLength = lineSize;
//
//            // split at last space
//            chunkLength = originalLine.substring(offset, offset + chunkLength).lastIndexOf(' ');
//
//            sb.append(originalLine.substring(offset, offset + chunkLength)).append("\n").append(G4INDENT);
//            offset += chunkLength;
//            remaining -= chunkLength;
//        }
//        sb.append(originalLine.substring(offset));
//    }
    
    @Override
    public String getStructure(String indent)
    {
        return indent + "Rule (" + ruleType + ") :" + ruleName + "\n" + rhs.getStructure(indent + INDENT) ;
    }


    @Override
    public GrammarItem reachThrough()
    {
        return this;
    }


    @Override
    public boolean isKeywordPart() {
        return false;
    }
}
