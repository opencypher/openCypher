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

import static org.junit.Assert.assertEquals;
import static org.opencypher.tools.io.Output.lines;
import static org.opencypher.tools.io.Output.stringBuilder;
import static org.opencypher.grammar.Grammar.caseInsensitive;
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.grammar.Grammar.nonTerminal;
import static org.opencypher.grammar.Grammar.optional;
import static org.opencypher.grammar.Grammar.zeroOrMore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Grammar.Builder;
import org.opencypher.grammar.Grammar.CharacterSet;
import org.opencypher.tools.g4processors.BNFProcessor;
import org.opencypher.tools.g4processors.G4Processor;
import org.opencypher.tools.grammar.Antlr4;
import org.opencypher.tools.grammar.Xml;
import org.opencypher.tools.io.Output;
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
		roundTripBNFG4("<alpha> ::= ALPHA <beta>",
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
	public void charsetG4() {
		// passage through std grammar will lose any original fragment rule name
		roundTripG4("grammar four;",
				"",
				"four : FOUR_0 ;",
				"",
				"fragment FOUR_0 : [abcd] ;");
	}
	
	@Test
	public void charsetList() {
		roundTripG4(grammar( "test" )
		        .production( "test", charactersOfSet( "[abcd]" ) ).build());
	}
	
	// named chars sets aren't recognised second time round
	@Test
	public void charsetSimpleName() {
		roundTripG4((grammar( "test" )
		        .production( "test", charactersOfSet("TAB")).build()));
	}
	
	//  G4 serializer can't do these sensibly yet (or possibly even correctly)
	@Test
	public void negCharset() {
		roundTripG4(grammar( "test" )
		        .production( "test", charactersOfSet("ANY").except('a','b','c') ).build());
	}
	
	// this used not work, but I changed the g4 serialisation
	@Test
	public void charsetSpecialName() {
		roundTripG4((grammar( "test" )
		        .production( "test", charactersOfSet("ID_Start")).build()));
	}
	
	@Test
	public void charsetExceptBackslash() {
		roundTripG4(grammar( "test" )
		        .production( "test", charactersOfSet( "ANY" ).except('"','\\') )
		        .build());
	}
	
	// this used not work, but I changed the g4 serialisation
	@Test
	public void charsetChoiceBNF() {
		roundTripBNFG4("<anychars> ::= $ID_Start$ | $Pc$");
	}
	
	
	@Test
	public void unionFromXml() {
		String production = "  <production name=\"Union\">\n" + 
				"    <alt>\n" + 
				"      <seq>\n" + 
				"        <literal value=\"UNION\" case-sensitive=\"false\"/>\n" + 
				"        <literal value=\"ALL\" case-sensitive=\"false\"/>\n" + 
				"      </seq>\n" + 
				"        <literal value=\"UNION\" case-sensitive=\"false\"/>\n" + 
				"    </alt>\n" + 
				"  </production>";
		Grammar prodGrammar = xmlin(production);
		roundTripG4(prodGrammar);
	}
	

	@Test
	public void shouldRecycleFewRules() throws Exception
	{
		Grammar grammarFromXml = Fixture.grammarResource( G4Processor.class, "/FewRules.xml");
		//  LOGGER.debug("xml out\n{}", xmlout(grammarFromXml));
		// now process 
		String firstG4 = makeAntlr4(grammarFromXml);
		LOGGER.debug("Generated G4\n{}", firstG4);
		// do we need a new one ?
		G4Processor g4processor = new G4Processor();
		Grammar grammarFromG4 = g4processor.processString(firstG4);
		String intermediateG4 = makeAntlr4(grammarFromG4);
		LOGGER.debug("Regenerated G4\n{}", intermediateG4);
		Grammar grammarFromSecondGenG4 = g4processor.processString(intermediateG4);
		String finalG4 = makeAntlr4(grammarFromG4);
		// can i eat my own dog food
		assertEquals(intermediateG4, finalG4);
		// but can i handle everything ?
		// not yet
//		assertEquals(firstG4, intermediateG4);
	}
	
	
	@Test
	public void shouldRecycleCypher() throws Exception
	{
		Grammar grammarFromXml = Fixture.grammarResource( G4Processor.class, "/cypher.xml");
		//  LOGGER.debug("xml out\n{}", xmlout(grammarFromXml));
		// now process 
		String firstG4 = makeAntlr4(grammarFromXml);
		LOGGER.debug("Generated G4\n{}", firstG4);
		// do we need a new one ?
		G4Processor g4processor = new G4Processor();
		Grammar grammarFromG4 = g4processor.processString(firstG4);
		String intermediateG4 = makeAntlr4(grammarFromG4);
		LOGGER.debug("Regenerated G4\n{}", intermediateG4);
		Grammar grammarFromSecondGenG4 = g4processor.processString(intermediateG4);
		String finalG4 = makeAntlr4(grammarFromG4);
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
		//  LOGGER.debug("xml of input\n{}", xmlout(testGrammar));
		String firstG4 = makeAntlr4(testGrammar);
		LOGGER.debug("generated G4\n{}", firstG4);
		
		G4Processor processor = new G4Processor();
		Grammar grammar = processor.processString(firstG4);
		//  LOGGER.debug("xml from g4\n{}", xmlout(grammar));
		String outputG4 = makeAntlr4(grammar);
		LOGGER.debug("second g4\n{}", outputG4);
		assertEquals(unPretty(firstG4), unPretty(outputG4));
	}

	
	
	private void roundTripG4(String... inputG4) 
	{
		String inG4 = lines(inputG4).trim();
		G4Processor processor = new G4Processor();
		LOGGER.debug("in {}", inG4);
		Grammar grammar = processor.processString(lines(inputG4));
		//  LOGGER.debug("grammar from g4 is \n{}", xmlout(grammar));
		String outputG4 = makeAntlr4(grammar);
		LOGGER.debug("out {}", outputG4);
		assertEquals(unPretty(inG4), unPretty(outputG4));
	}

	private String xmlout(Grammar testGrammar) {
        // given
        Output.Readable out = stringBuilder();

        try {
			Xml.write( testGrammar, out );
			return out.toString();
		} catch (TransformerException e) {
			throw new IllegalStateException("Failed to create xml", e);
		}
	}

	private static final Pattern XMLANG_PATTERN = Pattern.compile("name=(?:'|\")(\\w+)(?:'|\\\")");
	private Grammar xmlin(String productions) {
		Matcher m = XMLANG_PATTERN.matcher(productions);
		if (m.find()) {
			String language = m.group(1);

			StringBuilder xb = new StringBuilder(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<grammar language=\"").append(language)
							.append("\" xmlns=\"http://opencypher.org/grammar\">\n").append(productions)
							.append("\n</grammar>");
			String xmlString = xb.toString();
			InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes(Charset.forName("UTF-8")));
			try {
				return Grammar.parseXML(inputStream);
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to parse xml\n" + xmlString, e);
			}
		} else {
			throw new IllegalArgumentException("Cannot find first production name in " + productions);
		}
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
