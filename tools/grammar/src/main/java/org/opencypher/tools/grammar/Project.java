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
package org.opencypher.tools.grammar;

import org.opencypher.grammar.Conditional;
import org.opencypher.grammar.Grammar;
import org.opencypher.tools.io.Output;
import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;
import org.opencypher.tools.xml.XmlFile;
import org.opencypher.tools.xml.XmlParser;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import static org.opencypher.tools.Reflection.pathOf;

/**
 * Used for specifying a "project" in xml, to define a set of tools to run on a (set of) grammar(s).
 *
 * Example:
 * <code><pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;project xmlns="http://opencypher.org/grammar/project"&gt;
 *   &lt;grammar id="g1" path="grammars/my-grammar.xml"/&gt;
 *   &lt;grammar id="g-two" path="grammars/another-grammar.xml"/&gt;
 *
 *   &lt;output tool="{@linkplain RailRoadDiagramPages RAIL_ROAD_DIAGRAM_PAGES}" grammar="g1" path="out-dir/rr"&gt;
 *     &lt;option key="{@linkplain RailRoadDiagramPages.Options#productionDetailsLink() productionDetailsLink}" value="my-language.pdf#{1}"/&gt;
 *   &lt;/output&gt;
 *
 *   &lt;output tool="{@linkplain SQLBNF WG3BNF}" grammar="g1" path="out-dir/g1.bnf"/&gt;
 *   &lt;output tool="{@linkplain ISO14977 ISO14977}" grammar="g-two" path="out-dir/g-two.bnf"/&gt;
 *   &lt;output tool="{@linkplain Antlr4 ANTLR4}" grammar="g-two" path="out-dir/g-two.g"/&gt;
 *
 *   &lt;output tool="{@linkplain ParseTrees PARSE_TREE}" grammar="g-two" path="out-dir/parse-trees"&gt;
 *     &lt;option key="{@linkplain Project.Tool#log(Map) log}" value="out-dir/parse-tree-gen.log"/&gt;
 *     &lt;option key="{@linkplain ParseTrees.Options#sources() sources}" value="source-samples/g-two/"/&gt;
 *     &lt;option key="{@linkplain ParseTrees.Options#rootProduction() rootProduction}" value="grammar root"/&gt;
 *   &lt;/output&gt;
 * &lt;/project&gt;
 * </pre></code>
 */
@Element( uri = Project.NAMESPACE, name = "project" )
class Project
{
    static final String NAMESPACE = Grammar.XML_NAMESPACE + "/project";
    static final XmlParser<Project> XML = XmlParser.xmlParser( Project.class );

    public static void main( String... args ) throws Exception
    {
        if ( args == null || args.length < 1 )
        {
            String path = pathOf( Main.class );
            if ( new File( path ).isFile() && path.endsWith( ".jar" ) )
            {
                System.err.printf( "USAGE: java -jar %s Project <project.xml> ...%n", path );
            }
            else
            {
                System.err.printf( "USAGE: java -cp %s %s Project <project.xml> ...%n", path, Main.class.getName() );
            }
            System.exit( 1 );
        }
        else
        {
            Path xml = Path.of( args[0] );
            if ( !Files.isRegularFile( xml ) )
            {
                System.err.println( "No such file: " + args[0] );
                System.exit( 1 );
            }
            Map<String,String> options = new HashMap<>();
            for ( int i = 1; i < args.length; i++ )
            {
                int eq = args[i].indexOf( '=' );
                if ( eq < 0 )
                {
                    options.put( args[i], null );
                }
                else
                {
                    options.put( args[i].substring( 0, eq ), args[i].substring( eq + 1 ) );
                }
            }
            XML.parse( xml ).execute( xml.getParent(), options );
        }
    }

    private final List<Source> sources = new ArrayList<>();
    private final List<Target> targets = new ArrayList<>();

    @Child
    void input( Source input )
    {
        sources.add( input );
    }

    @Child
    void output( Target output )
    {
        targets.add( output );
    }

    void execute( Path workingDir, Map<String,String> options ) throws Exception
    {
        // TODO: use 'options' to inject config/parameters into the structure parsed from the XML
        for ( Task task : work( grammars() ) )
        {
            task.execute( workingDir );
        }
    }

    private Map<String,Grammar> grammars() throws ParserConfigurationException, SAXException, IOException
    {
        Map<String,Grammar.Unresolved> resolution = new HashMap<>();
        Grammar.Unresolved[] prestage = new Grammar.Unresolved[sources.size()];
        for ( int i = 0; i < prestage.length; i++ )
        {
            Source source = sources.get( i );
            resolution.put( source.id, prestage[i] = Grammar.Unresolved.parseXML( source.path ) );
        }
        Map<String,Grammar> grammars = new HashMap<>();
        for ( int i = 0; i < prestage.length; i++ )
        {
            Source source = sources.get( i );
            grammars.put( source.id, prestage[i].resolve( source.resolver( resolution ) ) );
        }
        return grammars;
    }

    private Task[] work( Map<String,Grammar> grammars )
    {
        Task[] work = new Task[targets.size()];
        Map<String,Map<Tool,List<Task>>> tasks = new HashMap<>();
        for ( int i = 0; i < work.length; i++ )
        {
            Target target = targets.get( i );
            Grammar grammar = grammars.get( target.grammar );
            if ( grammar == null )
            {
                throw new IllegalArgumentException( "No such grammar: " + target.grammar );
            }
            tasks.computeIfAbsent( target.grammar, key -> new EnumMap<>( Tool.class ) )
                    .computeIfAbsent( target.tool, key -> new ArrayList<>() )
                    .add( work[i] = new Task( grammar, target.tool, target.path, target.options, tasks ) );
        }
        return work;
    }

    @Element( uri = Project.NAMESPACE, name = "grammar" )
    static class Source
    {
        @Attribute
        String id;
        @Attribute
        XmlFile path;
        final List<ReferenceFormat> references = new ArrayList<>();
        final Set<String> flags = new HashSet<>();

        @Child( {WG3ReferenceFormat.class} )
        void reference( ReferenceFormat reference )
        {
            references.add( reference );
        }

        @Child
        void flag( Flag flag )
        {
            flags.add( flag.name );
        }

        Grammar.Resolver resolver( Map<String,Grammar.Unresolved> grammars )
        {
            Grammar.Resolver[] resolvers = new Grammar.Resolver[references.size()];
            for ( int i = 0; i < resolvers.length; i++ )
            {
                resolvers[i] = references.get( i ).resolver( grammars );
            }
            return Grammar.Resolver.combine( resolvers );
        }
    }

    @Element( uri = Project.NAMESPACE, name = "output" )
    static class Target
    {
        @Attribute
        Tool tool;
        @Attribute
        String grammar;
        @Attribute
        Path path;
        final Map<String,Object> options = new HashMap<>();

        @Child
        void option( Option option )
        {
            tool.addOption( options, option.key, option.value );
        }
    }

    @Element( uri = Conditional.XML_NAMESPACE, name = "flag" )
    static class Flag
    {
        @Attribute
        String name;
    }

    @Element( uri = Project.NAMESPACE, name = "option" )
    static class Option
    {
        @Attribute
        String key;
        @Attribute
        String value;
    }

    static abstract class ReferenceFormat
    {
        abstract Grammar.Resolver resolver( Map<String,Grammar.Unresolved> grammars );
    }

    @Element( uri = Project.NAMESPACE, name = "WG3Reference" )
    static class WG3ReferenceFormat extends ReferenceFormat
    {
        @Attribute( optional = true )
        String standard, part;
        @Attribute
        String resolves;

        @Override
        Grammar.Resolver.WG3 resolver( Map<String,Grammar.Unresolved> grammars )
        {
            Grammar.Unresolved grammar = grammars.get( resolves );
            if ( grammar == null )
            {
                throw new IllegalArgumentException( "Undefined grammar: " + resolves );
            }
            return ( standard, part, nonTerminal ) ->
                    Objects.equals( WG3ReferenceFormat.this.standard, standard ) && Objects.equals( WG3ReferenceFormat.this.part, part )
                    ? grammar.production( nonTerminal ) : null;
        }
    }

    enum Tool
    {
        RAIL_ROAD_DIAGRAM_PAGES( RailRoadDiagramPages.class ) // <pre>
        {// </pre>

            @Override
            void execute( Grammar grammar, Path workingDir, Path path, Map<String,Object> options ) throws Exception
            {
                options.put( reKey( "outputDir" ), path );
                RailRoadDiagramPages.generate( grammar, workingDir, log( options ), options );
            }
        },
        WG3BNF( SQLBNF.class ) // <pre>
        {// </pre>

            @Override
            void execute( Grammar grammar, Path workingDir, Path path, Map<String,Object> options ) throws Exception
            {
                write( grammar, workingDir, SQLBNF::write, path );
            }
        },
        ISO14977( org.opencypher.tools.grammar.ISO14977.class ) // <pre>
        {// </pre>

            @Override
            void execute( Grammar grammar, Path workingDir, Path path, Map<String,Object> options ) throws Exception
            {
                write( grammar, workingDir, org.opencypher.tools.grammar.ISO14977::write, path );
            }
        },
        ANTLR4( Antlr4.class ) // <pre>
        {// </pre>

            @Override
            void execute( Grammar grammar, Path workingDir, Path path, Map<String,Object> options ) throws Exception
            {
                write( grammar, workingDir, Antlr4::write, path );
            }
        },
        PARSE_TREES( ParseTrees.class ) // <pre>
        {// </pre>

            @Override
            void execute( Grammar grammar, Path workingDir, Path path, Map<String,Object> options ) throws Exception
            {
                options.put( reKey( "outputDir" ), path );
                new ParseTrees( workingDir, options ).generate( grammar, log( options ) );
            }
        };

        private final String keyPrefix;

        Tool( Class<?> tool )
        {
            this.keyPrefix = tool.getSimpleName();
        }

        void addOption( Map<String,Object> options, String key, String value )
        {
            options.put( key, value );
        }

        abstract void execute( Grammar grammar, Path workingDir, Path path, Map<String,Object> options ) throws Exception;

        String reKey( String key )
        {
            return keyPrefix + "." + key;
        }

        Path path( Map<String,Object> options, String key )
        {
            Object path = options.get( reKey( key ) );
            if ( path instanceof String )
            {
                path = Path.of( (String) path );
            }
            return (Path) path;
        }

        Output log( Map<String,Object> options )
        {
            Object log = options.get( reKey( "log" ) );
            if ( log instanceof String )
            {
                log = Output.output( Path.of( (String) log ) );
            }
            else if ( log == null )
            {
                log = Output.stdOut();
            }
            return (Output) log;
        }

        void write( Grammar grammar, Path workingDir, Executor tool, Path path ) throws IOException
        {
            try ( OutputStream out = Files.newOutputStream( path ) )
            {
                tool.execute( grammar, workingDir, out );
            }
        }

        private interface Executor
        {
            void execute( Grammar grammar, Path workingDir, OutputStream out );
        }
    }

    private static class Task
    {
        private final Grammar grammar;
        private final Tool tool;
        private final Path path;
        private final Map<String,Object> options = new HashMap<>();
        private final Map<String,Map<Tool,List<Task>>> tasks;

        Task( Grammar grammar, Tool tool, Path path, Map<String,Object> options, Map<String,Map<Tool,List<Task>>> tasks )
        {
            this.grammar = grammar;
            this.tool = tool;
            this.path = path;
            this.tasks = tasks;
            for ( Map.Entry<String,Object> option : options.entrySet() )
            {
                this.options.put( tool.reKey( option.getKey() ), option.getValue() );
            }
        }

        void execute( Path workingDir )
        {
            try
            {
                tool.execute( grammar, workingDir, path, options );
            }
            catch ( Exception e )
            {
                System.err.printf( "Failed to execute %s on %s.%n", tool.name(), grammar.language() );
                e.printStackTrace();
            }
        }
    }
}
