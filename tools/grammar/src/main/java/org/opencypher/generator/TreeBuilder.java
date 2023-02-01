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
package org.opencypher.generator;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.CharacterSet;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.Literal;
import org.opencypher.grammar.NonTerminal;
import org.opencypher.grammar.Optional;
import org.opencypher.grammar.Production;
import org.opencypher.grammar.ProductionTransformation;
import org.opencypher.grammar.Repetition;
import org.opencypher.grammar.Sequence;
import org.opencypher.grammar.TermTransformation;

import static org.opencypher.generator.Node.root;

class TreeBuilder<T> implements TermTransformation<TreeBuilder.State<T>, TreeBuilder.State<T>, RuntimeException>,
                                ProductionTransformation<Void, TreeBuilder.State<T>, RuntimeException>
{
    private final Choices choice;
    private final Supplier<T> context;
    private final Map<String, ProductionReplacement<T>> replacements;

    TreeBuilder( Choices choice, Supplier<T> context, Map<String, ProductionReplacement<T>> replacements )
    {
        this.choice = choice;
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
    public State<T> transformProduction( Void param, Production production )
    {
        return state( production.definition(), root( production.name() ), context.get() );
    }

    static <T> State<T> state( Grammar.Term term, Node.Tree root, T context )
    {
        return new State<>( root, term, context, null );
    }

    @Override
    public State<T> transformAlternatives( State<T> current, Alternatives alternatives )
    {
        return new State<>( current.node, choice.choose( current.node, alternatives.eligibleForGeneration() ),
                            current.context,
                            current.next() );
    }

    @Override
    public State<T> transformSequence( State<T> current, Sequence sequence )
    {
        return sequence( current.node, sequence.iterator(), current.context, current.next() );
    }

    @Override
    public State<T> transformLiteral( State<T> current, Literal value )
    {
        current.node.literal( value );
        return current.next();
    }

    @Override
    public State<T> transformNonTerminal( State<T> current, NonTerminal nonTerminal )
    {
        ProductionReplacement<T> replacement = replacements.get( nonTerminal.productionName() );
        if ( replacement != null )
        {
            current.node.production( nonTerminal.productionName(), replacement, current.context,
                                     node -> buildTree( new State<>(
                                             node, nonTerminal.productionDefinition(), current.context, null ) ) );
            return current.next();
        }
        return new State<>(
                current.node.child( nonTerminal.productionName() ),
                nonTerminal.productionDefinition(),
                current.context,
                current.next() );
    }

    @Override
    public State<T> transformOptional( State<T> current, Optional optional )
    {
        int times = choice.includeOptional( current.node, optional ) ? 1 : 0;
        return repeat( current.node, times, optional.term(), current.context, current.next() );
    }

    @Override
    public State<T> transformRepetition( State<T> current, Repetition repetition )
    {
        return repeat(
                current.node,
                choice.repetition( current.node, repetition ),
                repetition.term(),
                current.context,
                current.next() );
    }

    @Override
    public State<T> transformEpsilon( State<T> current )
    {
        return current.next();
    }

    @Override
    public State<T> transformCharacters( State<T> current, CharacterSet characters )
    {
        return codePoint( current, choice.codePoint( current.node, characters ) );
    }

    private State<T> codePoint( State<T> current, int cp )
    {
        current.node.codePoint( cp );
        return current.next();
    }

    private static <T> State<T> sequence( Node.Tree node, Iterator<Grammar.Term> sequence, T context, State<T> next )
    {
        return new StateSequence<>( sequence, node, context, next ).get();
    }

    private static <T> State<T> repeat( Node.Tree node, int times, Grammar.Term term, T context, State<T> next )
    {
        return new StateRepetition<>( node, times, term, context, next ).get();
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

    private static class StateSequence<T> implements Supplier<State<T>>
    {
        private final Iterator<Grammar.Term> sequence;
        private final Node.Tree node;
        private final T context;
        private final State next;

        StateSequence( Iterator<Grammar.Term> sequence, Node.Tree node, T context, State<T> next )
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

    private static class StateRepetition<T> implements Supplier<State<T>>
    {
        private final Node.Tree node;
        private final Grammar.Term term;
        private final T context;
        private final State next;
        private int count;

        StateRepetition( Node.Tree node, int times, Grammar.Term term, T context, State<T> next )
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
