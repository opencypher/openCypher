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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this has the same name, but slightly different behaviour to the scala equivalent
 */
public class FailingErrorListener extends BaseErrorListener
{

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(FailingErrorListener.class.getName());

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e)
    {
        super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
//         existing error message will tell us what's wrong
         LOGGER.warn("Syntax error at line {}:{} with '" + offendingSymbol + "'",
         line, charPositionInLine);
         LOGGER.warn("msg: {}", msg);
        throw new IllegalArgumentException("Syntax error:", e);
    }

    // @Override
    // public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int
    // stopIndex, boolean exact,
    // BitSet ambigAlts, ATNConfigSet configs) {
    // super.reportAmbiguity(recognizer, dfa, startIndex, stopIndex, exact,
    // ambigAlts, configs);
    // LOGGER.warn("ambiguity");
    // }
    //
    // @Override
    // public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int
    // startIndex, int stopIndex,
    // BitSet conflictingAlts, ATNConfigSet configs) {
    // super.reportAttemptingFullContext(recognizer, dfa, startIndex, stopIndex,
    // conflictingAlts, configs);
    // LOGGER.warn("attemptingFullContext");
    // }
    //
    // @Override
    // public void reportContextSensitivity(Parser recognizer, DFA dfa, int
    // startIndex, int stopIndex, int prediction,
    // ATNConfigSet configs) {
    // super.reportContextSensitivity(recognizer, dfa, startIndex, stopIndex,
    // prediction, configs);
    // LOGGER.warn("contextSensitivity");
    // }

}
