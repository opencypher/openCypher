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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.swing.JSVGCanvas;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.Interactive;
import org.opencypher.tools.Option;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.opencypher.tools.Option.options;

class RailRoadViewer implements Interactive.Test<RailRoadViewer.Context, Map<String, Document>>
{
    static final class Context
    {
        private final Grammar grammar;
        private final Option<Object>[] options;

        @SafeVarargs
        Context( Grammar grammar, Option<Object>... options )
        {
            this.grammar = grammar;
            this.options = options;
        }
    }

    @Override
    public Map<String, Document> suite( String className, String methodName, Context ctx ) throws XMLStreamException
    {
        ShapeRenderer<XMLStreamException> shapes = new ShapeRenderer<>( ctx.options );
        Map<String, Document> result = new HashMap<>();
        for ( Diagram diagram : Diagram.build( ctx.grammar, options( Diagram.BuilderOptions.class, ctx.options ) ) )
        {
            result.put( diagram.name(), diagram.convert( shapes, SVGShapes.SVG_DOM ) );
        }
        return result;
    }

    @Override
    public Map<String, Document> singleClass( String className, String methodName, Context test )
            throws XMLStreamException, IOException, TransformerException
    {
        Map<String, Document> documents = suite( className, methodName, test );
        for ( Map.Entry<String, Document> entry : documents.entrySet() )
        {
            printDocument( entry.getValue(), System.out );
        }
        return documents;
    }

    @Override
    public Map<String, Document> singleMethod( String className, String methodName, Context grammar )
            throws InterruptedException, XMLStreamException, IOException, TransformerException
    {
        Map<String, Document> documents = singleClass( className, methodName, grammar );
        CountDownLatch latch = new CountDownLatch( 1 );
        JFrame[] frames = new JFrame[documents.size()];
        SwingUtilities.invokeLater( () -> {
            try
            {
                int i = 0;
                for ( Map.Entry<String, Document> entry : documents.entrySet() )
                {
                    frames[i++] = frame( latch, String.format( "%s - %s#%s()", entry.getKey(), className, methodName ),
                                         entry.getValue() );
                }
                for ( JFrame frame : frames )
                {
                    frame.setVisible( true );
                }
            }
            catch ( Throwable e )
            {
                latch.countDown();
                throw e;
            }
        } );
        latch.await();
        SwingUtilities.invokeLater( () -> {
            for ( JFrame frame : frames )
            {
                if ( frame != null )
                {
                    frame.setVisible( false );
                    frame.dispose();
                }
            }
        } );
        return documents;
    }

    static JFrame frame( CountDownLatch latch, String title, Document document )
    {
        return frame( latch, title, () -> canvas( document ) );
    }

    static JFrame frame( CountDownLatch latch, String title, AwtShapes shapes )
    {
        return frame( latch, title, () -> {
            return new JPanel()
            {
                @Override
                public Dimension getPreferredSize()
                {
                    return new Dimension( (int) shapes.getWidth(), (int) shapes.getHeight() );
                }

                @Override
                protected void paintComponent( Graphics g )
                {
                    super.paintComponent( g );
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHints(
                            new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );
                    shapes.render( g2d );
                }
            };
        } );
    }

    private static JFrame frame( CountDownLatch latch, String title, Supplier<JComponent> canvas )
    {
        JFrame frame = new JFrame( title );
        frame.add( canvas.get(), BorderLayout.CENTER );
        frame.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                latch.countDown();
            }
        } );
        frame.add( button( "Done", e -> latch.countDown() ), BorderLayout.PAGE_END );
        frame.pack();
        return frame;
    }

    private static JButton button( String title, ActionListener listener )
    {
        JButton button = new JButton( title );
        button.addActionListener( listener );
        return button;
    }

    private static JComponent canvas( Document document )
    {
        JSVGCanvas canvas = new JSVGCanvas();
        canvas.setDocument( document );
        Element root = document.getDocumentElement();
        if ( root != null )
        {
            String width = root.getAttribute( "width" ), height = root.getAttribute( "height" );
            if ( width != null && !width.isEmpty() && height != null && !height.isEmpty() )
            {
                try
                {
                    canvas.setPreferredSize( new Dimension(
                            (int) Double.parseDouble( width ), (int) Double.parseDouble( height ) ) );
                }
                catch ( NumberFormatException e )
                {
                    // ok, we did our best, leave it be.
                }
            }
        }
        return canvas;
    }

    static void printDocument( Document doc, OutputStream out ) throws IOException, TransformerException
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
        transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
        transformer.transform( new DOMSource( doc ), new StreamResult( new OutputStreamWriter( out, "UTF-8" ) ) );
    }
}
