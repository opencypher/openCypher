package org.opencypher.tools.antlr.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opencypher.tools.antlr.tree.BnfSymbols.Interleaver;

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
