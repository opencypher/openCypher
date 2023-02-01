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

import java.util.Set;
import java.util.TreeSet;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Literal;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Production;
import org.opencypher.grammar.ProductionVisitor;
import org.opencypher.grammar.Repetition;
import org.opencypher.grammar.Sequence;
import org.opencypher.grammar.TermVisitor;
import org.opencypher.tools.io.Output;

import static org.opencypher.tools.Functions.requireNonNull;

/**
 * Shared base for generators that generate BNF-like grammar definitions.
 */
abstract class BnfWriter implements ProductionVisitor<RuntimeException>, TermVisitor<RuntimeException>, AutoCloseable
{
    private int altPrefix;
    private boolean group;
    protected final Output output;
    protected final Set<Integer> caseChars = new TreeSet<>();

    BnfWriter( Output output )
    {
        this.output = requireNonNull( Output.class, output );
    }

    protected abstract void productionCommentPrefix();

    protected abstract void productionCommentLinePrefix();

    protected abstract void productionCommentSuffix();

    protected abstract void productionStart( Production p );

    protected abstract String prefix( String s );

    protected abstract void productionEnd();

    protected abstract void alternativesLinePrefix( int altPrefix );

    protected abstract void alternativesSeparator();

    protected abstract void sequenceSeparator();

    protected abstract void groupPrefix();

    protected abstract void groupSuffix();

    /**
     * @return {@code true} if the prefix implies grouping.
     */
    protected abstract boolean optionalPrefix();

    protected abstract void optionalSuffix();

    protected abstract void repeat( int minTimes, Integer maxTimes, Runnable repeated );

    protected abstract void characterSet( CharacterSet characters );

    protected abstract void nonTerminal( NonTerminal nonTerminal );

    /**
     * Writes a case sensitive literal.
     *
     * @param value the case sensitive literal to be written.
     */
    protected abstract void literal( String value );

    /**
     * Writes a case insensitive literal.
     *
     * @param value the case insensitive literal to be written.
     */
    protected abstract void caseInsensitive( String value );

    protected abstract void caseInsensitiveProductionStart( String name );

    protected abstract void epsilon();

    @Override
    public final void visitProduction( Production production ) throws RuntimeException
    {
        String description = production.description();
        if ( description != null )
        {
            // standardise description (i think this should be done on the read
            description = description.replaceAll("\r", "").trim() + "\n";
            productionCommentPrefix();
            for ( int pos = 0, line; pos < description.length(); pos = line )
            {
                line = description.indexOf( '\n', pos );
                if ( line == -1 )
                {
                    line = description.length();
                }
                else
                {
                    line += 1;
                }
                if ( pos > 0 )
                {
                    productionCommentLinePrefix();
                }
                output.append( description, pos, line );
            }
            productionCommentSuffix();
        }
        this.altPrefix = prefix( production.name() ).length();
        group = false;
        productionStart( production );
        production.definition().accept( this );
        productionEnd();
        this.altPrefix = 0;
    }

    @Override
    public final void visitAlternatives( Alternatives alternatives )
    {
        group( () -> {
            boolean prefix = false;
            for ( Grammar.Term term : alternatives )
            {
                if ( prefix )
                {
                    if ( altPrefix > 0 )
                    {
                        alternativesLinePrefix( altPrefix );
                    }
                    alternativesSeparator();
                }
                term.accept( this );
                prefix = true;
            }
            if ( alternatives.terms() > 1 && altPrefix > 0 )
            {
                alternativesLinePrefix( altPrefix );
            }
        } );
    }

    @Override
    public final void visitSequence( Sequence sequence )
    {
        int altPrefix = this.altPrefix;
        this.altPrefix = 0;
        group( () -> {
            boolean sep = false;
            for ( Grammar.Term term : sequence )
            {
                if ( sep )
                {
                    sequenceSeparator();
                }
                term.accept( this );
                sep = true;
            }
        } );
        this.altPrefix = altPrefix;
    }

    @Override
    public final void visitLiteral( Literal literal )
    {
        if ( literal.caseSensitive()
             || ( Character.charCount( literal.codePointAt( 0 ) ) == literal.length()
                && !Character.isLetter( literal.codePointAt( 0 ) ) )
             || !hasCaseAlternatives( literal.toString() ) )
        {
            literal( literal.toString() );
        }
        else if ( literal.length() == 0 )
        {
            visitEpsilon();
        }
        else
        {
            caseInsensitive( literal.toString() );
        }
    }

    private boolean hasCaseAlternatives( String string )
    {
        for ( int i = 0; i < string.length(); ++i )
        {
            int upper = Character.toUpperCase( string.codePointAt( i ) );
            int lower = Character.toLowerCase( string.codePointAt( i ) );
            if ( upper != lower )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public final void visitCharacters( CharacterSet characters )
    {
        characterSet( characters );
    }

    @Override
    public final void visitNonTerminal( NonTerminal nonTerminal )
    {
        nonTerminal( nonTerminal );
    }

    @Override
    public final void visitOptional( Optional optional )
    {
        int altPrefix = this.altPrefix;
        this.altPrefix = 0;
        {
            boolean group = this.group;
            this.group = !optionalPrefix();
            optional.term().accept( this );
            this.group = group;
            optionalSuffix();
        }
        this.altPrefix = altPrefix;
    }

    @Override
    public final void visitRepetition( Repetition repetition )
    {
        int altPrefix = this.altPrefix;
        this.altPrefix = 0;
        try
        {
            repeat( repetition.minTimes(), maxTimes( repetition ), () -> repetition.term().accept( this ) );
        }
        catch ( UnsupportedOperationException e )
        {
            throw new UnsupportedOperationException( repetition.toString(), e );
        }
        this.altPrefix = altPrefix;
    }

    private Integer maxTimes( Repetition repetition )
    {
        return repetition.limited() ? repetition.maxTimes() : null;
    }

    @Override
    public final void visitEpsilon() throws RuntimeException
    {
        epsilon();
    }

    @Override
    public void close()
    {
        for ( int chr : caseChars )
        {
            int upper = Character.toUpperCase( chr );
            int lower = Character.toLowerCase( chr );
            int title = Character.toTitleCase( chr );
            caseInsensitiveProductionStart( String.valueOf( (char) upper ) );
            literal( String.valueOf( (char) upper ) );
            alternativesSeparator();
            literal( String.valueOf( (char) lower ) );
            if ( title != upper )
            {
                alternativesSeparator();
                literal( String.valueOf( (char) title ) );
            }
            productionEnd();
        }
    }

    final void group( Runnable action )
    {
        boolean group = this.group;
        this.group = true;
        if ( group )
        {
            groupPrefix();
        }
        action.run();
        if ( group )
        {
            groupSuffix();
        }
        this.group = group;
    }

    final void groupWith( char prefix, Runnable action, char suffix )
    {
        boolean group = this.group;
        this.group = false;
        output.append( prefix ).append( " " );
        action.run();
        output.append( " " ).append( suffix );
        this.group = group;
    }

    final void groupWithoutPrefix( Runnable action )
    {
        boolean group = this.group;
        this.group = true;
        action.run();
        this.group = group;
    }

    final void addCaseChar( int codePoint )
    {
        caseChars.add( codePoint );
    }
}
