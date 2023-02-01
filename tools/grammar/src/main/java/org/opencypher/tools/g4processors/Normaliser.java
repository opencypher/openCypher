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
  package org.opencypher.tools.g4processors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opencypher.tools.g4tree.BnfSymbols;
import org.opencypher.tools.g4tree.GrammarItem;
import org.opencypher.tools.g4tree.GrammarTop;
import org.opencypher.tools.g4tree.InAlternative;
import org.opencypher.tools.g4tree.InAlternatives;
import org.opencypher.tools.g4tree.InLiteral;
import org.opencypher.tools.g4tree.Rule;
import org.opencypher.tools.g4tree.RuleId;
import org.opencypher.tools.grammar.SQLBNF;
import org.opencypher.tools.g4tree.GrammarItem.ItemType;
import org.opencypher.tools.g4tree.Rule.RuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * modify a grammar tree to be what it would be for xml.
 * Can't be done during creation of each rule as it has to look ahead to other rules
 * Could be done at end of listening
 */
public class Normaliser {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Normaliser.class.getName());

    public Map<String, Rule> normalise(GrammarTop top) {
//        LOGGER.warn("Normalising \n{}", top.getStructure(""));
        List<Rule> rules = top.getRuleList().getRules();
        markLetterRules(rules);
        markBnfRules(rules);
        // build a map of the rules by name
        Map<String, Rule> ruleMap = new HashMap<>();
        for (Rule rule : rules) {
            if (ruleMap.put(rule.getRuleName(), rule) != null) {
                LOGGER.warn("duplicate rule {}", rule.getRuleName());
            }
        }
//        LOGGER.warn("as received :{}", top.getStructure(""));
//        Map<String, Rule> ruleMap = rules.stream().collect(Collectors.toMap(r -> r.getRuleName(), r -> r));
        markKeywordLiteralRulesSeparateAlphabet(rules, ruleMap);
        markKeywordRules(rules, ruleMap);
        markKeywordRulesAlternativesOfCase(rules, ruleMap);
//        LOGGER.warn("normalised : \n{}", ruleMap.values().stream().map(r -> r.getStructure("")).collect(Collectors.joining("\n")));
        return ruleMap;
    }

    private void markKeywordRules(List<Rule> rules, Map<String, Rule> ruleMap) {
        // and then the second generation
//          Rule (NORMAL) :stop
//             Alternatives (1)
//                   Alternative ():
//                         RuleReference : STOP                         
        for (Rule rule : rules) {
            // they have multi-letter lower-case names
            String ruleName = rule.getRuleName();
            if (ruleName.matches("[a-z]+")) {
                GrammarItem rhs = rule.getRhs();
                if (rhs.getType() == ItemType.ALTERNATIVES) {
                    // one alternative child
                    List<GrammarItem> alts = rhs.getChildren();
                    if (alts.size() == 1) {
                        List<GrammarItem> refs = alts.get(0).getChildren();
                        // just one reference
                        if (refs.size() == 1) {
                            // is a reference to a keyword literal with the upper case name
                            GrammarItem ref = refs.get(0);
                            if (ref.getType() == ItemType.REFERENCE) {
                                String refName = ((RuleId) ref).getName();
                                if (refName.equals(ruleName.toUpperCase())
                                        && ruleMap.containsKey(refName)
                                        && ruleMap.get(refName).getRuleType() == RuleType.KEYWORD_LITERAL) {
                                       LOGGER.debug("Rule {} is a keyword rule", ruleName);
                                    rule.setRuleType(RuleType.KEYWORD);
                                }
                             }
                        }
                    }
                }
            }
        }

    }

    private void markKeywordLiteralRulesSeparateAlphabet(List<Rule> rules, Map<String, Rule> ruleMap) {
        // now find the first generation keyword rules - this is assuming we have letter fragments to
        // handle the case insensitivity
        // they have upper-case names, with references to the letters
//          Rule (NORMAL) :STOP
//             Alternatives (*)
//                   Alternative (*):
//                         RuleReference : S
//                         RuleReference : T
//                         RuleReference : O
//                         RuleReference : P
        for (Rule rule : rules) {
            // they have multi-letter upper-case names
            String ruleName = rule.getRuleName();
            if (ruleName.matches("[A-Z]+")) {
                GrammarItem rhs = rule.getRhs();
                if (rhs.getType() == ItemType.ALTERNATIVES) {
                    // one alternative child
                    List<GrammarItem> alts = rhs.getChildren();
                    if (alts.size() == 1) {
                        List<GrammarItem> refs = alts.get(0).getChildren();
                        // same length as name
                        if (refs.size() == ruleName.length()) {
                            // and they are all references to letters
                            if (refs.stream().allMatch(r ->
                                              r.getType() == ItemType.REFERENCE
                                              &&  ruleNameIsInsensitiveLetter(((RuleId) r).getName()))) {
                                List<String> letters = refs.stream().map(r -> ((RuleId) r).getName()).collect(Collectors.toList());
                                if (letters.stream().allMatch(l -> ruleMap.containsKey(l)
                                         && ruleMap.get(l).getRuleType() == RuleType.LETTER)
                                        && ruleName.equals(letters.stream().collect(Collectors.joining("")))) {
                                       LOGGER.debug("Rule {} is a keyword literal rule", ruleName);
                                    rule.setRuleType(RuleType.KEYWORD_LITERAL);
                                }
                             }
                        }
                    }
                }
            }
        }
    }

    private boolean ruleNameIsInsensitiveLetter(String ruleName) {
        return ruleName.length() == 1 || ruleName.substring(1).equals(SQLBNF.LETTER_SUFFIX);
    }
    
    private void markKeywordRulesAlternativesOfCase(List<Rule> rules, Map<String, Rule> ruleMap) {
        // now find the keyword rules - this is assuming the keyword is represented as 
        // rules with a sequence of upper or lower case literals
        // they have upper-case names, with references to the letters
//        Rule (NORMAL) :ALL
//           Alternatives (*)
//                 Alternative (*):
//                       Group
//                          Alternatives (*)
//                                Alternative ():
//                                      Literal : "A"
//                                Alternative ():
//                                      Literal : "a"
//                       Group
//                          Alternatives (*)
//                                Alternative ():
//                                      Literal : "L"
//                                Alternative ():
//                                      Literal : "l"
//                       Group
//                          Alternatives (*)
//                                Alternative ():
//                                      Literal : "L"
//                                Alternative ():
//                                      Literal : "l"
        for (Rule rule : rules) {
            // they have multi-letter upper-case names
            String ruleName = rule.getRuleName();
            if (ruleName.matches("[A-Z]+")) {
                LOGGER.debug("rule \n{}", rule.getStructure(""));
                GrammarItem rhs = rule.getRhs();
                if (rhs.getType() == ItemType.ALTERNATIVES) {
                    // one alternative child
                    List<GrammarItem> topAlts = rhs.getChildren();
                    if (topAlts.size() == 1) {
                        List<GrammarItem> groups = topAlts.get(0).getChildren();
                        // same length as name
                        if (groups.size() == ruleName.length()) {
                            // to make the final comparison, split into letters
                            String[] letters = ruleName.split("");
                            int i = 0;
                            boolean goodSoFar = true;
                            for (GrammarItem group : groups) {
                                // that should be
//                                 Alternatives (al*)
//                                    Alternative ():
//                                          Literal : "O"
//                                    Alternative ():
//                                          Literal : "o"
                                List<GrammarItem> groupContent = group.getChildren();
                                if (groupContent.size() == 1) {
                                    List<GrammarItem> alts = groupContent.get(0).getChildren();
                                    if (alts.size() == 2) {
                                         boolean lower = false;
                                         boolean upper = false;
                                         for (GrammarItem alt : alts) {
                                            if (alt.getType() == ItemType.ALTERNATIVE && alt.getChildren().size() == 1
                                                    && alt.getChildren().get(0).getType() == ItemType.LITERAL) {
                                                String letter = ((InLiteral) alt.getChildren().get(0)).getValue();
                                                if (letter.length() == 1) {
                                                    if (letter.equals(letters[i])) {
                                                        upper = true;
                                                    } else if (letter.toUpperCase().equals(letters[i])) {
                                                        lower = true;
                                                    }
                                                }
                                                
                                            }
                                        }
                                         goodSoFar = goodSoFar & upper & lower;
                                         
                                    }
                                    i++;
                                    
                                }
                            }
                            if (goodSoFar) {
                                rule.setRuleType(RuleType.KEYWORD);
                            }
   
                        }
                    }
                }
            }
        }
    }
    
    private void markLetterRules(List<Rule> rules) {
        // find the letter rules - example
//          Rule: P
//             Alternatives (*)
//                   Alternative ():
//                         Literal : "P"
//                   Alternative ():
//                         Literal : "p"
        for (Rule rule : rules) {
            // they have single letter upper-case names
            String ruleName = rule.getRuleName();
            if (ruleName.matches("[A-Z]")) {
                GrammarItem rhs = rule.getRhs();
                if (rhs.getType() == ItemType.ALTERNATIVES) {
                    // children must all be alternative
                    List<GrammarItem> alts = rhs.getChildren();
                    if (alts.size() == 2) {
                        if ( alts.stream().allMatch(a -> 
                                a.getChildren().size() == 1 
                                && a.getChildren().stream().allMatch(c ->
                                    c.getType() == ItemType.LITERAL
                                 && ((InLiteral) c).getValue().equalsIgnoreCase(ruleName)))){
                            LOGGER.debug("Rule {} is a letter rule", ruleName);
                            rule.setRuleType(RuleType.LETTER);
                        }
                    }
                }
            }
        }
    }
    
    private void markBnfRules(List<Rule> rules) {
        // find the concocted bnf rules - example

        for (Rule rule : rules) {
            // they have names matching the
            String ruleName = rule.getRuleName();
            BnfSymbols bnfSymbolFromRuleName = BnfSymbols.getByName(ruleName);
            LOGGER.debug("rule {} gave {}", ruleName, bnfSymbolFromRuleName);
            if (bnfSymbolFromRuleName != null) {
                // should check further ?
                rule.setRuleType(RuleType.BNF);
                LOGGER.debug("BNF rule {}", rule.getStructure(""));

            }
        }
    }
}
