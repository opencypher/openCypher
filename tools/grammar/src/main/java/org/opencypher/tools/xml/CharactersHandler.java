package org.opencypher.tools.xml;

interface CharactersHandler
{
    void characters( Object target, char[] buffer, int start, int length );
}
