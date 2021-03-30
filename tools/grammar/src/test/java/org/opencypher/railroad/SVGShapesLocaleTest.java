/*
 * Copyright (c) 2015-2021 "Neo Technology,"
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opencypher.tools.io.Output;

import java.util.Arrays;
import java.util.Locale;
import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.*;
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
        assertEquals( "<?xml version=\"1.0\" ?>" +
                        "<svg:svg xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"345.013\" height=\"51.730\" style=\"stroke:black;\">" +
                        "<svg:g>" +
                        "<svg:circle r=\"2.500\" cx=\"10.500\" cy=\"16.077\" style=\"fill:none;\"></svg:circle>" +
                        "<svg:line x1=\"13.000\" y1=\"16.077\" x2=\"24.000\" y2=\"16.077\"></svg:line>" +
                        "<svg:path style=\"stroke:none;\" d=\"M 24.0 16.07666015625 L 21.0 12.57666015625 L 30.0 16.07666015625 L 21.0 19.57666015625 Z\"></svg:path>" +
                        "<svg:text x=\"39.077\" y=\"20.054\" style=\"font-family:Verdana; font-size:10px; font-weight:bold; font-style:normal; stroke:none;\" xml:space=\"preserve\">GRAPHS</svg:text>" +
                        "<svg:rect x=\"31.000\" y=\"9.000\" width=\"62.657\" height=\"14.153\" rx=\"7.077\" ry=\"7.077\" style=\"fill:none;\"></svg:rect>" +
                        "<svg:line x1=\"94.657\" y1=\"16.077\" x2=\"105.657\" y2=\"16.077\"></svg:line>" +
                        "<svg:path style=\"stroke:none;\" d=\"M 105.6572265625 16.07666015625 L 102.6572265625 12.57666015625 L 111.6572265625 16.07666015625 L 102.6572265625 19.57666015625 Z\"></svg:path>" +
                        "<svg:line x1=\"125.657\" y1=\"16.077\" x2=\"134.657\" y2=\"16.077\"></svg:line>" +
                        "<svg:line x1=\"134.657\" y1=\"16.077\" x2=\"150.442\" y2=\"16.077\"></svg:line>" +
                        "<svg:path style=\"stroke:none;\" d=\"M 153.4423828125 16.07666015625 L 150.4423828125 12.57666015625 L 159.4423828125 16.07666015625 L 150.4423828125 19.57666015625 Z\"></svg:path>" +
                        "<svg:line x1=\"159.442\" y1=\"16.077\" x2=\"179.228\" y2=\"16.077\"></svg:line>" +
                        "<svg:line x1=\"179.228\" y1=\"16.077\" x2=\"188.228\" y2=\"16.077\"></svg:line>" +
                        "<svg:path style=\"fill:none;\" d=\"M 118.6572265625 28.6533203125 A 7.0 7.0 0.0 0 0 125.6572265625 35.6533203125\"></svg:path>" +
                        "<svg:path style=\"stroke:none;\" d=\"M 128.6572265625 35.6533203125 L 125.6572265625 32.1533203125 L 134.6572265625 35.6533203125 L 125.6572265625 39.1533203125 Z\"></svg:path>" +
                        "<svg:text x=\"143.734\" y=\"39.630\" style=\"font-family:Verdana; font-size:10px; font-weight:bold; font-style:normal; stroke:none;\" xml:space=\"preserve\">ARE</svg:text>" +
                        "<svg:rect x=\"135.657\" y=\"28.577\" width=\"38.570\" height=\"14.153\" rx=\"7.077\" ry=\"7.077\" style=\"fill:none;\"></svg:rect>" +
                        "<svg:line x1=\"175.228\" y1=\"35.653\" x2=\"179.228\" y2=\"35.653\"></svg:line>" +
                        "<svg:path style=\"stroke:none;\" d=\"M 182.2275390625 35.6533203125 L 179.2275390625 32.1533203125 L 188.2275390625 35.6533203125 L 179.2275390625 39.1533203125 Z\"></svg:path>" +
                        "<svg:path style=\"fill:none;\" d=\"M 188.2275390625 35.6533203125 A 7.0 7.0 0.0 0 0 195.2275390625 28.6533203125\"></svg:path>" +
                        "<svg:path style=\"fill:none;\" d=\"M 118.6572265625 23.07666015625 A 7.0 7.0 0.0 0 0 111.6572265625 16.07666015625\"></svg:path>" +
                        "<svg:line x1=\"118.657\" y1=\"23.077\" x2=\"118.657\" y2=\"28.653\"></svg:line>" +
                        "<svg:line x1=\"111.657\" y1=\"16.077\" x2=\"125.657\" y2=\"16.077\"></svg:line>" +
                        "<svg:line x1=\"188.228\" y1=\"16.077\" x2=\"202.228\" y2=\"16.077\"></svg:line>" +
                        "<svg:path style=\"fill:none;\" d=\"M 202.2275390625 16.07666015625 A 7.0 7.0 0.0 0 0 195.2275390625 23.07666015625\"></svg:path>" +
                        "<svg:line x1=\"195.228\" y1=\"23.077\" x2=\"195.228\" y2=\"28.653\"></svg:line>" +
                        "<svg:line x1=\"202.228\" y1=\"16.077\" x2=\"213.228\" y2=\"16.077\"></svg:line>" +
                        "<svg:path style=\"stroke:none;\" d=\"M 213.2275390625 16.07666015625 L 210.2275390625 12.57666015625 L 219.2275390625 16.07666015625 L 210.2275390625 19.57666015625 Z\"></svg:path>" +
                        "<svg:text x=\"228.304\" y=\"20.054\" style=\"font-family:Verdana; font-size:10px; font-weight:bold; font-style:normal; stroke:none;\" xml:space=\"preserve\">EVERYWHERE</svg:text>" +
                        "<svg:rect x=\"220.228\" y=\"9.000\" width=\"93.785\" height=\"14.153\" rx=\"7.077\" ry=\"7.077\" style=\"fill:none;\"></svg:rect>" +
                        "<svg:line x1=\"315.013\" y1=\"16.077\" x2=\"326.013\" y2=\"16.077\"></svg:line>" +
                        "<svg:path style=\"stroke:none;\" d=\"M 326.0126953125 16.07666015625 L 323.0126953125 12.57666015625 L 332.0126953125 16.07666015625 L 323.0126953125 19.57666015625 Z\"></svg:path>" +
                        "<svg:circle r=\"2.500\" cx=\"334.513\" cy=\"16.077\" style=\"fill:none;\"></svg:circle></svg:g></svg:svg>",
                output.toString() );
    }
}