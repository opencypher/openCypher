package org.opencypher.grammar;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opencypher.tools.xml.LocationAware;

class Located implements LocationAware
{
    private String path;
    private int lineNumber;
    private int columnNumber;

    @Override
    public final void location( String path, int lineNumber, int columnNumber )
    {
        this.path = path;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    static Comparator<Located> comparator( Map<String, ? extends Located> locations )
    {
        return ( lhs, rhs ) -> compare( locations, lhs, rhs );
    }

    private static int compare( Map<String, ? extends Located> locations, Located lhs, Located rhs )
    {
        if ( Objects.equals( lhs.path, rhs.path ) )
        {
            return compare( lhs, rhs );
        }
        return compare( trace( locations, lhs ).iterator(),
                        trace( locations, rhs ).iterator() );
    }

    private static int compare( Iterator<Located> left, Iterator<Located> right )
    {
        while ( left.hasNext() && right.hasNext() )
        {
            Located lhs = left.next(), rhs = right.next();
            if ( !Objects.equals( lhs.path, rhs.path ) )
            {
                break;
            }
            int pos = compare( lhs, rhs );
            if ( pos != 0 )
            {
                return pos;
            }
        }
        throw new IllegalStateException( "Located objects should share a root, " +
                                         "and be included from different positions where their trees diverge." );
    }

    private static List<Located> trace( Map<String, ? extends Located> locations, Located located )
    {
        LinkedList<Located> result = new LinkedList<>();
        do
        {
            result.addFirst( located );
        } while ( null != (located = locations.get( located.path )) );
        return result;
    }

    private static int compare( Located lhs, Located rhs )
    {
        int pos = lhs.lineNumber - rhs.lineNumber;
        if ( pos == 0 )
        {
            pos = lhs.columnNumber - rhs.columnNumber;
        }
        return pos;
    }
}
