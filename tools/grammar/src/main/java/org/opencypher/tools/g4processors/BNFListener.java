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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opencypher.tools.antlr.bnf.BNFBaseListener;
import org.opencypher.tools.antlr.bnf.BNFLexer;
import org.opencypher.tools.antlr.bnf.BNFParser.AlternativeContext;
import org.opencypher.tools.antlr.bnf.BNFParser.AlternativesContext;
import org.opencypher.tools.antlr.bnf.BNFParser.BnfsymbolsContext;
import org.opencypher.tools.antlr.bnf.BNFParser.CharactersetContext;
import org.opencypher.tools.antlr.bnf.BNFParser.ElementContext;
import org.opencypher.tools.antlr.bnf.BNFParser.ExclusioncharactersetContext;
import org.opencypher.tools.antlr.bnf.BNFParser.IdContext;
import org.opencypher.tools.antlr.bnf.BNFParser.LhsContext;
import org.opencypher.tools.antlr.bnf.BNFParser.ListcharactersetContext;
import org.opencypher.tools.antlr.bnf.BNFParser.NamedcharactersetContext;
import org.opencypher.tools.antlr.bnf.BNFParser.NormaltextContext;
import org.opencypher.tools.antlr.bnf.BNFParser.OptionalitemContext;
import org.opencypher.tools.antlr.bnf.BNFParser.RequireditemContext;
import org.opencypher.tools.antlr.bnf.BNFParser.RhsContext;
import org.opencypher.tools.antlr.bnf.BNFParser.Rule_Context;
import org.opencypher.tools.antlr.bnf.BNFParser.RuleidContext;
import org.opencypher.tools.antlr.bnf.BNFParser.RulelistContext;
import org.opencypher.tools.antlr.bnf.BNFParser.RulerefContext;
import org.opencypher.tools.antlr.bnf.BNFParser.TextContext;
import org.opencypher.tools.g4tree.BnfSymbolLiteral;
import org.opencypher.tools.g4tree.BnfSymbols;
import org.opencypher.tools.g4tree.CharacterLiteral;
import org.opencypher.tools.g4tree.EOFreference;
import org.opencypher.tools.g4tree.ExclusionCharacterSet;
import org.opencypher.tools.g4tree.FreeTextItem;
import org.opencypher.tools.g4tree.GrammarItem;
import org.opencypher.tools.g4tree.GrammarName;
import org.opencypher.tools.g4tree.GrammarTop;
import org.opencypher.tools.g4tree.Group;
import org.opencypher.tools.g4tree.InAlternative;
import org.opencypher.tools.g4tree.InAlternatives;
import org.opencypher.tools.g4tree.InLiteral;
import org.opencypher.tools.g4tree.InOptional;
import org.opencypher.tools.g4tree.ListedCharacterSet;
import org.opencypher.tools.g4tree.NamedCharacterSet;
import org.opencypher.tools.g4tree.OneOrMore;
import org.opencypher.tools.g4tree.Rule;
import org.opencypher.tools.g4tree.RuleId;
import org.opencypher.tools.g4tree.RuleList;
import org.opencypher.tools.g4tree.ZeroOrMore;
import org.opencypher.tools.g4tree.GrammarItem.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * builds a GrammarItem tree from a SQL-BNF specification 
 */
public class BNFListener extends BNFBaseListener
{
    // build
    private final ParseTreeProperty<GrammarItem> items = new ParseTreeProperty<>();
    private final CommonTokenStream tokens; 
    private GrammarTop treeTop = null;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BNFListener.class.getName());
    
    public BNFListener(CommonTokenStream tokens)
    {
        super();
        this.tokens = tokens;
    }

    public GrammarTop getTreeTop()
    {
        return treeTop;
    }

    private GrammarItem getItem(ParseTree ctx) {
        return items.get(ctx);
    }
    
    private void setItem(ParseTree ctx, GrammarItem item) {
        LOGGER.debug("setting a {} for {}", item.getClass().getSimpleName(), ctx.getText());
        items.put(ctx, item);
    }

    private void pullUpItem(ParseTree ctx) {

        if (ctx.getChildCount() != 1) {
            throw new IllegalStateException("There should be only one child at " + ctx.getText() + " but there are " +
                        ctx.getChildCount());
        }
        GrammarItem movedItem = getItem(ctx.getChild(0));
        if (movedItem != null) {
            items.put(ctx,  movedItem);
        } else {
            throw new IllegalStateException("No item to be moved from " + ctx.getChild(0).getClass().getSimpleName()
                    + " up to " + ctx.getClass().getSimpleName());
        }
    }


    @Override
    public void exitRulelist(RulelistContext ctx)
    {
        String header = findHiddenTextBefore(ctx, true).replaceFirst("\r?\n$", "");
        // this is the top level for bnf, since that doesn't have a global declaration
        RuleList rules = new RuleList();
        GrammarName name = null;
        for (Rule_Context ruleCtx : ctx.rule_())
        {
            GrammarItem grammarItem = getItem(ruleCtx);
            if (name == null) {
                // first rule is also the grammar name
                name = new GrammarName(((Rule) grammarItem).getRuleName());
            }
            rules.addItem(grammarItem);
        }

        treeTop = new GrammarTop(name, rules, header.length() > 0 ? header : null);
    }


    @Override
    public void exitRule_(Rule_Context ctx)
    {
        String description = (findHiddenTextBefore(ctx, false) 
                + findHiddenTextWithin(ctx)
                + findHiddenTextAfter(ctx)).replaceFirst("\r?\n$", "");
        Rule rule = new Rule(getItem(ctx.lhs()), getItem(ctx.rhs()), description.length() == 0 ? null : description);
        setItem(ctx, rule);
    }

    @Override
    public void exitLhs(LhsContext ctx)
    {
        // lhs is now < ruleid >, so it's the middle bit we want
        setItem(ctx, getItem(ctx.getChild(1)));
    }

    @Override
    public void exitBnfsymbols(BnfsymbolsContext ctx)
    {
        setItem(ctx, new InLiteral(ctx.getText()));
    }

    @Override
    public void exitRhs(RhsContext ctx)
    {
        pullUpItem(ctx);
    }

    @Override
    public void exitAlternatives(AlternativesContext ctx)
    {
        InAlternatives alts = new InAlternatives();
        for (AlternativeContext element : ctx.alternative())
        {
            alts.addItem(getItem(element));
        }
        setItem(ctx, alts);
    }

    @Override
    public void exitAlternative(AlternativeContext ctx)
    {
        InAlternative alt = new InAlternative();
        // special case //
        
        for (ElementContext element : ctx.element())
        {
            alt.addItem(getItem(element));
        }
        // special case //
        if (alt.size() == 2 && 
            alt.getChildren().stream().allMatch(c -> c.getType() == ItemType.LITERAL
                    && ((InLiteral)c).getValue().equals("/")) ) {
            // make a new one
            alt = new InAlternative();
            alt.addItem(new InLiteral("//"));
        }

        setItem(ctx, alt);
    }

    // this is looking for description or grammar header
    // now defined as // lines
    //   for description : immediately before rule, with no blank lines
    //   for header : blank line before
    private String findHiddenTextBefore(ParserRuleContext ctx, boolean forHeader)
    {
        Token startCtx = ctx.getStart();
        int i = startCtx.getTokenIndex();
        List<Token> normalTextChannel =
                    tokens.getHiddenTokensToLeft(i, BNFLexer.HIDDEN);
        if (normalTextChannel != null) {
            // find where the blank lines are
            // when called for a rule, is the quasi-comment part of the content of the previous rule or
            // the description of this one. Immaterial for grammar header
            
            List<Token> lineTokens = normalTextChannel.stream().collect(Collectors.toList());

            int precedingBlankLines = startCtx.getLine() - lineTokens.get(lineTokens.size()-1).getLine() - 1;
            if (precedingBlankLines > 0) {
                if (forHeader) {
                    // this will preserve the linefeeds
                    return lineTokens.stream().map(tk -> tk.getText().replaceFirst("// ?", ""))
                            .collect(Collectors.joining("\n"));
                }  // it wasn't a description (just a stray comment ?)
            } else {
                if (forHeader) {
                    // no blank line, so this is a description to the first 
                    return "";
                }
                // description - go back and find any gap showing a last blank line
                int lastGoodLine = startCtx.getLine() - 1;        
                int currentIndex = lineTokens.size() - 1;
                while (currentIndex >= 0 && lineTokens.get(currentIndex).getLine() == lastGoodLine) {
                    currentIndex--;
                    lastGoodLine--;
                }
                List<String> content = new ArrayList<>();
                for (int j = currentIndex + 1; j <lineTokens.size(); j++) {
                    content.add(lineTokens.get(j).getText().replaceFirst("// ?", ""));
                }
                return content.stream().collect(Collectors.joining("\n"));
            }
        }
        return "";
    }
    
    // looking for free (comment) text in the middle of the production
    private String findHiddenTextWithin(ParserRuleContext ctx)
    {
        List<Token> allTokens = tokens.get(ctx.getStart().getTokenIndex(),  ctx.getStop().getTokenIndex());
        
        return allTokens.stream().filter(t -> t.getChannel() == BNFLexer.HIDDEN)
            .map(t -> t.getText().replaceFirst("// ?", "")).collect(Collectors.joining("\n"));
    }


    
    // looking for free (comment) text at the end of the production
    private String findHiddenTextAfter(ParserRuleContext ctx)
    {
        Token endCtx = ctx.getStop();
        int i = endCtx.getTokenIndex();
        List<Token> normalTextChannel =
                    tokens.getHiddenTokensToRight(i, BNFLexer.HIDDEN);
        if (normalTextChannel != null) {
            // the quasi-comment (description) may be the end of a rule or start of the next. separation is on
            // a blank line
            int nextLine = endCtx.getLine() + 1;
            List<String> content = new ArrayList<>();
            for (Token lineToken : normalTextChannel) {
                if (lineToken.getLine() == nextLine) {
                    content.add(lineToken.getText().replaceFirst("// ?", ""));
                    nextLine++;
                } else {
                    break;
                }
            }
            return content.stream().collect(Collectors.joining("\n"));
        }
        return "";
    }

    @Override
    public void exitElement(ElementContext ctx)
    {
        pullUpItem(ctx);
    }

    @Override
    public void exitOptionalitem(OptionalitemContext ctx)
    {
        // if there are a lot of these, it's actually a zero or more
        // required item is known to be plural, as it has braces round something
        final GrammarItem contentItem = getItem(ctx.alternatives());
        final GrammarItem ourItem ;
        if (ctx.ELLIPSIS() == null) {
            
            // convert [ { a b } ... ] from optional(oneOrMore) to zeroOrMore
            GrammarItem simplifiedContent = contentItem.reachThrough();
            if (simplifiedContent instanceof OneOrMore) {
                ourItem = new ZeroOrMore(((OneOrMore) simplifiedContent).getContent());
            } else {
                ourItem = new InOptional(contentItem);
            }
        } else {
            ourItem = new ZeroOrMore(contentItem);
        }
        setItem(ctx, ourItem);
    }

    @Override
    public void exitRequireditem(RequireditemContext ctx)
    {
        // required item is known to be plural, as it has braces round something
        final GrammarItem contentItem = getItem(ctx.alternatives());
        final GrammarItem ourItem ;
        if (ctx.ELLIPSIS() == null) {
            // this is just a group
            if (contentItem.isPlural()) {
                ourItem = new Group(contentItem);
            } else {
                // unnecessary braces, i think
                ourItem = contentItem;
            }
            
        } else {
            ourItem = new OneOrMore(contentItem);
        }
        setItem(ctx, ourItem);
    }

    private static final Pattern UCODE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]+)");
    
    @Override
    public void exitText(TextContext ctx)
    {
        String text = ctx.getText().trim();
        // text is UNICODE_LITERAL | WORD | CHARACTER_LITERAL | INTEGER_LITERAL
        if (ctx.UNICODE_LITERAL() != null) {
             // translate it
            setItem(ctx, new InLiteral(((Character) ((char) Integer.parseInt(text.substring(2), 16))).toString() ));
        } else if (ctx.CHARACTER_LITERAL() != null) {
            setItem(ctx, new CharacterLiteral(text));
        } else {
            // i think everything else is just a regular literal
            setItem(ctx, new InLiteral(text));
        }
    }

    @Override
    public void exitCharacterset(CharactersetContext ctx) {
        if (ctx.getChildCount() == 3) {
            GrammarItem movedItem = getItem(ctx.getChild(1));
            if (movedItem != null) {
//                LOGGER.warn("moving a {} upto to ctx {}", 
//                      movedItem.getClass().getSimpleName(), ctx.getClass().getSimpleName());
                items.put(ctx,  movedItem);
            } else {
                throw new IllegalStateException("No item to be moved from " + ctx.getChild(1).getClass().getSimpleName()
                        + " up to " + ctx.getClass().getSimpleName());
            }
        } else {
            throw new IllegalStateException("child count for character set is " + ctx.getChildCount() + ". expected 3");
        }
    }
    
    
    @Override
    public void exitNamedcharacterset(NamedcharactersetContext ctx) {
        setItem(ctx, new NamedCharacterSet(ctx.ID().getText()));
    }

    @Override
    public void exitExclusioncharacterset(ExclusioncharactersetContext ctx) {
        ListcharactersetContext listCtx = ctx.listcharacterset();
        setItem(ctx, new ExclusionCharacterSet(interpret(listCtx)));
    }

    @Override
    public void exitListcharacterset(ListcharactersetContext ctx) {
        setItem(ctx, new ListedCharacterSet(interpret(ctx)));
    }
    
    
    @Override
    public void exitNormaltext(NormaltextContext ctx) {
        // prune off the BNF marker
        String content = ctx.getText().replaceFirst("!!\\s*","");
        if (content.matches("\\s*")) {
            return;
        }
        setItem(ctx, new FreeTextItem(content));
    }

    private String interpret(ListcharactersetContext listCtx) {
        // to cope with punctuation, especially backslash, we use text.  this may cause grief 
        // if we need to have ] in character set
        
    
        List<String> bnfString = listCtx.text().stream().map(TextContext::getText).collect(Collectors.toList());
        LOGGER.debug("bnfString {}.", bnfString);
        StringBuilder b = new StringBuilder();
        while (! bnfString.isEmpty()) {
            String piece = bnfString.remove(0);
            LOGGER.debug("piece is [{}]", piece);
            if (! piece.equals("\\")) {
                b.append(piece);
            } else {
                String next = bnfString.remove(0);
                char first = next.charAt(0);
                String rest = next.substring(1);
                switch (first) {
                    case 'r': b.append('\r');  break;
                    case 'n': b.append("\n");  break;
                    case 't': b.append("\t");  break;
                    case 'b': b.append("\b");  break;
                    case 'f': b.append("\f");  break;
                    case '\\': b.append("\\\\"); break;
                    case '-':  b.append("-");  break;
                    case ']':  b.append("]");  break;
                    case '$':  b.append("$");  break;
                    // have to escape bnf comment characters !
                    case '/':  b.append("/");  break;
                    case 'u':  
                        if (rest.length() < 4) {
                            throw new IllegalArgumentException("unicode escape requires 4 hex digits");
                        }
                        int cp = Integer.parseInt(rest.substring(1,4), 16);
                        b.append((char) cp);
                        rest = rest.substring(4);
                        break;
                default:
                    throw new IllegalArgumentException("Don't know how to interpret escaped character " + first);
                }
                if (rest.length() > 0) {
                    b.append(rest);
                }
            }
            
        }
        String answer = b.toString();
//        LOGGER.warn("became {}, len {}", answer, answer.length());
        
        return answer;
    }


    @Override
    public void exitId(IdContext ctx)
    {
//        GrammarItem item = MappedLiterals.getFromBNFid(ctx.ruleref().getText());
        String ruleName = ctx.ruleref().getText();
        if ( ruleName.equals(EOFreference.BNF_NAME)) {
            setItem(ctx, new EOFreference());
        } else {
            GrammarItem item = new RuleId(ruleName);

            if (ctx.ELLIPSIS() != null) {
                setItem(ctx, new OneOrMore(item));
            } else {
                setItem(ctx, item);
            }
        }
    }

    @Override
    public void exitRuleref(RulerefContext ctx) {
        String referencedName = ctx.getText();
        BnfSymbols bnfSymbol = BnfSymbols.getByName(referencedName);
        if (bnfSymbol != null) {
            // if this is only here because bnf can't put bnf symbols in normal rules, 
            // reverse it
            setItem(ctx, new BnfSymbolLiteral(bnfSymbol));
        } else {
            setItem(ctx, new RuleId(referencedName));
        }
    }

    @Override
    public void exitRuleid(RuleidContext ctx)
    {
        setItem(ctx, new RuleId(ctx.getText()));

    }

    
}
