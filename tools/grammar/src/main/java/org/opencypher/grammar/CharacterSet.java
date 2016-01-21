package org.opencypher.grammar;

import java.util.List;

public interface CharacterSet
{
    String setName();

    List<Exclusion> exclusions();

    default boolean hasExclusions()
    {
        return !exclusions().isEmpty();
    }
}
