Feature: Literals

  Scenario: Returning an integer
    Given any graph
    When executing query: RETURN 1 AS literal
    Then expect result:
      | literal |
      | 1       |

  Scenario: Returning a float
    Given any graph
    When executing query: RETURN 1.0 AS literal
    Then expect result:
      | literal |
      | 1.0     |

  Scenario: Returning a float in exponent form
    Given any graph
    When executing query: RETURN 1e-10 AS literal
    Then expect result:
      | literal    |
      | -.00000001 |

  Scenario: Returning a boolean`
    Given any graph
    When executing query: RETURN true AS literal
    Then expect result:
      | literal |
      | true    |

  Scenario: Returning a single-quoted string
    Given empty graph
    When executing query: RETURN '' AS literal
    Then expect result:
      | literal |
      | ''      |

  Scenario: Returning a double-quoted string
    Given any graph
    When executing query: RETURN "" AS literal
    Then expect result:
      | literal |
      | ''      |

  Scenario: Returning null
    Given any graph
    When executing query: RETURN null AS literal
    Then expect result:
      | literal |
      | null    |

  Scenario: Returning an empty list
    Given any graph
    When executing query: RETURN [] AS literal
    Then expect result:
      | literal |
      | []      |

  Scenario: Returning a nonempty list
    Given any graph
    When executing query: RETURN [0,1,2] AS literal
    Then expect result:
      | literal   |
      | [0, 1, 2] |

  Scenario: Returning an empty map
    Given any graph
    When executing query: RETURN {} AS literal
    Then expect result:
      | literal |
      | {}      |

  Scenario: Returning a nonempty map
    Given any graph
    When executing query: RETURN {k1: 0, k2: "string"} AS literal
    Then expect result:
      | literal             |
      | {k1:0, k2:'string'} |
