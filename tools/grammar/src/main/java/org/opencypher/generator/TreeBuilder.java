package org.opencypher.generator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.opencypher.grammar.Exclusion;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.ProductionTransformation;
import org.opencypher.grammar.TermTransformation;

import static org.opencypher.generator.Node.root;

class TreeBuilder<T> implements TermTransformation<TreeBuilder.State<T>, TreeBuilder.State<T>, RuntimeException>,
                                ProductionTransformation<Void, TreeBuilder.State<T>, RuntimeException>
{
    private final Randomisation random;
    private final Supplier<T> context;
    private final Map<String, ProductionReplacement<T>> replacements;

    TreeBuilder( Randomisation random, Supplier<T> context, Map<String, ProductionReplacement<T>> replacements )
    {
        this.random = random;
        this.context = context;
        this.replacements = replacements;
    }

    Node buildTree( State<T> root )
    {
        for ( TreeBuilder.State<T> state = root; state != null; )
        {
            state = state.generate( this );
        }
        return root.node;
    }

    @Override
    public State<T> transformProduction( Void param, String language, Grammar.Term definition )
    {
        return state( definition, root( language ), context.get() );
    }

    static <T> State<T> state( Grammar.Term term, Node.Tree root, T context )
    {
        return new State<>( root, term, context, null );
    }

    @Override
    public State<T> transformAlternatives( State<T> current, Collection<Grammar.Term> alternatives )
    {
        return new State<>( current.node, random.choice( alternatives ), current.context, current.next() );
    }

    @Override
    public State<T> transformSequence( State<T> current, Collection<Grammar.Term> sequence )
    {
        return sequence( current.node, sequence.iterator(), current.context, current.next() );
    }

    @Override
    public State<T> transformLiteral( State<T> current, String value )
    {
        current.node.literal( value );
        return current.next();
    }

    @Override
    public State<T> transformNonTerminal( State<T> current, String productionName, Grammar.Term productionDef )
    {
        ProductionReplacement<T> replacement = replacements.get( productionName );
        if ( replacement != null )
        {
            current.node.production( productionName, replacement, current.context,
                                     node -> buildTree( new State<>( node, productionDef, current.context, null ) ) );
            return current.next();
        }
        return new State<>( current.node.child( productionName ), productionDef, current.context, current.next() );
    }

    @Override
    public State<T> transformOptional( State<T> current, Grammar.Term term )
    {
        return repeat( current.node, times( 0, 1 ), term, current.context, current.next() );
    }

    @Override
    public State<T> transformRepetition( State<T> current, int min, Integer max, Grammar.Term term )
    {
        return repeat( current.node, times( min, max ), term, current.context, current.next() );
    }

    @Override
    public State<T> transformEpsilon( State<T> current )
    {
        return current.next();
    }

    @Override
    public State<T> transformCharacters( State<T> current, String wellKnownSetName, List<Exclusion> exclusions )
    {
        switch ( wellKnownSetName )
        {// <pre>
        case "NUL": case "SOH": case "STX": case "ETX": case "EOT": case "ENQ": case "ACK": case "BEL":
        case "BS": case "TAB": case "LF": case "VT": case "FF": case "CR": case "SO": case "SI":
        case "DLE": case "DC1": case "DC2": case "DC3": case "DC4": case "NAK": case "SYN": case "ETB":
        case "CAN": case "EM": case "SUB": case "ESC": case "FS": case "GS": case "RS": case "US":
        case "SPACE": case "DEL":
        // </pre>
            assert exclusions.isEmpty();
            return codePoint( current, charNamed( wellKnownSetName ) );
        case "ANY":
            return codePoint( current, anyChar( exclusions ) );
        case "EOI":
            throw new IllegalStateException( "Cannot generate end of input." );
        default:
            throw new UnsupportedOperationException( "unknown character set: " + wellKnownSetName );
        }
    }

    private State<T> codePoint( State<T> current, int cp )
    {
        current.node.codePoint( cp );
        return current.next();
    }

    private int anyChar( List<Exclusion> exclusions )
    {
        return random.anyChar();
    }

    private int times( int min, Integer max )
    {
        return max == null ? random.repetition( min ) : random.repetition( min, max );
    }

    private static char charNamed( String singleCharSet )
    {
        switch ( singleCharSet )
        {// <pre>
        case "NUL":   return 0x00;
        case "SOH":   return 0x01;
        case "STX":   return 0x02;
        case "ETX":   return 0x03;
        case "EOT":   return 0x04;
        case "ENQ":   return 0x05;
        case "ACK":   return 0x06;
        case "BEL":   return 0x07;
        case "BS":    return 0x08;
        case "TAB":   return 0x09;
        case "LF":    return 0x0A;
        case "VT":    return 0x0B;
        case "FF":    return 0x0C;
        case "CR":    return 0x0D;
        case "SO":    return 0x0E;
        case "SI":    return 0x0F;
        case "DLE":   return 0x10;
        case "DC1":   return 0x11;
        case "DC2":   return 0x12;
        case "DC3":   return 0x13;
        case "DC4":   return 0x14;
        case "NAK":   return 0x15;
        case "SYN":   return 0x16;
        case "ETB":   return 0x17;
        case "CAN":   return 0x18;
        case "EM":    return 0x19;
        case "SUB":   return 0x1A;
        case "ESC":   return 0x1B;
        case "FS":    return 0x1C;
        case "GS":    return 0x1D;
        case "RS":    return 0x1E;
        case "US":    return 0x1F;
        case "SPACE": return 0x20;
        case "DEL":   return 0x7F;
        // </pre>
        default:
            throw new IllegalArgumentException( singleCharSet );
        }
    }

    private static <T> State<T> sequence( Node.Tree node, Iterator<Grammar.Term> sequence, T context, State<T> next )
    {
        return new Sequence<>( sequence, node, context, next ).get();
    }

    private static <T> State<T> repeat( Node.Tree node, int times, Grammar.Term term, T context, State<T> next )
    {
        return new Repetition<>( node, times, term, context, next ).get();
    }

    static final class State<T> implements Supplier<State<T>>
    {
        private final Node.Tree node;
        private final Grammar.Term term;
        private final T context;
        private final Supplier<State<T>> next;

        private State( Node.Tree node, Grammar.Term term, T context, Supplier<State<T>> next )
        {
            this.node = node;
            this.term = term;
            this.context = context;
            this.next = next == null ? () -> null : next;
        }

        @Override
        public String toString()
        {
            return "TreeBuilder.State{" + node + " @ " + term + "}";
        }

        private State<T> next()
        {
            return next.get();
        }

        State<T> generate( TreeBuilder<T> builder )
        {
            return term.transform( builder, this );
        }

        @Override
        public State<T> get()
        {
            return this;
        }
    }

    private static class Sequence<T> implements Supplier<State<T>>
    {
        private final Iterator<Grammar.Term> sequence;
        private final Node.Tree node;
        private final T context;
        private final State next;

        Sequence( Iterator<Grammar.Term> sequence, Node.Tree node, T context, State<T> next )
        {
            this.sequence = sequence;
            this.node = node;
            this.context = context;
            this.next = next;
        }

        @Override
        public State<T> get()
        {
            return sequence.hasNext() ? new State<>( node, sequence.next(), context, this ) : next;
        }
    }

    private static class Repetition<T> implements Supplier<State<T>>
    {
        private final Node.Tree node;
        private final Grammar.Term term;
        private final T context;
        private final State next;
        private int count;

        Repetition( Node.Tree node, int times, Grammar.Term term, T context, State<T> next )
        {
            this.node = node;
            this.count = times;
            this.term = term;
            this.context = context;
            this.next = next;
        }

        @Override
        public State<T> get()
        {
            return count-- > 0 ? new State<>( node, term, context, this ) : next;
        }
    }
}
