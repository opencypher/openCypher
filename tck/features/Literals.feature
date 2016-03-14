Feature: Literals

  Background:
    Given any graph

  Scenario: Returning an integer
    When executing query: RETURN 1 AS literal
    Then the result should be:
      | literal |
      | 1       |

  Scenario: Returning a float
    When executing query: RETURN 1.0 AS literal
    Then the result should be:
      | literal |
      | 1.0     |

  Scenario: Returning a float in exponent form
    When executing query: RETURN -1e-9 AS literal
    Then the result should be:
      | literal     |
      | -.000000001 |

  Scenario: Returning a boolean`
    When executing query: RETURN true AS literal
    Then the result should be:
      | literal |
      | true    |

  Scenario: Returning a single-quoted string
    When executing query: RETURN '' AS literal
    Then the result should be:
      | literal |
      | ''      |

  Scenario: Returning a double-quoted string
    When executing query: RETURN "" AS literal
    Then the result should be:
      | literal |
      | ''      |

  Scenario: Returning null
    When executing query: RETURN null AS literal
    Then the result should be:
      | literal |
      | null    |

  Scenario: Returning an empty list
    When executing query: RETURN [] AS literal
    Then the result should be:
      | literal |
      | []      |

  Scenario: Returning a nonempty list
    When executing query: RETURN [0,1,2] AS literal
    Then the result should be:
      | literal   |
      | [0, 1, 2] |

  Scenario: Returning an empty map
    When executing query: RETURN {} AS literal
    Then the result should be:
      | literal |
      | {}      |

  Scenario: Returning a nonempty map
    When executing query: RETURN {k1: 0, k2: "string"} AS literal
    Then the result should be:
      | literal               |
      | {k1: 0, k2: 'string'} |
