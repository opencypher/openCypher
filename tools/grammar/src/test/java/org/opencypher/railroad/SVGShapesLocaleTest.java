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

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opencypher.tools.io.Output;

import java.util.Arrays;
import java.util.Locale;
import javax.xml.stream.XMLStreamException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opencypher.railroad.FigureBuilder.branch;
import static org.opencypher.railroad.FigureBuilder.line;
import static org.opencypher.railroad.FigureBuilder.nothing;
import static org.opencypher.railroad.FigureBuilder.text;

@RunWith( Parameterized.class )
public class SVGShapesLocaleTest
{
    private Locale locale;

    @Parameterized.Parameters( name = "{0}" )
    public static Iterable<Object[]> data()
    {
        return Arrays.asList( new Object[][]{{Locale.GERMANY}, {Locale.US}, {Locale.JAPAN}, {Locale.CHINA}} );
    }

    public SVGShapesLocaleTest( Locale locale )
    {
        this.locale = locale;
    }

    @Before
    @After
    public void swapLocale()
    {
        Locale dl = Locale.getDefault();
        Locale.setDefault( locale );
        locale = dl;
    }

    @Test
    public void shouldRenderCorrectSvgRegardlessOfLocale() throws Exception
    {
        // Given
        Diagram diagram = Diagram.diagram( "", line( text( "GRAPHS" ), branch( nothing(), text( "ARE" ) ), text( "EVERYWHERE" ) ) );
        Output.Readable output = Output.stringBuilder();
        ShapeRenderer<XMLStreamException> renderer = new ShapeRenderer<>();
        Diagram.CanvasProvider<SVGShapes,XMLStreamException> canvas = SVGShapes.svgFile( name -> output );

        // When
        diagram.render( renderer, canvas );

        // Then
        assertThat( output.toString(), not( containsString( "," ) ) );
    }
}