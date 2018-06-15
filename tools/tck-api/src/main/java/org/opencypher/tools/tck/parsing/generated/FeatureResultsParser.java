/*
 * Copyright (c) 2015-2018 "Neo Technology,"
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
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FeatureResultsParser extends Parser {
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
	public static final int
		RULE_value = 0, RULE_node = 1, RULE_nodeDesc = 2, RULE_relationship = 3, 
		RULE_relationshipDesc = 4, RULE_path = 5, RULE_pathBody = 6, RULE_pathLink = 7, 
		RULE_forwardsRelationship = 8, RULE_backwardsRelationship = 9, RULE_integer = 10, 
		RULE_floatingPoint = 11, RULE_bool = 12, RULE_nullValue = 13, RULE_list = 14, 
		RULE_listContents = 15, RULE_listElement = 16, RULE_map = 17, RULE_propertyMap = 18, 
		RULE_mapContents = 19, RULE_keyValuePair = 20, RULE_propertyKey = 21, 
		RULE_propertyValue = 22, RULE_relationshipType = 23, RULE_relationshipTypeName = 24, 
		RULE_label = 25, RULE_labelName = 26, RULE_string = 27;
	public static final String[] ruleNames = {
		"value", "node", "nodeDesc", "relationship", "relationshipDesc", "path", 
		"pathBody", "pathLink", "forwardsRelationship", "backwardsRelationship", 
		"integer", "floatingPoint", "bool", "nullValue", "list", "listContents", 
		"listElement", "map", "propertyMap", "mapContents", "keyValuePair", "propertyKey", 
		"propertyValue", "relationshipType", "relationshipTypeName", "label", 
		"labelName", "string"
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

	@Override
	public String getGrammarFileName() { return "FeatureResults.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public FeatureResultsParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ValueContext extends ParserRuleContext {
		public NodeContext node() {
			return getRuleContext(NodeContext.class,0);
		}
		public RelationshipContext relationship() {
			return getRuleContext(RelationshipContext.class,0);
		}
		public PathContext path() {
			return getRuleContext(PathContext.class,0);
		}
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public FloatingPointContext floatingPoint() {
			return getRuleContext(FloatingPointContext.class,0);
		}
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public BoolContext bool() {
			return getRuleContext(BoolContext.class,0);
		}
		public NullValueContext nullValue() {
			return getRuleContext(NullValueContext.class,0);
		}
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public MapContext map() {
			return getRuleContext(MapContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_value);
		try {
			setState(66);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(56);
				node();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(57);
				relationship();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(58);
				path();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(59);
				integer();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(60);
				floatingPoint();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(61);
				string();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(62);
				bool();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(63);
				nullValue();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(64);
				list();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(65);
				map();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeContext extends ParserRuleContext {
		public NodeDescContext nodeDesc() {
			return getRuleContext(NodeDescContext.class,0);
		}
		public NodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_node; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitNode(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitNode(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NodeContext node() throws RecognitionException {
		NodeContext _localctx = new NodeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_node);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68);
			nodeDesc();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeDescContext extends ParserRuleContext {
		public List<LabelContext> label() {
			return getRuleContexts(LabelContext.class);
		}
		public LabelContext label(int i) {
			return getRuleContext(LabelContext.class,i);
		}
		public TerminalNode WS() { return getToken(FeatureResultsParser.WS, 0); }
		public PropertyMapContext propertyMap() {
			return getRuleContext(PropertyMapContext.class,0);
		}
		public NodeDescContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeDesc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterNodeDesc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitNodeDesc(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitNodeDesc(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NodeDescContext nodeDesc() throws RecognitionException {
		NodeDescContext _localctx = new NodeDescContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_nodeDesc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			match(T__0);
			setState(74);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__16) {
				{
				{
				setState(71);
				label();
				}
				}
				setState(76);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(77);
				match(WS);
				}
			}

			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__13) {
				{
				setState(80);
				propertyMap();
				}
			}

			setState(83);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationshipContext extends ParserRuleContext {
		public RelationshipDescContext relationshipDesc() {
			return getRuleContext(RelationshipDescContext.class,0);
		}
		public RelationshipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationship; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterRelationship(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitRelationship(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitRelationship(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationshipContext relationship() throws RecognitionException {
		RelationshipContext _localctx = new RelationshipContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_relationship);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			relationshipDesc();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationshipDescContext extends ParserRuleContext {
		public RelationshipTypeContext relationshipType() {
			return getRuleContext(RelationshipTypeContext.class,0);
		}
		public TerminalNode WS() { return getToken(FeatureResultsParser.WS, 0); }
		public PropertyMapContext propertyMap() {
			return getRuleContext(PropertyMapContext.class,0);
		}
		public RelationshipDescContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationshipDesc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterRelationshipDesc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitRelationshipDesc(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitRelationshipDesc(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationshipDescContext relationshipDesc() throws RecognitionException {
		RelationshipDescContext _localctx = new RelationshipDescContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_relationshipDesc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(T__2);
			setState(88);
			relationshipType();
			setState(90);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(89);
				match(WS);
				}
			}

			setState(93);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__13) {
				{
				setState(92);
				propertyMap();
				}
			}

			setState(95);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathContext extends ParserRuleContext {
		public PathBodyContext pathBody() {
			return getRuleContext(PathBodyContext.class,0);
		}
		public PathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitPath(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitPath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathContext path() throws RecognitionException {
		PathContext _localctx = new PathContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_path);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			match(T__4);
			setState(98);
			pathBody();
			setState(99);
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathBodyContext extends ParserRuleContext {
		public NodeDescContext nodeDesc() {
			return getRuleContext(NodeDescContext.class,0);
		}
		public List<PathLinkContext> pathLink() {
			return getRuleContexts(PathLinkContext.class);
		}
		public PathLinkContext pathLink(int i) {
			return getRuleContext(PathLinkContext.class,i);
		}
		public PathBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterPathBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitPathBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitPathBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathBodyContext pathBody() throws RecognitionException {
		PathBodyContext _localctx = new PathBodyContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_pathBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			nodeDesc();
			setState(105);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__6 || _la==T__8) {
				{
				{
				setState(102);
				pathLink();
				}
				}
				setState(107);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathLinkContext extends ParserRuleContext {
		public NodeDescContext nodeDesc() {
			return getRuleContext(NodeDescContext.class,0);
		}
		public ForwardsRelationshipContext forwardsRelationship() {
			return getRuleContext(ForwardsRelationshipContext.class,0);
		}
		public BackwardsRelationshipContext backwardsRelationship() {
			return getRuleContext(BackwardsRelationshipContext.class,0);
		}
		public PathLinkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathLink; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterPathLink(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitPathLink(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitPathLink(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathLinkContext pathLink() throws RecognitionException {
		PathLinkContext _localctx = new PathLinkContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_pathLink);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__6:
				{
				setState(108);
				forwardsRelationship();
				}
				break;
			case T__8:
				{
				setState(109);
				backwardsRelationship();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(112);
			nodeDesc();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForwardsRelationshipContext extends ParserRuleContext {
		public RelationshipDescContext relationshipDesc() {
			return getRuleContext(RelationshipDescContext.class,0);
		}
		public ForwardsRelationshipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forwardsRelationship; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterForwardsRelationship(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitForwardsRelationship(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitForwardsRelationship(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForwardsRelationshipContext forwardsRelationship() throws RecognitionException {
		ForwardsRelationshipContext _localctx = new ForwardsRelationshipContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_forwardsRelationship);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(114);
			match(T__6);
			setState(115);
			relationshipDesc();
			setState(116);
			match(T__7);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BackwardsRelationshipContext extends ParserRuleContext {
		public RelationshipDescContext relationshipDesc() {
			return getRuleContext(RelationshipDescContext.class,0);
		}
		public BackwardsRelationshipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_backwardsRelationship; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterBackwardsRelationship(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitBackwardsRelationship(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitBackwardsRelationship(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BackwardsRelationshipContext backwardsRelationship() throws RecognitionException {
		BackwardsRelationshipContext _localctx = new BackwardsRelationshipContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_backwardsRelationship);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118);
			match(T__8);
			setState(119);
			relationshipDesc();
			setState(120);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntegerContext extends ParserRuleContext {
		public TerminalNode INTEGER_LITERAL() { return getToken(FeatureResultsParser.INTEGER_LITERAL, 0); }
		public IntegerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterInteger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitInteger(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitInteger(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntegerContext integer() throws RecognitionException {
		IntegerContext _localctx = new IntegerContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_integer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			match(INTEGER_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FloatingPointContext extends ParserRuleContext {
		public TerminalNode FLOAT_LITERAL() { return getToken(FeatureResultsParser.FLOAT_LITERAL, 0); }
		public TerminalNode INFINITY() { return getToken(FeatureResultsParser.INFINITY, 0); }
		public FloatingPointContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatingPoint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterFloatingPoint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitFloatingPoint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitFloatingPoint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FloatingPointContext floatingPoint() throws RecognitionException {
		FloatingPointContext _localctx = new FloatingPointContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_floatingPoint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			_la = _input.LA(1);
			if ( !(_la==INFINITY || _la==FLOAT_LITERAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BoolContext extends ParserRuleContext {
		public BoolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bool; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterBool(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitBool(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitBool(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BoolContext bool() throws RecognitionException {
		BoolContext _localctx = new BoolContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_bool);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			_la = _input.LA(1);
			if ( !(_la==T__9 || _la==T__10) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NullValueContext extends ParserRuleContext {
		public NullValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterNullValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitNullValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitNullValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NullValueContext nullValue() throws RecognitionException {
		NullValueContext _localctx = new NullValueContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_nullValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			match(T__11);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListContext extends ParserRuleContext {
		public ListContentsContext listContents() {
			return getRuleContext(ListContentsContext.class,0);
		}
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			match(T__2);
			setState(132);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__2) | (1L << T__4) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__13) | (1L << INTEGER_LITERAL) | (1L << INFINITY) | (1L << FLOAT_LITERAL) | (1L << STRING_LITERAL))) != 0)) {
				{
				setState(131);
				listContents();
				}
			}

			setState(134);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListContentsContext extends ParserRuleContext {
		public List<ListElementContext> listElement() {
			return getRuleContexts(ListElementContext.class);
		}
		public ListElementContext listElement(int i) {
			return getRuleContext(ListElementContext.class,i);
		}
		public ListContentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listContents; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterListContents(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitListContents(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitListContents(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListContentsContext listContents() throws RecognitionException {
		ListContentsContext _localctx = new ListContentsContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_listContents);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			listElement();
			setState(141);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__12) {
				{
				{
				setState(137);
				match(T__12);
				setState(138);
				listElement();
				}
				}
				setState(143);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListElementContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public ListElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listElement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterListElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitListElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitListElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListElementContext listElement() throws RecognitionException {
		ListElementContext _localctx = new ListElementContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_listElement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MapContext extends ParserRuleContext {
		public PropertyMapContext propertyMap() {
			return getRuleContext(PropertyMapContext.class,0);
		}
		public MapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_map; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterMap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitMap(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitMap(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MapContext map() throws RecognitionException {
		MapContext _localctx = new MapContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_map);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			propertyMap();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyMapContext extends ParserRuleContext {
		public MapContentsContext mapContents() {
			return getRuleContext(MapContentsContext.class,0);
		}
		public PropertyMapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyMap; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterPropertyMap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitPropertyMap(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitPropertyMap(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyMapContext propertyMap() throws RecognitionException {
		PropertyMapContext _localctx = new PropertyMapContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_propertyMap);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148);
			match(T__13);
			setState(150);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__15 || _la==SYMBOLIC_NAME) {
				{
				setState(149);
				mapContents();
				}
			}

			setState(152);
			match(T__14);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MapContentsContext extends ParserRuleContext {
		public List<KeyValuePairContext> keyValuePair() {
			return getRuleContexts(KeyValuePairContext.class);
		}
		public KeyValuePairContext keyValuePair(int i) {
			return getRuleContext(KeyValuePairContext.class,i);
		}
		public MapContentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapContents; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterMapContents(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitMapContents(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitMapContents(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MapContentsContext mapContents() throws RecognitionException {
		MapContentsContext _localctx = new MapContentsContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_mapContents);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			keyValuePair();
			setState(159);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__12) {
				{
				{
				setState(155);
				match(T__12);
				setState(156);
				keyValuePair();
				}
				}
				setState(161);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyValuePairContext extends ParserRuleContext {
		public PropertyValueContext propertyValue() {
			return getRuleContext(PropertyValueContext.class,0);
		}
		public PropertyKeyContext propertyKey() {
			return getRuleContext(PropertyKeyContext.class,0);
		}
		public TerminalNode WS() { return getToken(FeatureResultsParser.WS, 0); }
		public KeyValuePairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyValuePair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterKeyValuePair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitKeyValuePair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitKeyValuePair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyValuePairContext keyValuePair() throws RecognitionException {
		KeyValuePairContext _localctx = new KeyValuePairContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_keyValuePair);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SYMBOLIC_NAME:
				{
				setState(162);
				propertyKey();
				}
				break;
			case T__15:
				{
				{
				setState(163);
				match(T__15);
				setState(164);
				propertyKey();
				setState(165);
				match(T__15);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(169);
			match(T__16);
			setState(171);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WS) {
				{
				setState(170);
				match(WS);
				}
			}

			setState(173);
			propertyValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyKeyContext extends ParserRuleContext {
		public TerminalNode SYMBOLIC_NAME() { return getToken(FeatureResultsParser.SYMBOLIC_NAME, 0); }
		public PropertyKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterPropertyKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitPropertyKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitPropertyKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyKeyContext propertyKey() throws RecognitionException {
		PropertyKeyContext _localctx = new PropertyKeyContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_propertyKey);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			match(SYMBOLIC_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyValueContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public PropertyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterPropertyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitPropertyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitPropertyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyValueContext propertyValue() throws RecognitionException {
		PropertyValueContext _localctx = new PropertyValueContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_propertyValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(177);
			value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationshipTypeContext extends ParserRuleContext {
		public RelationshipTypeNameContext relationshipTypeName() {
			return getRuleContext(RelationshipTypeNameContext.class,0);
		}
		public RelationshipTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationshipType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterRelationshipType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitRelationshipType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitRelationshipType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationshipTypeContext relationshipType() throws RecognitionException {
		RelationshipTypeContext _localctx = new RelationshipTypeContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_relationshipType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			match(T__16);
			setState(185);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SYMBOLIC_NAME:
				{
				setState(180);
				relationshipTypeName();
				}
				break;
			case T__15:
				{
				{
				setState(181);
				match(T__15);
				setState(182);
				relationshipTypeName();
				setState(183);
				match(T__15);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationshipTypeNameContext extends ParserRuleContext {
		public TerminalNode SYMBOLIC_NAME() { return getToken(FeatureResultsParser.SYMBOLIC_NAME, 0); }
		public RelationshipTypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationshipTypeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterRelationshipTypeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitRelationshipTypeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitRelationshipTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationshipTypeNameContext relationshipTypeName() throws RecognitionException {
		RelationshipTypeNameContext _localctx = new RelationshipTypeNameContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_relationshipTypeName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			match(SYMBOLIC_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LabelContext extends ParserRuleContext {
		public LabelNameContext labelName() {
			return getRuleContext(LabelNameContext.class,0);
		}
		public LabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_label; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitLabel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LabelContext label() throws RecognitionException {
		LabelContext _localctx = new LabelContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_label);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			match(T__16);
			setState(195);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SYMBOLIC_NAME:
				{
				setState(190);
				labelName();
				}
				break;
			case T__15:
				{
				{
				setState(191);
				match(T__15);
				setState(192);
				labelName();
				setState(193);
				match(T__15);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LabelNameContext extends ParserRuleContext {
		public TerminalNode SYMBOLIC_NAME() { return getToken(FeatureResultsParser.SYMBOLIC_NAME, 0); }
		public LabelNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_labelName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterLabelName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitLabelName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitLabelName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LabelNameContext labelName() throws RecognitionException {
		LabelNameContext _localctx = new LabelNameContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_labelName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(SYMBOLIC_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(FeatureResultsParser.STRING_LITERAL, 0); }
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FeatureResultsListener ) ((FeatureResultsListener)listener).exitString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof FeatureResultsVisitor ) return ((FeatureResultsVisitor<? extends T>)visitor).visitString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_string);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(199);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3!\u00cc\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3"+
		"\2\3\2\3\2\5\2E\n\2\3\3\3\3\3\4\3\4\7\4K\n\4\f\4\16\4N\13\4\3\4\5\4Q\n"+
		"\4\3\4\5\4T\n\4\3\4\3\4\3\5\3\5\3\6\3\6\3\6\5\6]\n\6\3\6\5\6`\n\6\3\6"+
		"\3\6\3\7\3\7\3\7\3\7\3\b\3\b\7\bj\n\b\f\b\16\bm\13\b\3\t\3\t\5\tq\n\t"+
		"\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16"+
		"\3\17\3\17\3\20\3\20\5\20\u0087\n\20\3\20\3\20\3\21\3\21\3\21\7\21\u008e"+
		"\n\21\f\21\16\21\u0091\13\21\3\22\3\22\3\23\3\23\3\24\3\24\5\24\u0099"+
		"\n\24\3\24\3\24\3\25\3\25\3\25\7\25\u00a0\n\25\f\25\16\25\u00a3\13\25"+
		"\3\26\3\26\3\26\3\26\3\26\5\26\u00aa\n\26\3\26\3\26\5\26\u00ae\n\26\3"+
		"\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u00bc"+
		"\n\31\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u00c6\n\33\3\34\3\34"+
		"\3\35\3\35\3\35\2\2\36\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,"+
		".\60\62\64\668\2\4\3\2\30\31\3\2\f\r\2\u00c7\2D\3\2\2\2\4F\3\2\2\2\6H"+
		"\3\2\2\2\bW\3\2\2\2\nY\3\2\2\2\fc\3\2\2\2\16g\3\2\2\2\20p\3\2\2\2\22t"+
		"\3\2\2\2\24x\3\2\2\2\26|\3\2\2\2\30~\3\2\2\2\32\u0080\3\2\2\2\34\u0082"+
		"\3\2\2\2\36\u0084\3\2\2\2 \u008a\3\2\2\2\"\u0092\3\2\2\2$\u0094\3\2\2"+
		"\2&\u0096\3\2\2\2(\u009c\3\2\2\2*\u00a9\3\2\2\2,\u00b1\3\2\2\2.\u00b3"+
		"\3\2\2\2\60\u00b5\3\2\2\2\62\u00bd\3\2\2\2\64\u00bf\3\2\2\2\66\u00c7\3"+
		"\2\2\28\u00c9\3\2\2\2:E\5\4\3\2;E\5\b\5\2<E\5\f\7\2=E\5\26\f\2>E\5\30"+
		"\r\2?E\58\35\2@E\5\32\16\2AE\5\34\17\2BE\5\36\20\2CE\5$\23\2D:\3\2\2\2"+
		"D;\3\2\2\2D<\3\2\2\2D=\3\2\2\2D>\3\2\2\2D?\3\2\2\2D@\3\2\2\2DA\3\2\2\2"+
		"DB\3\2\2\2DC\3\2\2\2E\3\3\2\2\2FG\5\6\4\2G\5\3\2\2\2HL\7\3\2\2IK\5\64"+
		"\33\2JI\3\2\2\2KN\3\2\2\2LJ\3\2\2\2LM\3\2\2\2MP\3\2\2\2NL\3\2\2\2OQ\7"+
		"\35\2\2PO\3\2\2\2PQ\3\2\2\2QS\3\2\2\2RT\5&\24\2SR\3\2\2\2ST\3\2\2\2TU"+
		"\3\2\2\2UV\7\4\2\2V\7\3\2\2\2WX\5\n\6\2X\t\3\2\2\2YZ\7\5\2\2Z\\\5\60\31"+
		"\2[]\7\35\2\2\\[\3\2\2\2\\]\3\2\2\2]_\3\2\2\2^`\5&\24\2_^\3\2\2\2_`\3"+
		"\2\2\2`a\3\2\2\2ab\7\6\2\2b\13\3\2\2\2cd\7\7\2\2de\5\16\b\2ef\7\b\2\2"+
		"f\r\3\2\2\2gk\5\6\4\2hj\5\20\t\2ih\3\2\2\2jm\3\2\2\2ki\3\2\2\2kl\3\2\2"+
		"\2l\17\3\2\2\2mk\3\2\2\2nq\5\22\n\2oq\5\24\13\2pn\3\2\2\2po\3\2\2\2qr"+
		"\3\2\2\2rs\5\6\4\2s\21\3\2\2\2tu\7\t\2\2uv\5\n\6\2vw\7\n\2\2w\23\3\2\2"+
		"\2xy\7\13\2\2yz\5\n\6\2z{\7\t\2\2{\25\3\2\2\2|}\7\24\2\2}\27\3\2\2\2~"+
		"\177\t\2\2\2\177\31\3\2\2\2\u0080\u0081\t\3\2\2\u0081\33\3\2\2\2\u0082"+
		"\u0083\7\16\2\2\u0083\35\3\2\2\2\u0084\u0086\7\5\2\2\u0085\u0087\5 \21"+
		"\2\u0086\u0085\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u0089"+
		"\7\6\2\2\u0089\37\3\2\2\2\u008a\u008f\5\"\22\2\u008b\u008c\7\17\2\2\u008c"+
		"\u008e\5\"\22\2\u008d\u008b\3\2\2\2\u008e\u0091\3\2\2\2\u008f\u008d\3"+
		"\2\2\2\u008f\u0090\3\2\2\2\u0090!\3\2\2\2\u0091\u008f\3\2\2\2\u0092\u0093"+
		"\5\2\2\2\u0093#\3\2\2\2\u0094\u0095\5&\24\2\u0095%\3\2\2\2\u0096\u0098"+
		"\7\20\2\2\u0097\u0099\5(\25\2\u0098\u0097\3\2\2\2\u0098\u0099\3\2\2\2"+
		"\u0099\u009a\3\2\2\2\u009a\u009b\7\21\2\2\u009b\'\3\2\2\2\u009c\u00a1"+
		"\5*\26\2\u009d\u009e\7\17\2\2\u009e\u00a0\5*\26\2\u009f\u009d\3\2\2\2"+
		"\u00a0\u00a3\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2)\3"+
		"\2\2\2\u00a3\u00a1\3\2\2\2\u00a4\u00aa\5,\27\2\u00a5\u00a6\7\22\2\2\u00a6"+
		"\u00a7\5,\27\2\u00a7\u00a8\7\22\2\2\u00a8\u00aa\3\2\2\2\u00a9\u00a4\3"+
		"\2\2\2\u00a9\u00a5\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\u00ad\7\23\2\2\u00ac"+
		"\u00ae\7\35\2\2\u00ad\u00ac\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae\u00af\3"+
		"\2\2\2\u00af\u00b0\5.\30\2\u00b0+\3\2\2\2\u00b1\u00b2\7\34\2\2\u00b2-"+
		"\3\2\2\2\u00b3\u00b4\5\2\2\2\u00b4/\3\2\2\2\u00b5\u00bb\7\23\2\2\u00b6"+
		"\u00bc\5\62\32\2\u00b7\u00b8\7\22\2\2\u00b8\u00b9\5\62\32\2\u00b9\u00ba"+
		"\7\22\2\2\u00ba\u00bc\3\2\2\2\u00bb\u00b6\3\2\2\2\u00bb\u00b7\3\2\2\2"+
		"\u00bc\61\3\2\2\2\u00bd\u00be\7\34\2\2\u00be\63\3\2\2\2\u00bf\u00c5\7"+
		"\23\2\2\u00c0\u00c6\5\66\34\2\u00c1\u00c2\7\22\2\2\u00c2\u00c3\5\66\34"+
		"\2\u00c3\u00c4\7\22\2\2\u00c4\u00c6\3\2\2\2\u00c5\u00c0\3\2\2\2\u00c5"+
		"\u00c1\3\2\2\2\u00c6\65\3\2\2\2\u00c7\u00c8\7\34\2\2\u00c8\67\3\2\2\2"+
		"\u00c9\u00ca\7\37\2\2\u00ca9\3\2\2\2\22DLPS\\_kp\u0086\u008f\u0098\u00a1"+
		"\u00a9\u00ad\u00bb\u00c5";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}