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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opencypher.tools.g4tree.BnfSymbols;
import org.opencypher.tools.g4tree.BnfSymbols.Interleaver;

public class BnfSymbolsTest {

	@Test
	public void allBnf() {
		assertTrue(BnfSymbols.allBnfSymbols("::="));
		assertTrue(BnfSymbols.allBnfSymbols("<"));
		assertTrue(BnfSymbols.allBnfSymbols(">"));
		assertTrue(BnfSymbols.allBnfSymbols("{"));
		assertTrue(BnfSymbols.allBnfSymbols("}"));
		assertTrue(BnfSymbols.allBnfSymbols("["));
		assertTrue(BnfSymbols.allBnfSymbols("]"));
		assertTrue(BnfSymbols.allBnfSymbols("|"));
		assertTrue(BnfSymbols.allBnfSymbols("..."));
		assertTrue(BnfSymbols.allBnfSymbols("$"));
		assertTrue(BnfSymbols.allBnfSymbols("!!"));
		assertTrue(BnfSymbols.allBnfSymbols("::=$"));
		assertTrue(BnfSymbols.allBnfSymbols("[][][}"));
		assertFalse(BnfSymbols.allBnfSymbols("("));
		assertFalse(BnfSymbols.allBnfSymbols("{("));
		assertFalse(BnfSymbols.allBnfSymbols("good[9]"));
	}
	
	@Test
	public void anyBnf() {
		assertTrue(BnfSymbols.anyBnfSymbols("this has $ in it"));
		assertTrue(BnfSymbols.anyBnfSymbols("$"));
		assertTrue(BnfSymbols.anyBnfSymbols("and this { has more } thane[one"));
		assertTrue(BnfSymbols.anyBnfSymbols("::=$"));
		assertFalse(BnfSymbols.anyBnfSymbols("but this doesn;t hav any"));
		assertFalse(BnfSymbols.anyBnfSymbols("o his \"£%^&*()_but this doesn;t hav any"));
		assertFalse(BnfSymbols.anyBnfSymbols(".. .. .. .. "));
		
	}
	
	@Test
	public void interleaver() {
		testInterleaver("tt","<", "then" , "..." ,"or");
		testInterleaver("","<", "then" , "..." ,"or","}","");
		testInterleaver("and","::=","", "$", "", "<", "=", ">","");
		testInterleaver("tt","<", "then" , "..." ,"or");
		testInterleaver("tt","<", "then" , "..." ,"or");

	}

	private void testInterleaver(String... input) {
		// input must start and finish with (possibly zero length) non-symbols, and interleave
		// and interleave
		assertFalse(BnfSymbols.anyBnfSymbols(input[0]));
		// also must interleave
		assertFalse(BnfSymbols.anyBnfSymbols(input[input.length-1]));
		String testString = Arrays.asList( input).stream().collect(Collectors.joining());
		System.out.println(testString);
		Interleaver interleaver = BnfSymbols.getInterleave(testString);
		List<String> result = new ArrayList<>();
		while (interleaver.hasNext()) {
			result.add(interleaver.nextText());
			result.add(interleaver.nextSymbol().getActualCharacters());
		}
		result.add(interleaver.nextText());
		assertEquals(Arrays.asList( input), result);
	}


}
