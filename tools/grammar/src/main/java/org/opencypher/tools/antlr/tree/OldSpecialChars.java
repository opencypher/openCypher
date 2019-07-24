package org.opencypher.tools.antlr.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opencypher.tools.grammar.CharLit;

/**
 * characters or groups of characters that need special treatment in BNF, G4 or both
 */
public enum OldSpecialChars {
	// real bnf symbols
	//   bnf "escapes" by declaring an element whose right hand side is only bnfsymbols
	//      9075-2 doesn't entirely obey this, as <greater than operator> ::= >=
	//	ASSIGN | LBRACE | RBRACE | LEND | REND | BAR | GT | LT | ELLIPSIS 
	ASSIGN("::="),
	LEFT_BRACE("{"),
	RIGHT_BRACE("}"),
	LEFT_BRACKET("["),
	RIGHT_BRACKET("["),
	ELLIPSIS("..."),
	DOUBLE_EXCLAM("!!","\\!\\!"),
	DOLLAR("$")   // used for charsets
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

	
	private static final Map<String, OldSpecialChars> charMap;
	private static final Map<String, OldSpecialChars> bnfNameMap;
//	private static final Set<String> punctuation;
	private static final Pattern bnfPattern;

	static {
		charMap = new HashMap<>();
		bnfNameMap = new HashMap<>();
//		punctuation = new HashSet<>();
		List<String> bnfSyms = new ArrayList<>();
		
		for (OldSpecialChars lit : OldSpecialChars.values()) {
			charMap.put(lit.actualCharacters, lit);
			bnfNameMap.put(lit.name(), lit);
    			// a special literal may have been escaped 
    			//  (arguably, this depends on the input language
    			charMap.put(lit.bnfForm,  lit);
//			for (String character : lit.actualCharacters.split("")) {
//				punctuation.add(character);
//			}

		}
		// make a pattern for determining all of a string is bnf symbols
		bnfPattern = Pattern.compile(bnfSyms.stream().collect(Collectors.joining("|")));
	}
	
	OldSpecialChars(String characters) {
		this(characters, null);
	}

	OldSpecialChars(String characters, String escapedCharacters)
	{
		
		this.actualCharacters = characters;
		this.bnfForm =  (escapedCharacters != null) ? escapedCharacters : characters;

			// bnfName is lower case with spaces in angles
		this.bnfName = name().toLowerCase().replaceAll("_", " ");
		
		// g4name is as is (uppercase with _) - this is a lexer rule
		g4Name = name();

	}

	public static OldSpecialChars getByValue(String characters) {
		return charMap.get(characters);
	}
	
	public static OldSpecialChars getByName(String bnfName) {
		return bnfNameMap.get(bnfName);
	}
	
	public static boolean allBnfSymbols(String subject) 
	{
		Matcher m = bnfPattern.matcher(subject);
		int start = 0;
		while (m.find()) {
			if (m.start() != start) {
				return false;
			}
			start = m.end();
		}
		return start == subject.length();
	}
}
