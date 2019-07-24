package org.opencypher.tools.antlr.tree;

import java.util.Collections;
import java.util.List;

public class NamedCharacterSet implements GrammarItem {

	private final String name;

	public NamedCharacterSet(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public ItemType getType() {
		return ItemType.NAMEDCHARSET;
	}

	@Override
	public List<GrammarItem> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public boolean isPlural() {
		return false;
	}

	@Override
	public GrammarItem reachThrough() {
		return this;
	}

	@Override
	public boolean isKeywordPart() {
		return false;
	}

	@Override
	public String getStructure(String indent) {
		return indent + "CharacterSet : " + name;
	}

}
