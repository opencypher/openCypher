package org.opencypher.tools.xml;

import java.util.BitSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

class NodeBuilder
{
    public static NodeBuilder tree( Class<?> root )
    {
        return Structure.tree( root ).factory( null, ( parent, child ) -> {
        } );
    }

    final String uri, name;
    private final AttributeHandler[] attributes;
    private final CharactersHandler characters;
    private final NodeBuilder[] children;
    private final Function<Object, Object> factory;
    private final BiConsumer<Object, Object> handler;

    NodeBuilder( String uri, String name, AttributeHandler[] attributes, CharactersHandler characters,
                 NodeBuilder[] children, Function<Object, Object> factory, BiConsumer<Object, Object> handler )
    {
        this.uri = uri;
        this.name = name;
        this.attributes = attributes;
        this.characters = characters;
        this.children = children;
        this.factory = factory;
        this.handler = handler;
    }

    @Override
    public String toString()
    {
        return String.format( "Element{uri='%s', name='%s'}", uri, name );
    }

    public Object create( Object parent )
    {
        return factory.apply( parent );
    }

    public void child( Object parent, Object child )
    {
        handler.accept( parent, child );
    }

    public boolean attribute(
            BitSet remaining, Object target, Resolver resolver, String uri, String name, String type, String value )
    {
        for ( int i = 0; i < attributes.length; i++ )
        {
            AttributeHandler attribute = attributes[i];
            if ( attribute.matches( uri, name ) )
            {
                attribute.apply( target, resolver, value );
                remaining.clear( i );
                return true;
            }
        }
        return false;
    }

    public void characters( Object target, char[] buffer, int start, int length )
    {
        characters.characters( target, buffer, start, length );
    }

    public NodeBuilder child( String uri, String name )
    {
        for ( NodeBuilder child : children )
        {
            if ( child.matches( uri, name ) )
            {
                return child;
            }
        }
        throw new IllegalArgumentException(
                "No such child: '" + name + "' in namespace " + uri +
                " of '" + this.name + "' in namespace " + this.uri );
    }

    public boolean matches( String uri, String name )
    {
        return this.uri.equalsIgnoreCase( uri ) && this.name.equalsIgnoreCase( name );
    }

    public BitSet requiredAttributes()
    {
        BitSet required = new BitSet( attributes.length );
        for ( int i = 0; i < attributes.length; i++ )
        {
            if ( !attributes[i].optional )
            {
                required.set( i );
            }
        }
        return required;
    }

    public void verifyRequiredAttributes( BitSet required )
    {
        if ( required.cardinality() != 0 )
        {
            throw new IllegalArgumentException( required.stream().mapToObj( ( i ) -> attributes[i].name ).collect(
                    Collectors.joining( ", ", "Missing required attributes: ", "" ) ) );
        }
    }
}
