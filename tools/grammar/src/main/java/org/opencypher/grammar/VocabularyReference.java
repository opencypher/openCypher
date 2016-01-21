package org.opencypher.grammar;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;
import org.opencypher.tools.xml.XmlFile;
import org.xml.sax.SAXException;

@Element(uri = Grammar.XML_NAMESPACE, name = "vocabulary")
final class VocabularyReference extends Located
{
    @Attribute
    XmlFile file;
    private Collection<VocabularyReference> children;

    Iterable<ProductionNode> resolve() throws IOException, SAXException, ParserConfigurationException
    {
        return file.parseOnce( Root.XML ).map( ( root ) -> {
            children = root.referencedFiles.values();
            return (Iterable<ProductionNode>) root;
        } ).orElseGet( Collections::emptyList );
    }

    void flattenTo( Map<String, VocabularyReference> references )
    {
        if ( children != null )
        {
            for ( VocabularyReference reference : children )
            {
                references.putIfAbsent( path(), reference );
            }
        }
    }

    String path()
    {
        return file.path();
    }
}
