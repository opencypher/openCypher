/*
 * Copyright (c) 2015-2023 "Neo Technology,"
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.grammar;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.XmlFile;
import org.opencypher.tools.xml.XmlParser;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

abstract class ProtoGrammar
{
    static ProtoGrammar parse( Path input, XmlParser.Option... options ) throws ParserConfigurationException, SAXException, IOException
    {
        return Parsing.XML.parse( input, options );
    }

    static ProtoGrammar parse( Reader input, XmlParser.Option... options ) throws ParserConfigurationException, SAXException, IOException
    {
        return Parsing.XML.parse( input, options );
    }

    static ProtoGrammar parse( InputStream input, XmlParser.Option... options ) throws IOException, SAXException, ParserConfigurationException
    {
        return Parsing.XML.parse( input, options );
    }

    static ProtoGrammar parse( XmlFile file ) throws IOException, SAXException, ParserConfigurationException
    {
        return file.parse( Parsing.XML );
    }

    abstract String language();

    abstract ProductionNode production( String name );

    abstract void setName( String name );

    enum ResolutionOption
    {
        ALLOW_ROOTLESS,
        SKIP_UNUSED_PRODUCTIONS,
        IGNORE_UNUSED_PRODUCTIONS,
        INCLUDE_LEGACY
    }

    final Grammar resolve( Grammar.Resolver resolver, ResolutionOption... config )
    {
        Set<ResolutionOption> options = EnumSet.noneOf( ProtoGrammar.ResolutionOption.class );
        if ( config != null )
        {
            Collections.addAll( options, config );
        }
        return resolve( resolver, options );
    }

    static final Grammar.Resolver NO_RESOLVER = reference -> null;

    abstract Grammar resolve( Grammar.Resolver resolver, Set<ResolutionOption> options );

    abstract void add( ProductionNode production );

    abstract void apply( Patch patch );

    static abstract class Patch
    {
        private Conditional conditional = Conditional.NONE;

        abstract void apply( Mutator mutator );

        @Attribute( optional = true, uri = Conditional.XML_NAMESPACE )
        void given( String flag )
        {
            conditional = conditional.given( flag );
        }

        @Attribute( optional = true, uri = Conditional.XML_NAMESPACE )
        void unless( String flag )
        {
            conditional = conditional.unless( flag );
        }
    }

    static abstract class Mutator
    {
        abstract <P, EX extends Exception> void replaceProduction( String production, ProductionTransformation<P,ProductionNode,EX> transformation, P param )
                throws EX;

        abstract void removeProduction( String production );

        abstract Collection<String> productions();
    }

    private static class Parsing
    {
        static final XmlParser<ProtoGrammar> XML = XmlParser.combine( ProtoGrammar.class, Root.XML, WG3Grammar.XML, GrammarAnnotation.XML );
    }
}
