package org.opencypher.tools.output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

@FunctionalInterface
public interface Input
{
    String read();

    static Input stdIn()
    {
        return input( System.in );
    }

    static Input input( InputStream input )
    {
        return input( new BufferedReader( new InputStreamReader( input ) ) );
    }

    static Input input( Reader in )
    {
        BufferedReader input = in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader( in );
        return () -> {
            try
            {
                return input.readLine();
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( e );
            }
        };
    }
}
