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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Fixture implements TestRule
{
    private Class<?> testClass;
    private String testName;

    public String testName()
    {
        return testName;
    }

    public static Grammar grammarResource( Class<?> testClass, String resource, Grammar.ParserOption... options )
            throws IOException, SAXException, ParserConfigurationException, URISyntaxException
    {
        return grammar( resourceURL( testClass, resource ), options );
    }

    public Grammar grammarResource( String resource, Grammar.ParserOption... options )
            throws IOException, SAXException, ParserConfigurationException, URISyntaxException
    {
        return grammar( resource( resource ), options );
    }

    public Document xmlResource( String resource ) throws TransformerException, IOException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMResult result = new DOMResult();
        URL url = resource( resource );
        transformer.transform( new StreamSource( url.openStream(), url.toString() ), result );
        return (Document) result.getNode();
    }

    public URL resource( String resource )
    {
        return resourceURL( this.testClass, resource );
    }

    private static Grammar grammar( URL resource, Grammar.ParserOption... options )
            throws IOException, SAXException, ParserConfigurationException, URISyntaxException
    {
        return Grammar.parseXML( Paths.get( resource.toURI() ), options );
    }

    private static URL resourceURL( Class<?> testClass, String resource )
    {
        URL url = testClass.getResource( resource );
        if ( url == null )
        {
            throw new IllegalArgumentException( "No such resource: " + resource );
        }
        return url;
    }

    @Override
    public Statement apply( Statement base, Description description )
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                testClass = description.getTestClass();
                testName = description.getMethodName();
                try
                {
                    base.evaluate();
                }
                finally
                {
                    testClass = null;
                    testName = null;
                }
            }
        };
    }
}
