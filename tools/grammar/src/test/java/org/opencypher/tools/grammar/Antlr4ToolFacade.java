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
package org.opencypher.tools.grammar;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.v4.Tool;
import org.antlr.v4.parse.GrammarTreeVisitor;
import org.antlr.v4.tool.ANTLRMessage;
import org.antlr.v4.tool.ANTLRToolListener;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.antlr.v4.tool.ast.RuleAST;
import org.opencypher.grammar.Fixture;
import org.opencypher.tools.io.Output;

import static org.junit.Assert.fail;
import static org.opencypher.tools.io.Output.lineNumbers;
import static org.opencypher.tools.io.Output.stdOut;
import static org.opencypher.tools.io.Output.stringBuilder;

public class Antlr4ToolFacade implements ANTLRToolListener
{
    public static void assertGeneratesValidParser( String resource ) throws Exception
    {
        Output.Readable buffer = stringBuilder();
        Tool antlr = new Tool();
        Antlr4ToolFacade facade = new Antlr4ToolFacade( antlr, buffer );
        try
        {
            Antlr4.write( Fixture.grammarResource( Antlr4.class, resource ), buffer );
        }
        catch ( Throwable e )
        {
            try
            {
                facade.reportFailureIn( "generating grammar" );
            }
            catch ( AssertionError x )
            {
                throw e;
            }
        }
        antlr.addListener( facade );
        GrammarRootAST ast = antlr.parse( resource, new ANTLRReaderStream( buffer.reader() ) );
        if ( ast.hasErrors )
        {
            RuleAST lastGood = lastGoodRule( ast );
            if ( lastGood == null )
            {
                facade.reportFailureIn( "parsing grammar" );
            }
            else
            {
                facade.reportFailureIn(
                        "parsing grammar, after " + lastGood.getRuleName() + " on line " + lastGood.getLine() );
            }
        }
        antlr.process( antlr.createGrammar( ast ), false );
        if ( facade.hasErrors() )
        {
            facade.reportFailureIn( "processing grammar" );
        }
    }

    private static RuleAST lastGoodRule( GrammarAST ast )
    {
        ast = (GrammarAST) ast.getFirstChildWithType( GrammarTreeVisitor.RULES );
        if ( ast == null )
        {
            return null;
        }
        RuleAST last = null;
        for ( GrammarAST rule : ast.getChildrenAsArray() )
        {
            if ( rule instanceof RuleAST )
            {
                last = (RuleAST) rule;
            }
            else
            {
                return last;
            }
        }
        return null;
    }

    private final List<ANTLRMessage> errors = new ArrayList<>();
    private final Tool tool;
    private final Output.Readable buffer;

    private Antlr4ToolFacade( Tool tool, Output.Readable buffer )
    {
        this.tool = tool;
        this.buffer = buffer;
    }

    private void reportFailureIn( String phase )
    {
        lineNumbers( stdOut() ).append( buffer );
        for ( ANTLRMessage msg : errors )
        {
            System.err.println( tool.errMgr.getMessageTemplate( msg ).render() );
        }
        fail( "Antlr failure in " + phase );
    }

    private boolean hasErrors()
    {
        return !errors.isEmpty();
    }

    @Override
    public void info( String msg )
    {
        System.out.println( msg );
    }

    @Override
    public void error( ANTLRMessage msg )
    {
        errors.add( msg );
    }

    @Override
    public void warning( ANTLRMessage msg )
    {
        errors.add( msg );
    }
}
