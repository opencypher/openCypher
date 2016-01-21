package org.opencypher.generator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.opencypher.grammar.Alternatives;
import org.opencypher.grammar.Grammar;

public interface Randomisation
{
    static Randomisation simpleRandomisation()
    {
        return new Randomisation()
        {
            @Override
            public int repetition( int min, int max )
            {
                return ThreadLocalRandom.current().nextInt( min, max + 1 );
            }

            @Override
            public int repetition( int min )
            {
                if ( min == 0 )
                {
                    return (int) Math.abs( Math.floor( ThreadLocalRandom.current().nextGaussian() * 2 ) );
                }
                else
                {
                    return (int) (Math.abs( ThreadLocalRandom.current().nextGaussian() * 2 ) + min);
                }
            }

            @Override
            public Grammar.Term choice( Alternatives alternatives )
            {
                return alternatives.term( ThreadLocalRandom.current().nextInt( alternatives.terms() ) );
            }

            @Override
            public int choice( int[] codepoints )
            {
                return codepoints[ThreadLocalRandom.current().nextInt( codepoints.length )];
            }

            @Override
            public int anyChar()
            {
                int cp;
                do
                {
                    cp = ThreadLocalRandom.current().nextInt( Character.MAX_CODE_POINT );
                } while ( !Character.isValidCodePoint( cp ) );
                return cp;
            }
        };
    }

    int repetition( int min, int max );

    int repetition( int min );

    Grammar.Term choice( Alternatives alternatives );

    int choice( int[] codepoints );

    int anyChar();
}
