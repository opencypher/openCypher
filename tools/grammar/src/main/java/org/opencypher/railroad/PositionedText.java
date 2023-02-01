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
package org.opencypher.railroad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.opencypher.tools.io.Output;

class PositionedText
{
    static abstract class Renderer
            implements Diagram.Renderer<PositionedText, String, RuntimeException>,
                       Diagram.CanvasProvider<PositionedText, RuntimeException>,
                       Function<PositionedText, String>
    {
        @Override
        public PositionedText newCanvas( String name, double width, double height )
        {
            return new PositionedText();
        }

        @Override
        public String apply( PositionedText text )
        {
            Output result = Output.stringBuilder();
            text.emit( result );
            return result.toString();
        }

        @Override
        public void renderDiagram( String name, PositionedText text, Diagram.Figure root )
        {
            root.render( text, 0, 0, this, true );
        }

        @Override
        public String renderText( String type, String text )
        {
            return text;
        }
    }

    private final List<Text> text = new ArrayList<>();

    public void add( double row, double col, String text )
    {
        this.text.add( new Text( (int) row, (int) col, text ) );
    }

    public void emit( Output target )
    {
        Text[] text = this.text.toArray( new Text[0] );
        Arrays.sort( text );
        for ( int i = 0, row = 0, col = 0; i < text.length; i++ )
        {
            Text element = text[i];
            while ( row < element.row )
            {
                target.println();
                row++;
                col = 0;
            }
            while ( col < element.col )
            {
                target.append( ' ' );
                col++;
            }
            target.append( element.text );
            col += element.text.length();
        }
    }

    private static class Text implements Comparable<Text>
    {
        private final int row, col;
        private final String text;

        Text( int row, int col, String text )
        {
            this.row = row;
            this.col = col;
            this.text = text;
        }

        @Override
        public int compareTo( Text that )
        {
            int cmp = this.row - that.row;
            if ( cmp == 0 )
            {
                cmp = this.col - that.col;
            }
            return cmp;
        }
    }
}
