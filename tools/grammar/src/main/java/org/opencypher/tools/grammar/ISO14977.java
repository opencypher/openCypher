package org.opencypher.tools.grammar;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.GrammarVisitor;
import org.opencypher.tools.output.Output;

import static org.opencypher.tools.output.Output.output;

public class ISO14977 implements GrammarVisitor<RuntimeException>
{
    public static void write( Grammar grammar, Writer writer )
    {
        write( grammar, output( writer ) );
    }

    public static void write( Grammar grammar, OutputStream stream )
    {
        write( grammar, output( stream ) );
    }

    public static void write( Grammar grammar, Output output )
    {
        grammar.accept( new ISO14977( grammar, output ) );
    }

    public static void main( String... args ) throws Exception
    {
        Main.execute( ISO14977::write, args );
    }

    private final Grammar grammar;
    private final Output output;
    private String altPrefix = "";
    private boolean group;

    private ISO14977( Grammar grammar, Output output )
    {
        this.grammar = grammar;
        this.output = output;
    }

    @Override
    public void visitProduction( String production, Grammar.Term definition ) throws RuntimeException
    {
        StringBuilder altPrefix = new StringBuilder( production.length() + 1 ).append( '\n' );
        for ( int i = production.length(); i-- > 0; )
        {
            altPrefix.append( ' ' );
        }
        String description = grammar.productionDescription( production );
        if ( description != null )
        {
            output.append( "(* " ).append( description ).println( " *)" );
        }
        this.altPrefix = altPrefix.toString();
        group = false;
        output.append( production ).append( " = " );
        definition.accept( this );
        output.println( " ;" ).println();
        this.altPrefix = "";
    }

    @Override
    public void visitAlternatives( Collection<Grammar.Term> alternatives )
    {
        boolean group = this.group;
        this.group = true;
        boolean prefix = false;
        if ( group )
        {
            output.append( '(' );
        }
        for ( Grammar.Term term : alternatives )
        {
            if ( prefix )
            {
                output.append( altPrefix ).append( " | " );
            }
            term.accept( this );
            prefix = true;
        }
        if ( alternatives.size() > 1 )
        {
            output.append( altPrefix );
        }
        if ( group )
        {
            output.append( ')' );
        }
        this.group = group;
    }

    @Override
    public void visitSequence( Collection<Grammar.Term> sequence )
    {
        boolean group = this.group;
        this.group = true;
        String altPrefix = this.altPrefix;
        this.altPrefix = "";
        String prefix = "";
        if ( group )
        {
            output.append( '(' );
        }
        for ( Grammar.Term term : sequence )
        {
            output.append( prefix );
            term.accept( this );
            prefix = ", ";
        }
        if ( group )
        {
            output.append( ')' );
        }
        this.altPrefix = altPrefix;
        this.group = group;
    }

    @Override
    public void visitLiteral( String value )
    {
        output.append( '"' ).append( value ).append( '"' );
    }

    @Override
    public void visitNonTerminal( String production, Grammar.Term definition )
    {
        output.append( production );
    }

    @Override
    public void visitOptional( Grammar.Term term )
    {
        String altPrefix = this.altPrefix;
        this.altPrefix = "";
        {
            boolean group = this.group;
            this.group = false;
            {
                output.append( '[' );
                term.accept( this );
                output.append( ']' );
            }
            this.group = group;
        }
        this.altPrefix = altPrefix;
    }

    @Override
    public void visitRepetition( int min, Integer max, Grammar.Term term )
    {
        String altPrefix = this.altPrefix;
        this.altPrefix = "";
        {
            if ( group && min > 0 && (max == null || max > min) )
            {
                output.append( '(' );
            }
            if ( min > 0 )
            {
                boolean group = this.group;
                this.group = true;
                {
                    if ( min > 1 )
                    {
                        output.append( min ).append( " * " );
                    }
                    term.accept( this );
                }
                this.group = group;
            }
            if ( max == null )
            {
                boolean group = this.group;
                this.group = false;
                {
                    if ( min > 0 )
                    {
                        output.append( ", " );
                    }
                    output.append( '{' );
                    term.accept( this );
                    output.append( '}' );
                }
                this.group = group;
            }
            else if ( max > min )
            {
                boolean group = this.group;
                this.group = false;
                {
                    if ( min > 0 )
                    {
                        output.append( ", " );
                    }
                    output.append( max - min ).append( " * " );
                    output.append( '[' );
                    term.accept( this );
                    output.append( ']' );
                }
                this.group = group;
            }
            if ( group && min > 0 && (max == null || max > min) )
            {
                output.append( ')' );
            }
        }
        this.altPrefix = altPrefix;
    }
}
