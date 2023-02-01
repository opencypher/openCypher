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
import static org.opencypher.grammar.Grammar.charactersOfSet;
import static org.opencypher.grammar.Grammar.grammar;
import static org.opencypher.grammar.Grammar.literal;
import static org.opencypher.tools.io.Output.lines;
import static org.opencypher.tools.io.Output.stringBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.opencypher.grammar.Fixture;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.g4processors.BNFProcessor;
import org.opencypher.tools.grammar.SQLBNF;
import org.opencypher.tools.grammar.Xml;
import org.opencypher.tools.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class BNFProcessorTest {

    public final @Rule Fixture fixture = new Fixture();

	private static final Logger LOGGER = LoggerFactory.getLogger(BNFProcessorTest.class.getName());
	
	@Test
	public void oneProduction() {
		roundTripBNF("<alpha> ::= ALPHA");
	}

	@Test
	public void twoProductions() {
		roundTripBNF("<alpha> ::= ALPHA <beta>",
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
		roundTripBNF("<thing> ::= <vertical bar>",
				"",
				"<vertical bar> ::= |");
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
	public void whitespace() {
		roundTripBNF("<whitespace> ::= \\u0020 | $TAB$ | $LF$ | \\u1680 | \\u180E | \\u2000 | \\u2001");
	}

	@Test
	public void exclamations() {
		roundTripBNF("<something> ::= !! something old",
				  "    | <newthing>",
				  "",
				  "<newthing> ::= NEW");
	}

	@Test
	public void leadingDescription() {
		// description on first prodn is tricky
		roundTripBNF(
					"<something> ::= <newthing>",
				  "",
				  "// this is one",
				  "<newthing> ::= NEW");
	}
	
	@Test
	public void leadingDescriptionLines() {
		// description on first prodn is tricky
		roundTripBNF(
					"<something> ::= <newthing>",
				  "",
				  "// this is line one",
				  "//     this is line two",
				  "//     this is line three",
				  "<newthing> ::= NEW");
	}
	
	@Test
	public void retainHeader() {
		// description on first prodn is tricky
		roundTripBNF("//",
					 "// an example header",
					 "//",
					 "",
					 "<something> ::= <newthing>",
				     "",
				     "// this is one",
				  "<newthing> ::= NEW");
	}
	
	@Test
	public void firstDescription() {
		// description on first prodn 
		roundTripBNF("// an example header",
					 "<something> ::= <newthing>",
				     "",
				     "// this is one",
				  "<newthing> ::= NEW");
	}
	
	@Test
	public void trailingDescription() {
		// can't use the normal round trip, because the description ends up differently
		String[] inputBnf = { "<something> ::= <newthing>", "// that might not work", "", "<newthing> ::= NEW" };
		String inBnf = lines(inputBnf).trim();
		BNFProcessor processor = new BNFProcessor();
		Grammar grammar = processor.processString(lines(inputBnf));
		String outputBnf = makeSQLBNF(grammar);
		String[] expected = { "// that might not work", "<something> ::= <newthing>",  "", "<newthing> ::= NEW" };
		String expect = lines(expected).trim();
		assertEquals(unPretty(expect), unPretty(outputBnf));
	}
	

	@Test
	public void bnfProduction() {
		// the sql writer can't tell that all of the rhs is bnf without more special work to 
		// set the production/@bnfsymbols attribute.  So it will unwind it
		roundTripBNF("<prodassign> ::= <assign>",
				"",
				"<assign> ::= ::=");
	}
	

	@Test
	public void bnfSymbolsProduction() {
		// original form of this <notequals> ::= <> requires detection of all bnf
		roundTrip(grammar( "notequals" )
		        .production( "notequals", literal( "<>" ) ).build());
	}
	
	@Test
	public void commaProduction() {
		roundTripBNF("<comma> ::= ,");
	}
		
	@Test
	public void punctuation() {
		roundTripBNF("<puncs> ::= +.*=");
	}
	
	// TODO minus sign is a problem - it is both punctuation and a hyphen in words
	//   probably need to call it out as a specialliteral and concatenate if surrounded by words
    @Test
    public void minus() {
    	roundTripBNF("<mmm> ::= a-");
    }
	
	@Test
	public void commasProduction() {
		roundTripBNF("<list> ::= ONE <comma> TWO <comma> THREE",
				"",
				"<comma> ::= ,");
	}
	
	@Test
	public void charsetList() {
		roundTrip(grammar( "test" )
		        .production( "test", charactersOfSet( "[abcd]" ) ).build());
	}
	
	@Test
	public void charsetName() {
		roundTrip(grammar( "test" )
		        .production( "test", charactersOfSet( "FF" ) ).build());
	}
	
	@Test
	public void charsetNameWithException() {
		roundTrip(grammar( "test" )
		        .production( "test", charactersOfSet( "Lu" ).except('X','Y') )
		        .build());
	}
	
	@Test
	public void charsetExceptBackslash() {
		roundTrip(grammar( "test" )
		        .production( "test", charactersOfSet( "ANY" ).except('"','\\') )
		        .build());
	}
	
	@Test
	public void charsetRT() {
		roundTripBNF("<namedCharset> ::= $FF$");
	}
	
	// haven't made this one work
	@Test
	public void charsetChoice() {
		roundTripBNF("<IdentifierStart> ::= $ID_Start$",
					"  |  $Pc$");
	}
	
	// this would require merging of the // literal together, which isn't easy
	@Ignore
	@Test
	public void commentInXML() throws Exception 
	{        
		Grammar grammar = xmlin("<production name=\"comment\">\n" + 
				"    <alt>      <seq>//\r\n" + 
				"        <repeat>\r\n" + 
				"          <character>\r\n" + 
				"            <except literal=\"&#10;\"/> <!-- Line Feed -->\r\n" + 
				"            <except literal=\"&#13;\"/> <!-- Carriage Return -->\r\n" + 
				"          </character>\r\n" + 
				"        </repeat>\r\n" + 
				"        <opt><literal value=\"&#13;\"/></opt> <!-- Carriage Return -->\r\n" + 
				"        <alt>\r\n" + 
				"          <literal value=\"&#10;\"/> <!-- Line Feed -->\r\n" + 
				"          <character set=\"EOI\"/>\r\n" + 
				"        </alt>\r\n" + 
				"      </seq>\r\n" + 
				"</alt>\n" + 
				"  </production>");

		roundTrip(grammar);
	}
	
	// next two ignored until the handling of letter rules is correct
	@Test
	public void xmlProduction() throws Exception 
	{        
		Grammar grammar = xmlin("<production name=\"alpha\">\n" + 
				"    <alt>a b c d e f g h i j k l m n o p q r s t u v x y z</alt>\n" + 
				"  </production>");

		roundTrip(grammar);
	}
	
	// the // constract (also tested by commentInXml) doesn't get reformatted as \u002F\u002F
	@Ignore
	@Test
	public void someGrammar() throws Exception 
	{        
		Grammar grammar = fixture.grammarResource( "/somegrammar.xml" ) ;

		roundTrip(grammar);
	}
	
	@Test
	public void doubleSlash() throws Exception 
	{        
		roundTrip(grammar( "test" )
		        .production( "test", literal("//") ).build());
	}

	@Test
	public void leftArrowHead() {
		roundTripBNF("<LeftArrowHead> ::= <less than>",
				"    |  \\u27E8", 
				"    |  \\u3008",
				"    |  \\uFE64",
				"    |  \\uFF1C ",
				"",
				"<less than> ::= <");
	}
	
	@Test
	public void shouldRecycleCypher() throws Exception
	{
		Grammar grammar = Fixture.grammarResource( BNFProcessor.class, "/cypher.xml");
		// not doing this at the moment because two of the rules get an unnecessary { } round a single item
		//  LOGGER.debug("Original grammar \n{}", xmlout(grammar));
		String firstBNF = makeSQLBNF(grammar);
		LOGGER.debug("Generated \n{}", firstBNF);
		// do we need a new one ?
		BNFProcessor secondProcessor = new BNFProcessor();
		Grammar grammarTwo = secondProcessor.processString(firstBNF);
		String intermediateBNF = makeSQLBNF(grammarTwo);
		LOGGER.debug("Regenerated\n{}", intermediateBNF);
		Grammar grammarThree = secondProcessor.processString(intermediateBNF);
		String finalBNF = makeSQLBNF(grammarTwo);
		// can i eat my own dog food
		assertEquals(intermediateBNF, finalBNF);
		// but can i handle everything ?
		// not yet - some things get wrapped in { }
//		assertEquals(firstBNF, intermediateBNF);
	}
	
	private void roundTrip(Grammar testGrammar) {
//		  LOGGER.warn("supplied grammar \n{}", xmlout(testGrammar));
		String firstBNF = makeSQLBNF(testGrammar);
		LOGGER.debug("in \n{}", firstBNF);
		
		BNFProcessor processor = new BNFProcessor();
		Grammar grammar = processor.processString(firstBNF);
//		  LOGGER.warn("grammar afer first pass through bnf\n{}", xmlout(grammar));

		String outputBNF = makeSQLBNF(grammar);
		LOGGER.debug("out \n{}", outputBNF);
		assertEquals(unPretty(firstBNF), unPretty(outputBNF));
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

	private void roundTripBNF(String... inputBnf) 
	{
		String inBnf = lines(inputBnf).trim();
		BNFProcessor processor = new BNFProcessor();
		LOGGER.debug("in {}", inBnf);
		Grammar grammar = processor.processString(lines(inputBnf));
//		LOGGER.warn("bnf read makes\n{}", xmlout(grammar));
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
		// special case line comments and !!
		original = original.replaceAll("\r", "");
		original = original.replaceAll("((?:!!|//).*?)\n", "$1@!@");
		original = original.replaceAll("\n\n", "#!#");
		original = original.replaceAll("\\s+", " ");
		original = original.replaceAll("@!@", "\n");
		original = original.replaceAll("#!#", "\n\n");
		original = original.replaceAll("\\s*\n\\s*", "\n");
		return original;
	}

}
