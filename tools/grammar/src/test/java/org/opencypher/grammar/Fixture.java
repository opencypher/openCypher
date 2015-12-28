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
import org.opencypher.tools.xml.XmlParser;
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

    public Grammar grammarResource( String resource, Grammar.ParserOption... options )
            throws IOException, SAXException, ParserConfigurationException, URISyntaxException
    {
        return Grammar.parseXML( Paths.get( resource( resource ).toURI() ), options );
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
