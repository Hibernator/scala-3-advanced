package com.rockthejvm.part2afp

object LazyEvaluation {

  lazy val x: Int = { // evaluation is delayed until first usage
    println("Hello")
    42
  }

  // lazy DELAYS the evaluation of a value until the first use
  // evaluation occurs ONCE

  /*
    Example 1: call by need
   */
  def byNameMethod(n: => Int): Int = n + n + n + 1

  def retrieveMagicValue(): Int = {
    println("waiting...")
    Thread.sleep(1000)
    42
  }

  def demoByName(): Unit = {
    println(byNameMethod(retrieveMagicValue())) // retrieveMagicValue() + retrieveMagicValue() + retrieveMagicValue + 1
    // the long-running computation happens 3 times which is inefficient
  }

  // call by need pattern = call by name + lazy values
  def byNeedMethod(n: => Int): Int = {
    lazy val lazyN = n
    lazyN + lazyN + lazyN + 1 // memoization
    // here, the argument was evaluated only once. The argument evaluation was delayed but not repeated
  }

  def demoByNeed(): Unit = {
    println(byNeedMethod(retrieveMagicValue()))
  }

  /*
    Example 2: withFilter
    This is actually used by for-comprehensions
   */

  def lessThan30(i: Int): Boolean = {
    println(s"$i is less than 30?")
    i < 30
  }

  def greaterThan20(i: Int): Boolean = {
    println(s"$i is greater than 20?")
    i > 20
  }

  val numbers = List(1, 25, 40, 5, 23)

  def demoFilter(): Unit = {
    val lt30 = numbers.filter(lessThan30)
    val gt20 = lt30.filter(greaterThan20)
    println(gt20)
  }

  def demoWithFilter(): Unit = {
    val lt30 = numbers.withFilter(lessThan30)
    val gt20 = lt30.withFilter(greaterThan20)
    println(gt20.map(identity))
  }

  def demoForComprehension(): Unit = {
    val forComp = for {
      n <- numbers if lessThan30(n) && greaterThan20(n)
    } yield n
    println(forComp)
  }

  def main(args: Array[String]): Unit = {
//    println(x)
//    println(x)
//    demoByName()
//    demoByNeed()
    demoFilter()
    demoWithFilter() // different order of evaluation than filter, the numbers are tested on demand
    demoForComprehension() // filters are applied in the same style as demoWithFilter
  }

}
