package org.opencypher.tools.xml;

public interface LocationAware
{
    void location( String systemId, int lineNumber, int columnNumber );
}
