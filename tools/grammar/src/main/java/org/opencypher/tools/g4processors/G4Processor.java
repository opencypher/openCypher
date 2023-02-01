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
  package org.opencypher.tools.g4processors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opencypher.grammar.Grammar;
import org.opencypher.grammar.GrammarConverter;
import org.opencypher.tools.antlr.g4.Gee4Lexer;
import org.opencypher.tools.antlr.g4.Gee4Parser;
import org.opencypher.tools.g4tree.GrammarTop;


public class G4Processor
{
    public Grammar processString(String inString) {
        return processStream(new ByteArrayInputStream((inString).getBytes()) );
    }
    
    public Grammar processStream(InputStream inStream)
    {
        try
            {
            return processAntlrStream( CharStreams.fromStream(inStream));
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to read or convert java.io.InputStream", e);
        }
    }

    public Grammar processFile(String fileName)
    {
        try
        {
            // when back on antlr 4.7.1, use CharStreams.fromFileName(scriptFile)
            return processAntlrStream(CharStreams.fromFileName(fileName));
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to find or read " + fileName, e);
        }

    }

    private Grammar processAntlrStream(CharStream inStream)
    {
        Gee4Lexer lexer = new Gee4Lexer(inStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Gee4Parser parser = new Gee4Parser(tokens);
        // leaving the old listeners in gives a nice error message
        // parser.removeErrorListeners();
        // lexer.removeErrorListeners();
        lexer.addErrorListener(new FailingErrorListener());
        parser.addErrorListener(new FailingErrorListener());

        ParseTree tree = parser.wholegrammar();

        ParseTreeWalker walker = new ParseTreeWalker();
        G4Listener listener = new G4Listener(tokens);
        walker.walk(listener, tree);
        
        GrammarTop itemTree = listener.getTreeTop();
//        LOGGER.warn("bnf gave {}", itemTree.getStructure(""));
        // convert to openCypher grammar
        GrammarConverter converter = new GrammarConverter(itemTree);
        return converter.convert();
    }
}
