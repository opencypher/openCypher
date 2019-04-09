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
// Generated from FeatureResults.g4 by ANTLR 4.7
package org.opencypher.tools.tck.parsing.generated;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FeatureResultsLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		INTEGER_LITERAL=18, DECIMAL_LITERAL=19, DIGIT=20, NONZERODIGIT=21, INFINITY=22, 
		FLOAT_LITERAL=23, FLOAT_REPR=24, EXPONENTPART=25, SYMBOLIC_NAME=26, WS=27, 
		IDENTIFIER=28, STRING_LITERAL=29, STRING_BODY=30, ESCAPED_APOSTROPHE=31;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
		"INTEGER_LITERAL", "DECIMAL_LITERAL", "DIGIT", "NONZERODIGIT", "INFINITY", 
		"FLOAT_LITERAL", "FLOAT_REPR", "EXPONENTPART", "SYMBOLIC_NAME", "WS", 
		"IDENTIFIER", "STRING_LITERAL", "STRING_BODY", "ESCAPED_APOSTROPHE"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "'['", "']'", "'<'", "'>'", "'-'", "'->'", "'<-'", 
		"'true'", "'false'", "'null'", "', '", "'{'", "'}'", "'`'", "':'", null, 
		null, null, null, null, null, null, null, null, "' '", null, null, null, 
		"'\\''"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, "INTEGER_LITERAL", "DECIMAL_LITERAL", 
		"DIGIT", "NONZERODIGIT", "INFINITY", "FLOAT_LITERAL", "FLOAT_REPR", "EXPONENTPART", 
		"SYMBOLIC_NAME", "WS", "IDENTIFIER", "STRING_LITERAL", "STRING_BODY", 
		"ESCAPED_APOSTROPHE"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public FeatureResultsLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "FeatureResults.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2!\u00d7\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \3\2"+
		"\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\n\3"+
		"\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3"+
		"\r\3\r\3\16\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\5\23"+
		"r\n\23\3\23\3\23\3\24\3\24\3\24\7\24y\n\24\f\24\16\24|\13\24\5\24~\n\24"+
		"\3\25\3\25\5\25\u0082\n\25\3\26\3\26\3\27\5\27\u0087\n\27\3\27\3\27\3"+
		"\27\3\27\3\30\5\30\u008e\n\30\3\30\3\30\3\31\6\31\u0093\n\31\r\31\16\31"+
		"\u0094\3\31\3\31\6\31\u0099\n\31\r\31\16\31\u009a\3\31\5\31\u009e\n\31"+
		"\3\31\3\31\6\31\u00a2\n\31\r\31\16\31\u00a3\3\31\5\31\u00a7\n\31\3\31"+
		"\3\31\3\31\3\31\6\31\u00ad\n\31\r\31\16\31\u00ae\3\31\5\31\u00b2\n\31"+
		"\5\31\u00b4\n\31\3\32\3\32\5\32\u00b8\n\32\3\32\6\32\u00bb\n\32\r\32\16"+
		"\32\u00bc\3\33\3\33\3\34\3\34\3\35\6\35\u00c4\n\35\r\35\16\35\u00c5\3"+
		"\36\3\36\7\36\u00ca\n\36\f\36\16\36\u00cd\13\36\3\36\3\36\3\37\3\37\5"+
		"\37\u00d3\n\37\3 \3 \3 \2\2!\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13"+
		"\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61"+
		"\32\63\33\65\34\67\359\36;\37= ?!\3\2\7\3\2\63;\4\2GGgg\4\2--//\7\2&&"+
		"\62;C\\aac|\4\2\2(*\u0201\2\u00eb\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2"+
		"\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3"+
		"\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2"+
		"\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2"+
		"\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2"+
		"\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\3A\3\2\2"+
		"\2\5C\3\2\2\2\7E\3\2\2\2\tG\3\2\2\2\13I\3\2\2\2\rK\3\2\2\2\17M\3\2\2\2"+
		"\21O\3\2\2\2\23R\3\2\2\2\25U\3\2\2\2\27Z\3\2\2\2\31`\3\2\2\2\33e\3\2\2"+
		"\2\35h\3\2\2\2\37j\3\2\2\2!l\3\2\2\2#n\3\2\2\2%q\3\2\2\2\'}\3\2\2\2)\u0081"+
		"\3\2\2\2+\u0083\3\2\2\2-\u0086\3\2\2\2/\u008d\3\2\2\2\61\u00b3\3\2\2\2"+
		"\63\u00b5\3\2\2\2\65\u00be\3\2\2\2\67\u00c0\3\2\2\29\u00c3\3\2\2\2;\u00c7"+
		"\3\2\2\2=\u00d2\3\2\2\2?\u00d4\3\2\2\2AB\7*\2\2B\4\3\2\2\2CD\7+\2\2D\6"+
		"\3\2\2\2EF\7]\2\2F\b\3\2\2\2GH\7_\2\2H\n\3\2\2\2IJ\7>\2\2J\f\3\2\2\2K"+
		"L\7@\2\2L\16\3\2\2\2MN\7/\2\2N\20\3\2\2\2OP\7/\2\2PQ\7@\2\2Q\22\3\2\2"+
		"\2RS\7>\2\2ST\7/\2\2T\24\3\2\2\2UV\7v\2\2VW\7t\2\2WX\7w\2\2XY\7g\2\2Y"+
		"\26\3\2\2\2Z[\7h\2\2[\\\7c\2\2\\]\7n\2\2]^\7u\2\2^_\7g\2\2_\30\3\2\2\2"+
		"`a\7p\2\2ab\7w\2\2bc\7n\2\2cd\7n\2\2d\32\3\2\2\2ef\7.\2\2fg\7\"\2\2g\34"+
		"\3\2\2\2hi\7}\2\2i\36\3\2\2\2jk\7\177\2\2k \3\2\2\2lm\7b\2\2m\"\3\2\2"+
		"\2no\7<\2\2o$\3\2\2\2pr\7/\2\2qp\3\2\2\2qr\3\2\2\2rs\3\2\2\2st\5\'\24"+
		"\2t&\3\2\2\2u~\7\62\2\2vz\5+\26\2wy\5)\25\2xw\3\2\2\2y|\3\2\2\2zx\3\2"+
		"\2\2z{\3\2\2\2{~\3\2\2\2|z\3\2\2\2}u\3\2\2\2}v\3\2\2\2~(\3\2\2\2\177\u0082"+
		"\7\62\2\2\u0080\u0082\5+\26\2\u0081\177\3\2\2\2\u0081\u0080\3\2\2\2\u0082"+
		"*\3\2\2\2\u0083\u0084\t\2\2\2\u0084,\3\2\2\2\u0085\u0087\7/\2\2\u0086"+
		"\u0085\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u0089\7K"+
		"\2\2\u0089\u008a\7p\2\2\u008a\u008b\7h\2\2\u008b.\3\2\2\2\u008c\u008e"+
		"\7/\2\2\u008d\u008c\3\2\2\2\u008d\u008e\3\2\2\2\u008e\u008f\3\2\2\2\u008f"+
		"\u0090\5\61\31\2\u0090\60\3\2\2\2\u0091\u0093\5)\25\2\u0092\u0091\3\2"+
		"\2\2\u0093\u0094\3\2\2\2\u0094\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095"+
		"\u0096\3\2\2\2\u0096\u0098\7\60\2\2\u0097\u0099\5)\25\2\u0098\u0097\3"+
		"\2\2\2\u0099\u009a\3\2\2\2\u009a\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b"+
		"\u009d\3\2\2\2\u009c\u009e\5\63\32\2\u009d\u009c\3\2\2\2\u009d\u009e\3"+
		"\2\2\2\u009e\u00b4\3\2\2\2\u009f\u00a1\7\60\2\2\u00a0\u00a2\5)\25\2\u00a1"+
		"\u00a0\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00a1\3\2\2\2\u00a3\u00a4\3\2"+
		"\2\2\u00a4\u00a6\3\2\2\2\u00a5\u00a7\5\63\32\2\u00a6\u00a5\3\2\2\2\u00a6"+
		"\u00a7\3\2\2\2\u00a7\u00b4\3\2\2\2\u00a8\u00a9\5)\25\2\u00a9\u00aa\5\63"+
		"\32\2\u00aa\u00b4\3\2\2\2\u00ab\u00ad\5)\25\2\u00ac\u00ab\3\2\2\2\u00ad"+
		"\u00ae\3\2\2\2\u00ae\u00ac\3\2\2\2\u00ae\u00af\3\2\2\2\u00af\u00b1\3\2"+
		"\2\2\u00b0\u00b2\5\63\32\2\u00b1\u00b0\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2"+
		"\u00b4\3\2\2\2\u00b3\u0092\3\2\2\2\u00b3\u009f\3\2\2\2\u00b3\u00a8\3\2"+
		"\2\2\u00b3\u00ac\3\2\2\2\u00b4\62\3\2\2\2\u00b5\u00b7\t\3\2\2\u00b6\u00b8"+
		"\t\4\2\2\u00b7\u00b6\3\2\2\2\u00b7\u00b8\3\2\2\2\u00b8\u00ba\3\2\2\2\u00b9"+
		"\u00bb\5)\25\2\u00ba\u00b9\3\2\2\2\u00bb\u00bc\3\2\2\2\u00bc\u00ba\3\2"+
		"\2\2\u00bc\u00bd\3\2\2\2\u00bd\64\3\2\2\2\u00be\u00bf\59\35\2\u00bf\66"+
		"\3\2\2\2\u00c0\u00c1\7\"\2\2\u00c18\3\2\2\2\u00c2\u00c4\t\5\2\2\u00c3"+
		"\u00c2\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\u00c3\3\2\2\2\u00c5\u00c6\3\2"+
		"\2\2\u00c6:\3\2\2\2\u00c7\u00cb\7)\2\2\u00c8\u00ca\5=\37\2\u00c9\u00c8"+
		"\3\2\2\2\u00ca\u00cd\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc"+
		"\u00ce\3\2\2\2\u00cd\u00cb\3\2\2\2\u00ce\u00cf\7)\2\2\u00cf<\3\2\2\2\u00d0"+
		"\u00d3\t\6\2\2\u00d1\u00d3\5? \2\u00d2\u00d0\3\2\2\2\u00d2\u00d1\3\2\2"+
		"\2\u00d3>\3\2\2\2\u00d4\u00d5\7^\2\2\u00d5\u00d6\7)\2\2\u00d6@\3\2\2\2"+
		"\26\2qz}\u0081\u0086\u008d\u0094\u009a\u009d\u00a3\u00a6\u00ae\u00b1\u00b3"+
		"\u00b7\u00bc\u00c5\u00cb\u00d2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}