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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * a list of items of some kind.  Reserialisations will often require some separator between items - the "glue".
 */
abstract class GrammarItemList implements GrammarItem
{

    private final List<GrammarItem> items = new ArrayList<>();
    
    @Override
    public List<GrammarItem> getChildren() {
        return items;
    }

    public int size() {
        return items.size();
    }
    
    public void addItem(GrammarItem grammarItem) {
        items.add(grammarItem);
    }

    protected GrammarItem getItem(int index) {
        return items.get(index);
    }
    
    
    @Override
    public boolean isPlural()
    {
        if (items.size() > 1) {
            return true;
        } else if (items.size() == 1) {
            return items.get(0).isPlural();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return items.stream().map(it -> it.getStructure(" ")).collect(Collectors.joining(", "));
    }

    public String getContentStructure(String indent)
    {
        return items.stream().map(it -> it.getStructure(indent + INDENT)).collect(Collectors.joining("\n"));
    }


    @Override
    public GrammarItem reachThrough()
    {
        if (items.size() != 1) {
            return this;
        } else {
            return items.get(0).reachThrough();
        }
    }
    

    @Override
    public boolean isKeywordPart() {
        for (GrammarItem grammarItem : items) {
            if (! grammarItem.isKeywordPart()) {
                return false;
            }
        }
        return true;
    }
}
