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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * characters or groups of characters that need special treatment in BNF, G4 or both
 */
public enum BnfSymbols {
    // real bnf symbols
    //   bnf "escapes" by declaring an element whose right hand side is only bnfsymbols
    //      9075-2 doesn't entirely obey this, as <greater than operator> ::= >=
    //    ASSIGN | LBRACE | RBRACE | LEND | REND | BAR | GT | LT | ELLIPSIS 
    ASSIGN("::="),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    LEFT_BRACE("{","\\{"),
    RIGHT_BRACE("}","\\}"),
    LEFT_BRACKET("[","\\["),
    RIGHT_BRACKET("]", "\\]"),
    ELLIPSIS("...", "\\.\\.\\."),
    DOUBLE_EXCLAM("!!","!!","\\!\\!"),
    VERTICAL_BAR("|","\\|"),
    DOLLAR("$","\\$"),   // used for charsets
    REVERSE_SOLIDUS("\\", "\\\\")  // used in unicode
    ;
    
    /** what the characters really are */
    private final String actualCharacters;
    public String getActualCharacters() {
        return actualCharacters;
    }

    public String getBnfForm() {
        return bnfForm;
    }

    public String getG4Name() {
        return g4Name;
    }

    public String getBnfName() {
        return bnfName;
    }

    /** if needs to be handled specially in bnf, how does it appear in the defining rule ? */
    private final String bnfForm;
    /** if needs special handling in g4, what will the fragment name be (I think) */
    private final String g4Name;
    // the form in G4 will just be quoted, so no problem
    /** what will be the name of the bnf element, if needed */
    private final String bnfName;
    private String regexChars;

    
    private static final Map<String, BnfSymbols> charMap;
    private static final Map<String, BnfSymbols> bnfNameMap;
//    private static final Set<String> punctuation;
    private static final Pattern bnfAnyPattern;
    private static final Pattern bnfAllPattern;

    static {
        charMap = new HashMap<>();
        bnfNameMap = new HashMap<>();
//        punctuation = new HashSet<>();
        List<String> bnfSyms = new ArrayList<>();
        
        for (BnfSymbols lit : BnfSymbols.values()) {
            charMap.put(lit.actualCharacters, lit);
            bnfNameMap.put(lit.bnfName, lit);
                // a special literal may have been escaped 
                //  (arguably, this depends on the input language
            charMap.put(lit.bnfForm,  lit);
            bnfSyms.add(lit.regexChars);
//            for (String character : lit.actualCharacters.split("")) {
//                punctuation.add(character);
//            }

        }
        // make a pattern for determining all of a string is bnf symbols
        String pattern = bnfSyms.stream().collect(Collectors.joining("|"));
        bnfAnyPattern = Pattern.compile("(" + pattern + ")");
        bnfAllPattern = Pattern.compile("(?:" + pattern + ")+");
    }
    
    BnfSymbols(String characters) {
        this(characters, characters, characters);
    }

    BnfSymbols(String characters, String escapedCharacters)
    {
        this(characters, escapedCharacters, characters);
    }
    BnfSymbols(String characters, String escapedCharacters, String bnfOutcharacters)
    {
        
        this.actualCharacters = characters;
        this.bnfForm =  bnfOutcharacters;
        this.regexChars = escapedCharacters;

            // bnfName is lower case with spaces in angles
        this.bnfName = name().toLowerCase().replaceAll("_", " ");
        
        // g4name is as is (uppercase with _) - this is a lexer rule
        g4Name = name();

    }

    public static BnfSymbols getByValue(String characters) {
        return charMap.get(characters);
    }
    
    public static BnfSymbols getByName(String bnfName) {
        return bnfNameMap.get(bnfName);
    }
    
    public static boolean allBnfSymbols(String subject) 
    {
        Matcher m = bnfAllPattern.matcher(subject);
        return m.matches();

    }

    public static boolean anyBnfSymbols(String subject) {
        return bnfAnyPattern.matcher(subject).find();
    }
    
    public static Interleaver getInterleave(String value) {
        final Matcher m = bnfAnyPattern.matcher(value);
        int begin = 0;
        final List<String> text = new ArrayList<>();
        final List<BnfSymbols> symbols = new ArrayList<>();
        
        while (m.find()) {
            int s = m.start();
            if (s > begin) {
                text.add(value.substring(begin, s));
            } else {
                text.add("");
            }
            symbols.add(BnfSymbols.getByValue(m.group()));
            begin = m.end();
        }
        if (begin < value.length()) {
            text.add(value.substring(begin));
        } else {
            text.add("");
        }
        return    new Interleaver() {
                int i = 0;
                
                @Override
                public boolean hasNext() {
                    return i < symbols.size();
                }

                @Override
                public String nextText() {
                    return text.get(i);
                }

                @Override
                public BnfSymbols nextSymbol() {
                    return symbols.get(i++);
                }
                
            };
        
    }
    public interface Interleaver {
        boolean hasNext();
        String nextText();
        BnfSymbols nextSymbol();
    }
}
