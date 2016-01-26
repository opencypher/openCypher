package org.opencypher.generator;

import java.util.List;

import org.opencypher.grammar.BiasedTerms;
import org.opencypher.grammar.Exclusion;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Repetition;
import org.opencypher.tools.output.Output;

import static java.util.Objects.requireNonNull;

public class TracingChoices implements Choices
{
    private final Output output;
    private final Choices choices;

    public TracingChoices( Output output, Choices choices )
    {
        this.output = requireNonNull( output, "output" );
        this.choices = requireNonNull( choices, "choices" );
    }

    @Override
    public Grammar.Term choose( Node location, BiasedTerms alternatives )
    {
        Grammar.Term result = choices.choose( location, alternatives );
        location( output.append( "At " ), location )
                .append( " choose: " )
                .append( result.toString() )
                .println();
        return result;
    }

    @Override
    public int repetition( Node location, Repetition repetition )
    {
        int result = choices.repetition( location, repetition );
        location( output.append( "At " ), location )
                .append( " repeat " )
                .append( repetition.term().toString() )
                .append( " " )
                .append( result )
                .println( " times" );
        return result;
    }

    @Override
    public boolean includeOptional( Node location, Optional optional )
    {
        boolean result = choices.includeOptional( location, optional );
        location( output.append( "At " ), location )
                .append( result ? " include optional " : " exclude optional " )
                .append( optional.term().toString() )
                .println( "." );
        return result;
    }

    @Override
    public int anyChar( Node location, List<Exclusion> exclusions )
    {
        int result = choices.anyChar( location, exclusions );
        location( output.append( "At " ), location )
                .append( " emit char '" )
                .appendCodePoint( result )
                .println( "'." );
        return result;
    }

    static Output location( Output output, Node location )
    {
        Node parent = location.parent();
        if ( parent != null )
        {
            location( output, parent );
            output.append( " -> " );
        }
        output.append( location.name() );
        return output;
    }
}
