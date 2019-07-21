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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Ignore;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.grammar.ISO14977;
import org.opencypher.tools.grammar.SQLBNF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class BNFProcessorTest {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(BNFProcessorTest.class.getName());
	
	@Test
	public void oneProduction() {
		roundTripBNF("<alpha> ::= ALPHA");
	}

	@Test
	public void twoProductions() {
		roundTripBNF("<alpha> ::= ALPHA",
				 "",
				 "<beta> ::= BETA");
	}
	
	@Test
	public void twoLiterals() {
		roundTripBNF("<alphabeta> ::= ALPHA BETA");
	}
	
	@Test
	public void alternatives() {
		roundTripBNF("<choice> ::= ALPHA | BETA");
	}
	
	@Test
	public void alternativesAgain() {
		roundTripBNF("<choice> ::= ALPHA ",
				 "     | BETA");
	}
	
	@Test
	public void reference() {
		roundTripBNF("<one> ::=  <beta>",
				  "",
				  "<beta> ::= BETA");
	}
	
	@Test
	public void repeatGroup() {
		roundTripBNF("<some> ::=  { ALPHA <beta> } ...",
				  "",
				  "<beta> ::= BETA");
	}
	
	@Test
	public void repeatOptionalGroup() {
		roundTripBNF("<some> ::=  [ ALPHA <beta> ] ...",
				  "",
				  "<beta> ::= BETA");
	}
	
	@Test
	public void optional() {
		roundTripBNF("<perhaps> ::=  [ <beta> ]",
				  "",
				  "<beta> ::= BETA");
	}
	
	@Test
	public void verticalBar() {
		roundTripBNF("<vertical bar> ::= |");
	}
	
	
	@Test
	public void keyword() {
		// case-insensitive keywords need to spotted and converted
		// xml grammar requires productions to be different case-insensitively
		// this is what would have been produced
		roundTripBNF("<something> ::= <STOP>",
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
		roundTripBNF("<whitespace> ::= SPACE | TAB | LF | 0x1680 | 0x180e | 0x2000 | 0x2001");
	}


	@Test
	public void bnfProduction() {
		roundTripBNF("<prodassign> ::= ::=");
	}
	

	@Test
	public void bnfSymbolsProduction() {
		roundTripBNF("<notequal> ::= <>");
	}
	
	@Test
	public void commaProduction() {
		roundTripBNF("<comma> ::= ,");
	}
		
	@Test
	public void punctuation() {
		roundTripBNF("<puncs> ::= +.-");
	}
	@Test
	public void commasProduction() {
		roundTripBNF("<list> ::= ONE <comma> TWO <comma> THREE",
				"",
				"<comma> ::= ,");
	}
	
	@Test
	public void charset() {
		roundTripBNF("<anychars> ::= !! characterset 'ANY'");
	}
	
	// haven't made this one work
	@Test
	@Ignore
	public void charsetChoice() {
		roundTripBNF("<anychars> ::= !! characterset 'ID_Start'",
					"  |  !! characterset 'Pc'");
	}
	
	@Test
	public void shouldRecycleCypher() throws Exception
	{
//		BNFProcessor processor = new BNFProcessor();
		Grammar grammar = Fixture.grammarResource( BNFProcessor.class, "/cypher.xml");
//		String bnfFile = "C:/Users/Peter/gitg4bnf/antlr4-bnf-translator/grammars/cypher.bnf";
//		Grammar grammar = processor.processFile(bnfFile);
		// now reprocess 
		// not doing this at the moment because two of the rules get an unnecessary { } round a single item
		String firstBNF = makeSQLBNF(grammar);
		LOGGER.debug("Generated \n{}", firstBNF);
		// do we need a new one ?
		BNFProcessor secondProcessor = new BNFProcessor();
		Grammar grammarTwo = secondProcessor.processString(firstBNF);
		String intermediateBNF = makeSQLBNF(grammarTwo);
		Grammar grammarThree = secondProcessor.processString(intermediateBNF);
		String finalBNF = makeSQLBNF(grammarTwo);
		// can i eat my own dog food
		assertEquals(intermediateBNF, finalBNF);
		// but can i handle everything ?
		// not yet
//		assertEquals(firstBNF, intermediateBNF);
	}
	
	
	private void roundTripBNF(String... inputBnf) 
	{
		String inBnf = lines(inputBnf).trim();
		BNFProcessor processor = new BNFProcessor();
		LOGGER.debug("in {}", inBnf);
		Grammar grammar = processor.processString(lines(inputBnf));
		String outputBnf = makeSQLBNF(grammar);
		LOGGER.debug("out {}", outputBnf);
		assertEquals(unPretty(inBnf), unPretty(outputBnf));
	}

	private String makeSQLBNF(Grammar grammar) {
		StringWriter writer = new StringWriter();
		SQLBNF.write(grammar, writer);
		
		String outputBnf = writer.toString().trim();
		return outputBnf;
	}

	private String unPretty(String original) {
		String nl = System.lineSeparator();
		return original.replaceAll(nl + nl, "#!#").replaceAll("\\s+", " ").replaceAll("#!#", nl + nl);
	}

}
