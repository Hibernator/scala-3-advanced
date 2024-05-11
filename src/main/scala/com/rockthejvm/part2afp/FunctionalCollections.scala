package com.rockthejvm.part2afp

object FunctionalCollections {

  // Most collections in Scala are (partial) functions

  // Sets are functions of "contains" (total function)
  val aSet: Set[String] = Set("I", "love", "Scala")
  val setContainsScala = aSet("Scala") // true

  // Seq are functions of PartialFunction[Int, A]. Not a total function
  val aSeq: Seq[Int] = Seq(1, 2, 3, 4)
  val anElement = aSeq(2) // 3
//  val aNonExistingElement = aSeq(100) // throws an OOBException (out of bounds)

  // Map[K, V] "extends" PartialFunction[K, V]
  val aPhonebook: Map[String, Int] = Map(
    "Alice" -> 123456,
    "Bob" -> 987654
  )
  val alicesPhone = aPhonebook("Alice")
//  val danielsPhone = aPhonebook("Daniel") // throws NoSuchElementException

  def main(args: Array[String]): Unit = {}
}
