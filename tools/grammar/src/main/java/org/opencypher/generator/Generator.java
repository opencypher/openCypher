package org.opencypher.generator;

import java.util.function.Supplier;

import org.opencypher.grammar.Grammar;
import org.opencypher.tools.output.Output;

import static java.util.Objects.requireNonNull;

import static org.opencypher.tools.Functions.map;
import static org.opencypher.tools.Functions.requireNonNull;

public final class Generator
{
    private final Grammar grammar;
    private final TreeBuilder<?> builder;

    @SafeVarargs
    public Generator( Grammar grammar, ProductionReplacement<Void>... replacements )
    {
        this( Randomisation.simpleRandomisation(), grammar, replacements );
    }

    @SafeVarargs
    public <T> Generator( Grammar grammar, Supplier<T> context, ProductionReplacement<T>... replacements )
    {
        this( Randomisation.simpleRandomisation(), grammar, context, replacements );
    }

    @SafeVarargs
    public Generator( Randomisation random, Grammar grammar, ProductionReplacement<Void>... replacements )
    {
        this( random, grammar, () -> null, replacements );
    }

    @SafeVarargs
    public <T> Generator( Randomisation random, Grammar grammar, Supplier<T> context,
                          ProductionReplacement<T>... replacements )
    {
        this.grammar = grammar;
        this.builder = new TreeBuilder<>(
                requireNonNull( Randomisation.class, random ),
                requireNonNull( context, "context" ),
                map( replacement -> {
                    String name = replacement.production();
                    if ( !grammar.hasProduction( name ) )
                    {
                        throw new IllegalArgumentException(
                                "Grammar for " + grammar.language() + " does not contain a production for " +
                                replacement.production() );
                    }
                    return name;
                }, replacements ) );
    }

    public void generate( String start, Output output )
    {
        generateTree( start ).write( output );
    }

    public void generate( Output output )
    {
        generate( grammar.language(), output );
    }

    Node generateTree( String start )
    {
        return generateTree( grammar, builder, start );
    }

    private static <T> Node generateTree( Grammar grammar, TreeBuilder<T> builder, String start )
    {
        return builder.buildTree( grammar.transform( start, builder, null ) );
    }
}
