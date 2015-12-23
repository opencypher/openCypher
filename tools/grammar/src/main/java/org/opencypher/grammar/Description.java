package org.opencypher.grammar;

import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;

import static java.lang.Character.charCount;
import static java.lang.Character.codePointAt;
import static java.lang.Character.isLowSurrogate;
import static java.lang.Character.isWhitespace;

@Element(uri = Grammar.XML_NAMESPACE, name = "description")
class Description
{
    private final StringBuilder text = new StringBuilder();

    @Child
    final void text( char[] buffer, int start, int length )
    {
        int cp;
        while ( isWhitespace( cp = codePointAt( buffer, start ) ) )
        {
            int chars = charCount( cp );
            start += chars;
            length -= chars;
        }
        while ( length > 0 &&
                (isLowSurrogate( buffer[start + length - 1] )
                 ? isWhitespace( cp = codePointAt( buffer, start + length - 2 ) )
                 : isWhitespace( cp = buffer[start + length - 1] )) )
        {
            length -= charCount( cp );
        }
        if ( length > 0 )
        {
            if ( text.length() > 0 )
            {
                text.append( '\n' );
            }
            text.append( buffer, start, length );
        }
    }

    @Override
    public String toString()
    {
        return text.toString();
    }

    String appendTo( String text )
    {
        if ( this.text.length() == 0 )
        {
            return text;
        }
        else if ( text.length() > 0 )
        {
            this.text.insert( 0, text ).insert( text.length(), '\n' );
        }
        return toString();
    }
}
