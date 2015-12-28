package org.opencypher.tools.xml;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public final class XmlFile
{
    private final Resolver resolver;
    private final Path path;

    XmlFile( Resolver resolver, Path path )
    {
        this.resolver = resolver;
        this.path = path;
    }

    public <T> T parse( XmlParser<T> parser )
            throws ParserConfigurationException, SAXException, IOException
    {
        return resolver.parse( path, parser );
    }

    public <T> T parseOnce( XmlParser<? extends T> parser, Supplier<? extends T> onSubsequentParse )
            throws IOException, SAXException, ParserConfigurationException
    {
        return resolver.parsed( path ) ? onSubsequentParse.get() : parse( parser );
    }
}
