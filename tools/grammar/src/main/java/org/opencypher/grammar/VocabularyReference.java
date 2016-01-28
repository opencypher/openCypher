/*
 * Copyright (c) 2015-2016 "Neo Technology,"
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
 */
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
