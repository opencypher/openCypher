package org.opencypher.tools.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static java.lang.invoke.MethodHandles.collectArguments;

abstract class Resolver
{
    private final Set<Path> parsedPaths;

    protected Resolver()
    {
        this( new HashSet<>() );
    }

    private Resolver( Set<Path> parsedPaths )
    {
        this.parsedPaths = parsedPaths;
    }

    final XmlFile file( String path )
    {
        Path resolved = path( path );
        return new XmlFile( new Child( parsedPaths, resolved.getParent() ), resolved );
    }

    abstract Path path( String path );

    boolean parsed( Path path )
    {
        return parsedPaths.contains( path );
    }

    <T> T parse( Path path, XmlParser<T> parser, XmlParser.Option... options )
            throws IOException, SAXException, ParserConfigurationException
    {
        parsedPaths.add( path );
        try ( InputStream stream = Files.newInputStream( path ) )
        {
            InputSource input = new InputSource( stream );
            input.setSystemId( XmlFile.canonicalize( path ) );
            return parser.parse( this, input, options );
        }
    }

    public static void initialize( Initializer init )
    {
        init.add( Resolver::file );
    }

    interface Initializer
    {
        void add( Class<?> type, Function<MethodHandle, MethodHandle> conversion );

        default <T> void add( Reference.BiFunction<Resolver, String, T> conversion )
        {
            MethodHandle mh = conversion.mh();
            add( mh.type().returnType(), conversion( mh ) );
        }
    }

    private static Function<MethodHandle, MethodHandle> conversion( MethodHandle filter )
    {
        return ( mh ) -> collectArguments( mh, 1, filter );
    }

    private static class Child extends Resolver
    {
        private final Path base;

        Child( Set<Path> parsedPaths, Path base )
        {
            super( parsedPaths );
            this.base = base;
        }

        @Override
        Path path( String path )
        {
            return base.resolve( path );
        }
    }
}
