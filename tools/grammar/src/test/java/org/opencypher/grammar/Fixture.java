package org.opencypher.grammar;

import java.io.IOException;
import java.io.InputStream;
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

    public Grammar.Builder grammar()
    {
        return Grammar.grammar( testName() );
    }

    public Grammar grammarResource( String resource, XmlParser.Option... options )
            throws IOException, SAXException, ParserConfigurationException
    {
        return Grammar.parseXML( resourceStream( resource ), options );
    }

    public Document xmlResource( String resource ) throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMResult result = new DOMResult();
        transformer.transform( new StreamSource( resourceStream( resource ) ), result );
        return (Document) result.getNode();
    }

    public InputStream resourceStream( String resource )
    {
        InputStream stream = testClass.getResourceAsStream( resource );
        if ( stream == null )
        {
            throw new IllegalArgumentException( "No such resource: " + resource );
        }
        return stream;
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
