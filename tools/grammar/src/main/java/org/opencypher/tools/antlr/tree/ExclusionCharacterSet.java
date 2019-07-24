package org.opencypher.tools.antlr.tree;

import java.util.Collections;
import java.util.List;

public class ExclusionCharacterSet implements GrammarItem {

	private String characters;

	public ExclusionCharacterSet(String characters) {
		this.characters = characters;
	}

	@Override
	public ItemType getType() {
		return ItemType.EXCLUSIONCHARSET;
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
		return indent + "CharacterSet : [" + characters + "]";
	}

	public String getCharacters() {
		return characters;
	}

}
