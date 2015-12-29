package org.opencypher.tools.xml;

public interface LocationAware
{
    void location( String path, int lineNumber, int columnNumber );
}
