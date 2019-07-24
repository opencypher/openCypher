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
package org.opencypher.tools.antlr;

import static org.junit.Assert.assertEquals;
import static org.opencypher.tools.io.Output.lines;
import static org.opencypher.grammar.Grammar.caseInsensitive;
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.grammar.Grammar.zeroOrMore;

import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.grammar.Antlr4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class G4ProcessorTest {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(G4ProcessorTest.class.getName());
	
	@Before
	public void initialize() {
		Antlr4.setPrefix("");
	}
	
	@After
	public void cleanup() {
		Antlr4.resetPrefix();
	}
	@Test
	public void oneProduction() {
		roundTripBNFG4("<alpha> ::= ALPHA");
	}

	@Test
	public void twoProductions() {
		roundTripBNFG4("<alpha> ::= ALPHA",
				 "",
				 "<beta> ::= BETA");
	}
	
	@Test
	public void twoLiterals() {
		roundTripBNFG4("<alphabeta> ::= ALPHA BETA");
	}
	
	@Test
	public void alternatives() {
		roundTripBNFG4("<choice> ::= ALPHA | BETA");
	}
	
	@Test
	public void alternativesAgain() {
		roundTripBNFG4("<choice> ::= ALPHA ",
				 "     | BETA");
	}
	
	@Test
	public void reference() {
		roundTripBNFG4("<one> ::=  <beta>",
				  "",
				  "<beta> ::= BETA");
	}
	
	@Test
	public void repeatGroup() {
		roundTripBNFG4("<some> ::=  { ALPHA <beta> } ...",
				  "",
				  "<beta> ::= BETA");
	}
	
	@Test
	public void repeatOptionalGroup() {
		roundTripBNFG4("<some> ::=  [ ALPHA <beta> ] ...",
				  "",
				  "<beta> ::= BETA");
	}
	
	@Test
	public void optional() {
		roundTripBNFG4("<perhaps> ::=  [ <beta> ]",
				  "",
				  "<beta> ::= BETA");
	}
	
	@Test
	public void verticalBar() {
		// first production will be used a language name and
		// cannot contain spaces
		roundTripBNFG4("<verticalBar> ::= |");
	}
	
	
	@Test
	public void keyword() {
		// case-insensitive keywords need to spotted and converted
		// xml grammar requires productions to be different case-insensitively
		// this is what would have been produced
		roundTripBNFG4("<something> ::= <STOP>",
				"",
				"<STOP> ::= <S> <T> <O> <P>",
				"",
				"<O> ::= O | o",
				"",
				"<P> ::= P | p",
				"",
				"<S> ::= S | s",
				"",
				"<T> ::= T | t"
				);
	}
	
	@Test
	public void unicode() {
		roundTripBNFG4("<whitespace> ::= SPACE | TAB | LF | 0x1680 | 0x180e | 0x2000 | 0x2001");
	}


	@Test
	public void bnfProduction() {
		roundTripBNFG4("<prodassign> ::= ::=");
	}
	

	@Test
	public void bnfSymbolsProduction() {
		roundTripBNFG4("<notequal> ::= <>");
	}
	
	@Test
	public void commaProduction() {
		roundTripBNFG4("<comma> ::= ,");
	}
		
	@Test
	public void punctuation() {
		roundTripBNFG4("<puncs> ::= +.-");
	}
	@Test
	public void commasProduction() {
		roundTripBNFG4("<list> ::= ONE <comma> TWO <comma> THREE",
				"",
				"<comma> ::= ,");
	}
	
	@Test
	public void charset() {
		roundTripG4(grammar( "test" )
		        .production( "test", charactersOfSet( "[abcd]" ) ).build());
	}
	
	// haven't made this one work
	@Test
	@Ignore
	public void charsetChoice() {
		roundTripBNFG4("<anychars> ::= !! characterset 'ID_Start'",
					"  |  !! characterset 'Pc'");
	}
	
	@Test
	public void shouldRecycleCypher() throws Exception
	{
//		G4Processor processor = new G4Processor();
		Grammar grammar = Fixture.grammarResource( G4Processor.class, "/cypher.xml");
//		String bnfFile = "C:/Users/Peter/gitg4bnf/antlr4-bnf-translator/grammars/cypher.bnf";
//		Grammar grammar = processor.processFile(bnfFile);
		// now reprocess 
		// not doing this at the moment because two of the rules get an unnecessary { } round a single item
		String firstG4 = makeAntlr4(grammar);
		LOGGER.debug("Generated \n{}", firstG4);
		// do we need a new one ?
		G4Processor secondProcessor = new G4Processor();
		Grammar grammarTwo = secondProcessor.processString(firstG4);
		String intermediateG4 = makeAntlr4(grammarTwo);
		Grammar grammarThree = secondProcessor.processString(intermediateG4);
		String finalG4 = makeAntlr4(grammarTwo);
		// can i eat my own dog food
		assertEquals(intermediateG4, finalG4);
		// but can i handle everything ?
		// not yet
//		assertEquals(firstG4, intermediateG4);
	}
	
	
	private void roundTripBNFG4(String... bnf) 
	{
		String inG4 = lines(bnf).trim();
		BNFProcessor bnfProcessor = new BNFProcessor();
		Grammar bnfGrammar = bnfProcessor.processString(inG4);
		roundTripG4(bnfGrammar);
	}

	private void roundTripG4(Grammar testGrammar) {
		String firstG4 = makeAntlr4(testGrammar);
		LOGGER.warn("in \n{}", firstG4);
		
		G4Processor processor = new G4Processor();
		Grammar grammar = processor.processString(firstG4);
		String outputG4 = makeAntlr4(grammar);
		LOGGER.debug("out \n{}", outputG4);
		assertEquals(unPretty(firstG4), unPretty(outputG4));
	}

	
	
	private void roundTripG4(String... inputG4) 
	{
		String inG4 = lines(inputG4).trim();
		G4Processor processor = new G4Processor();
		LOGGER.debug("in {}", inG4);
		Grammar grammar = processor.processString(lines(inputG4));
		String outputG4 = makeAntlr4(grammar);
		LOGGER.debug("out {}", outputG4);
		assertEquals(unPretty(inG4), unPretty(outputG4));
	}


	
	private String makeAntlr4(Grammar grammar) {
		StringWriter writer = new StringWriter();
		Antlr4.write(grammar, writer);
		
		String outputG4 = writer.toString().trim();
		return outputG4;
	}

	private String unPretty(String original) {
		String nl = System.lineSeparator();
		return original.replaceAll(nl + nl, "#!#").replaceAll("\\s+", " ").replaceAll("#!#", nl + nl);
	}

}
