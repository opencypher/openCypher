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
    package org.opencypher.tools.g4tree;

import java.util.List;

/**
 * an item in a grammar.
 */
public interface GrammarItem
{
    enum ItemType {
        ALTERNATIVE,
        ALTERNATIVES,
        LITERAL,
        CARDINALITY,
        REFERENCE,
        TEXT,
        OTHER,
        RULE, 
        CHARACTER_LITERAL,
        BNF_LITERAL,
        EOI, 
        NAMEDCHARSET, 
        LISTEDCHARSET,
        EXCLUSIONCHARSET
    }
    
    /**
     * what type, in terms of how to process its conversion to main grammar
     * @return
     */
    ItemType getType();

    /**
     * get the child items as a list. Terminals will return an empty list, non-lists will return 
     * a list of one.
     * @return
     */
    List<GrammarItem> getChildren();
    /**
     * are there more than one of these items. Some serialisations will require brackets of some kind if there are
     * @return true if there are multiple of these items.
     */
    boolean isPlural();

    /**
     * if this item is a wrapper round a single item, get that one. A wrapper may affect cardinality, optionality etc.
     * @return the wrapped item, or this item itself it if isn't wrapper.
     */
    GrammarItem reachThrough();

    
    /**
     * Could this be part or all of a keyword ? Is either a letter or all of its parts are letters
     * @return true iff this could be part of a keyword
     * 
     */
    boolean isKeywordPart();
    
    /**
     * Reserialise showing the internal structure of the grammar. This is based on the 
     * rules of the "orignal BNF" g4, and may not correspond to how the user thinks of the
     * structure
     * @param indent how much to indent each level of the structure
     * @return a String showing the internals and values in the grammar
     */
    String getStructure(String indent);
    
    /**
     * default indentation
     */
    static final String INDENT = "   ";

}
