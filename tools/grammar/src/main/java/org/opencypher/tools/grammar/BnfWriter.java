package org.opencypher.tools.grammar;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.GrammarVisitor;
import org.opencypher.grammar.Literal;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Production;
import org.opencypher.grammar.Repetition;
import org.opencypher.grammar.Sequence;
import org.opencypher.tools.output.Output;

import static org.opencypher.tools.Functions.requireNonNull;

abstract class BnfWriter implements GrammarVisitor<RuntimeException>
{
    private int altPrefix;
    private boolean group;
    protected final Output output;

    BnfWriter( Output output )
    {
        this.output = requireNonNull( Output.class, output );
    }

    protected abstract void productionCommentPrefix();

    protected abstract void productionCommentLinePrefix();

    protected abstract void productionCommentSuffix();

    protected abstract void productionStart( Production production );

    protected abstract void productionEnd( Production production );

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

    protected abstract void epsilon();

    @Override
    public final void visitProduction( Production production ) throws RuntimeException
    {
        String description = production.description();
        if ( description != null )
        {
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
        this.altPrefix = production.name().length();
        group = false;
        productionStart( production );
        production.definition().accept( this );
        productionEnd( production );
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
             || Character.charCount( literal.codePointAt( 0 ) ) == literal.length()
                && !Character.isLetter( literal.codePointAt( 0 ) ) )
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

    protected final void group( Runnable action )
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

    protected final void groupWith( char prefix, Runnable action, char suffix )
    {
        boolean group = this.group;
        this.group = false;
        output.append( prefix );
        action.run();
        output.append( suffix );
        this.group = group;
    }

    protected final void groupWithoutPrefix( Runnable action )
    {
        boolean group = this.group;
        this.group = true;
        action.run();
        this.group = group;
    }
}
