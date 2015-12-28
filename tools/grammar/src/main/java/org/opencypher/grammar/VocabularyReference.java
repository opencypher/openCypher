package org.opencypher.grammar;

import java.io.IOException;
import java.util.Collections;
import javax.xml.parsers.ParserConfigurationException;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Element;
import org.opencypher.tools.xml.XmlFile;
import org.xml.sax.SAXException;

@Element(uri = Grammar.XML_NAMESPACE, name = "vocabulary")
class VocabularyReference
{
    @Attribute
    XmlFile file;

    Iterable<Production> resolve() throws IOException, SAXException, ParserConfigurationException
    {
        return file.parseOnce( Root.XML, Collections::emptyList );
    }
}
