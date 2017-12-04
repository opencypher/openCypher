package org.opencypher.tools.tck

import org.scalatest.{FunSuite, Matchers}

class CypherValueVisitorTest extends FunSuite with Matchers {

  test("unlabelled node") {
    val string = "()"
    val parsed = CypherValue(string)
    val expected = CypherNode(Set.empty, CypherPropertyMap(Map.empty))
    parsed should equal(expected)
  }


  test("node") {
    val string = "(:A {name: 'Hans'})"
    val parsed = CypherValue(string)
    val expected = CypherNode(Set("A"), CypherPropertyMap(Map("name" -> CypherString("Hans"))))
    parsed should equal(expected)
  }

  test("relationship") {
    val string = "[:A {since: 1920}]"
    val parsed = CypherValue(string)
    val expected = CypherRelationship("A", CypherPropertyMap(Map("since" -> CypherInteger(1920))))
    parsed should equal(expected)
  }

  test("scalars") {
    CypherValue("true") should equal(CypherBoolean(true))
    CypherValue("false") should equal(CypherBoolean(false))
    CypherValue("-1") should equal(CypherInteger(-1))
    CypherValue("-1.0") should equal(CypherFloat(-1.0))
    CypherValue("'true'") should equal(CypherString("true"))
    CypherValue("''") should equal(CypherString(""))
    CypherValue("'-1'") should equal(CypherString("-1"))
    CypherValue("null") should equal(CypherNull)
  }

  test("path with a single node") {
    val string = "<()>"
    val parsed = CypherValue(string)
    val expected = CypherPath(CypherNode())
    parsed should equal(expected)
  }

  test("complex path") {
    val string = "<({a: true})-[:R]->(:A)<-[:T {b: 'true'}]-()>"
    val parsed = CypherValue(string)
    val expected = CypherPath(CypherNode(
      properties = CypherPropertyMap(Map("a" -> CypherBoolean(true)))),
      List(
        Connection(
          CypherNode(properties = CypherPropertyMap(Map("a" -> CypherBoolean(true)))),
          CypherRelationship("R"),
          CypherNode(Set("A"))),
        Connection(
          CypherNode(),
          CypherRelationship("T",CypherPropertyMap(Map("b" -> CypherString("true")))),
          CypherNode(Set("A"))
        )
      )
    )
    parsed should equal(expected)
  }

  test("map") {
    CypherValue("{}") should equal(CypherPropertyMap())
    CypherValue("{name: 'Hello', foo: true}") should equal(
      CypherPropertyMap(Map("name" -> CypherString("Hello"), "foo" -> CypherBoolean(true))))
  }

  test("list") {
    CypherValue("[]") should equal(CypherOrderedList())
    CypherValue("[1, 2, null]") should equal(
      CypherOrderedList(List(CypherInteger(1), CypherInteger(2), CypherNull)))
    CypherValue("[]", orderedLists = false) should equal(CypherUnorderedList())
    CypherValue("[1, 2, null]", orderedLists = false) should equal(
      CypherUnorderedList(Set(CypherInteger(1), CypherInteger(2), CypherNull)))
  }

}
