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
package org.opencypher.grammar;

import static org.opencypher.grammar.Grammar.caseInsensitive;
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.epsilon;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.oneOf;
import static org.opencypher.grammar.Grammar.oneOrMore;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.grammar.Grammar.sequence;
import static org.opencypher.grammar.Grammar.zeroOrMore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opencypher.grammar.Grammar.Term;
import org.opencypher.tools.g4processors.Normaliser;
import org.opencypher.tools.g4tree.BnfSymbolLiteral;
import org.opencypher.tools.g4tree.BnfSymbols;
import org.opencypher.tools.g4tree.CharacterLiteral;
import org.opencypher.tools.g4tree.ElementWithCardinality;
import org.opencypher.tools.g4tree.ExclusionCharacterSet;
import org.opencypher.tools.g4tree.FreeTextItem;
import org.opencypher.tools.g4tree.GrammarItem;
import org.opencypher.tools.g4tree.GrammarTop;
import org.opencypher.tools.g4tree.InAlternative;
import org.opencypher.tools.g4tree.InAlternatives;
import org.opencypher.tools.g4tree.InLiteral;
import org.opencypher.tools.g4tree.ListedCharacterSet;
import org.opencypher.tools.g4tree.NamedCharacterSet;
import org.opencypher.tools.g4tree.Rule;
import org.opencypher.tools.g4tree.RuleId;
import org.opencypher.tools.g4tree.GrammarItem.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * convert a "grammar" tree from the BNF, G4 parsers into a standard Grammar
 */
public class GrammarConverter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GrammarConverter.class.getName());
    private final Normaliser normaliser = new Normaliser();
    private final Map<String, Rule> ruleMap;
    private final Set<String> unknownRules = new HashSet<>();
    private final GrammarTop grammarTop;
    
    public GrammarConverter(GrammarTop grammarTop) {
        this.grammarTop = grammarTop;
        LOGGER.debug("originally \n{}", grammarTop.getStructure(""));
        ruleMap = normaliser.normalise(grammarTop);
        LOGGER.debug("normalised \n{}", grammarTop.getStructure(""));
    }
    
    public Grammar convert() {
        String language = grammarTop.getName();
        Grammar.Builder builder = Grammar.grammar(language);
        
        String headerText = grammarTop.getHeader();
        if (headerText != null) {
            builder.addHeader(headerText.toCharArray(), 0,headerText.length());
        }
        List<Rule> rules = grammarTop.getRuleList().getRules();
        for (Rule rule : rules) {
            String ruleName = rule.getRuleName();
            if (rule.getRuleType().keep()) {
                GrammarItem rhs = rule.getRhs();
                Term term = convertItem(rhs);
                builder.production(ruleName, rule.getDescription(), term);
                if (rhs.getType() == ItemType.BNF_LITERAL ) {
                    builder.markAsBnfSymbols(ruleName);
                }
            } else {
                LOGGER.debug("suppressing {}", rule.getStructure(""));
            }
        }
        for (String ruleName : unknownRules) {
            // concoct dummy rules
            String description = "this rule was missing in the input grammar";
            builder.production(ruleName, description,  literal(ruleName.replaceAll(" ", "_")));
        }
        return builder.build();
    }
    
    private Term convertItem(GrammarItem item) {
        switch (item.getType()) {
        case ALTERNATIVE:
            return convertAlternative((InAlternative) item);
        case LITERAL:
            return convertLiteral((InLiteral) item);
        case ALTERNATIVES:
            return convertAlternatives((InAlternatives) item);
        case REFERENCE:
            return convertReference((RuleId) item);
        case CARDINALITY:
            return convertWithCardinality((ElementWithCardinality) item);
        case BNF_LITERAL:
            return convertSpecial( (BnfSymbolLiteral) item);
        case TEXT:
            return convertText( (FreeTextItem) item);
        case NAMEDCHARSET:
            return convertCharSet( (NamedCharacterSet) item);
        case LISTEDCHARSET:
            return convertCharSet( (ListedCharacterSet) item);
        case EXCLUSIONCHARSET:
            return convertCharSet( (ExclusionCharacterSet) item);
        case EOI:
            return convertEOI();
            
        default:
            LOGGER.warn("Don't know how to handle {} that is a {}", item.getType(), item.getClass().getSimpleName());
            break;
        }
        return literal("* itemtype = " + item.getType() + "*");
    }

    private Term convertCharSet(NamedCharacterSet item) {
        return charactersOfSet( item.getName());
    }

    private Term convertCharSet(ListedCharacterSet item) {
        String characters = item.getCharacters();
        LOGGER.debug("We have charset {}.", characters);
        if (characters.contains("\\u")) {
            LOGGER.warn("We have unicode escapes in {}", characters);
        }
        return charactersOfSet("[" + characters  + "]");
        
    }
    
    private Term convertCharSet(ExclusionCharacterSet item) {
        char[] exclusions =  item.getCharacters().toCharArray();
        List<Integer> cps = new ArrayList<>();
        int lastOne = 0;
        try {
            for (int i=0; i < exclusions.length; i++) {
                // usual escape fun
                char ch = exclusions[i];
                if (ch == '\\') {
                    ch = exclusions[++i];
                } else if (ch == '-') {
                    char next = exclusions[++i];
                    if (next == '\\') {
                        next = exclusions[++i];
                    }
                    for (int cp = lastOne+1; cp < next; cp++) {
                        cps.add(cp);
                    }
                    ch = next;
                }
                lastOne = (int) ch;
                cps.add(lastOne);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Bad character set with exceptions : '" + item.getCharacters() + "'");
        }
        return charactersOfSet("ANY").except(cps.stream().mapToInt(cp->cp).toArray());
    }
    
    private Term convertEOI() {
        return charactersOfSet("EOI");
    }

    private static final Pattern CHARSET_PATTERN = Pattern.compile("\\s*character\\s*set\\s+'(\\w+)'\\s*");
    
    private Term convertText(FreeTextItem item) {
        LOGGER.debug("free text {}.", item.getContent());
        // treat as a literal, since we don't have a grammar object to hold it.
        //  there's an issue with line breaks - something upstream would convert them to 
        //  line feed character set, so special case them
        return literal("!! " + item.getContent());
    }

    private Term convertWithCardinality(ElementWithCardinality item) {
        // i think this is going to impose a seq
        Term child = convertItem(item.extractContent());
        if (item.getMin() == 0) {
            if (item.isUnbounded()) {
                // 0..*
                return zeroOrMore(child);
            } else {
                // 0..1
                return optional(child);
            }
        } else if (item.isUnbounded()) {
            return oneOrMore(child);
        } else {
            // 1..1 - just the seq, i think (if there's only one, something is superfluous
            return sequence(child);
        }
    }

    private Term convertAlternative(InAlternative item) {
        List<GrammarItem> children = item.getChildren();
        if (children.size() == 0) {
            LOGGER.debug("no child items from {}", item);
            return epsilon();
        }
        // leaving bnf, combine sequence of literal and references to bnfsymbol rules
        List<Term> terms = new ArrayList<>();
        boolean inLiterals = false;
        StringBuilder pending = new StringBuilder();
        
        for (GrammarItem grammarItem : children) {
            switch (grammarItem.getType()) {
            case CHARACTER_LITERAL:
                pending.append(((CharacterLiteral) grammarItem).getValue());
                inLiterals = true;
                break;
            case BNF_LITERAL:
                // a reference to bnf characters, that can be unwound in standard grammar
                pending.append(((BnfSymbolLiteral) grammarItem).getCharacters());
                inLiterals = true;
            default:
                if (inLiterals) {
                    terms.add(convertItem(new InLiteral(pending.toString())));
                    pending = new StringBuilder();
                    inLiterals = false;
                }
                terms.add(convertItem(grammarItem));
                break;
            }
        }
        if (inLiterals) {
            terms.add(convertItem(new InLiteral(pending.toString())));
        }

        return sequence(first(terms), getRest(terms));
    }

    private Term convertLiteral(InLiteral lit) {
        String value = lit.getValue();
        String[] words = value.split("\\s+");
        if (words.length == 0) {
            // its all whitespace !
            return literal(value);
        }
        // i'm sure there are better ways of doing this
        List<Term> lits = Stream.of(words).map(w -> literal(w)).collect(Collectors.toList());

        return sequence(lits.get(0), getRest(lits));
    }

    private Term convertSpecial(BnfSymbolLiteral item) {
        // if this hasn't been suppressed (for being a bnf special), it is part of a 
        // user-specified, all bnf rule
        String value = item.getCharacters();
        return literal(value);
    }
    
    private Term convertAlternatives(InAlternatives alt) {
        List<GrammarItem> children = alt.getChildren();
        List<Term> terms = children.stream().map(g -> convertItem(g)).collect(Collectors.toList());
        return oneOf(first(terms), getRest(terms));
    }
    
    private Term convertReference(RuleId ref) {
        
        String ruleName = ref.getName();
        Rule referencedRule = ruleMap.get(ruleName);
        if (referencedRule != null) {
            LOGGER.debug("ref to {} which is a {}", ruleName, referencedRule.getRuleType());
            switch (referencedRule.getRuleType()) {
            case NORMAL:
                return nonTerminal(ruleName);
            case KEYWORD:
            case KEYWORD_LITERAL:
                return caseInsensitive(ruleName);
            case BNF:
                BnfSymbols bnfSymbol = BnfSymbols.getByName(ruleName);
                return literal(bnfSymbol.getActualCharacters());
                // not sure about LETTER
            case LETTER:
                return caseInsensitive(ruleName);
            case FRAGMENT:
                // pull up the fragment into the reference. i think
                List<GrammarItem> children = referencedRule.getChildren();
                LOGGER.debug("consider fragment {} = {}", ruleName, children);
                List<Term> terms = children.stream().map(g -> convertItem(g)).collect(Collectors.toList());
                LOGGER.debug("fragment reference becomes {}", terms);
                if (terms.size() == 1) {
                    return terms.get(0);
                }
                return sequence(first(terms), getRest(terms));
            default:
                LOGGER.warn("No special handling for rulereference {}, type {}", ruleName, referencedRule.getRuleType());
                break;
            }
        } else {
            LOGGER.warn("Reference to unknown rule {}", ruleName);
            unknownRules.add(ruleName);
            return nonTerminal(ruleName);
        }
        return nonTerminal(ruleName);
    }
    
    protected Term first(List<Term> list) {
        return list.get(0);
    }
    protected Term[] getRest(List<Term> list) {
        if (list.size() <= 1) {
            return null;
        }
        List<Term> subList = list.subList(1, list.size());
        return subList.toArray(new Term[list.size()-1]);
    }
    

}
