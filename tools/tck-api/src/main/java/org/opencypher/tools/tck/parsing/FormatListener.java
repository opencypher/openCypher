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
package org.opencypher.tools.tck.parsing;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opencypher.tools.tck.parsing.generated.FeatureResultsLexer;
import org.opencypher.tools.tck.parsing.generated.FeatureResultsParser;

public class FormatListener
{
    private FeatureResultsParser parser;
    private FeatureResultsLexer lexer;
    private ParseTreeWalker walker;
    private ParameterVerifier parameterVerifier;
    private ResultsVerifier resultsVerifier;

    public FormatListener()
    {
        this.lexer = new FeatureResultsLexer( new ANTLRInputStream( "" ) );
        this.parser = new FeatureResultsParser( new CommonTokenStream( lexer ) );
        this.walker = new ParseTreeWalker();
        this.parameterVerifier = new ParameterVerifier();
        this.resultsVerifier = new ResultsVerifier();
    }

    private void resetListeners( ANTLRErrorListener listener )
    {
        // This also removes some default-added error printing listeners
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener( listener );
        parser.addErrorListener( listener );
    }

    public boolean parseParameter( String value )
    {
        resetListeners( parameterVerifier );
        lexer.setInputStream( new ANTLRInputStream( value ) );
        walker.walk( parameterVerifier, parser.value() );
        return parameterVerifier.getOkAndReset();
    }

    public boolean parseResults( String value )
    {
        resetListeners( resultsVerifier );
        lexer.setInputStream( new ANTLRInputStream( value ) );
        walker.walk( resultsVerifier, parser.value() );
        return resultsVerifier.getOkAndReset();
    }

}
