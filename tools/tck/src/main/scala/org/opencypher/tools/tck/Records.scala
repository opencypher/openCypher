package org.opencypher.tools.tck

case class Records(header: List[String], rows: List[Map[String, Any]]) {

  def equalsUnordered(otherRecords: Records): Boolean = {
    def equalHeaders = header == otherRecords.header
    def equalRows = rows.toSet == otherRecords.rows.toSet
    equalHeaders && equalRows
  }

}

object Records {
  def fromRows(header: List[String], data: List[Map[String, String]]): Records = {
    val parsed = data.map(row => row.mapValues(v => CypherValue(v)).view.force)
    Records(header, parsed)
  }

  val empty = Records(List.empty, List.empty)
  def emptyWithHeader(header: List[String]) = Records(header, List.empty)
}