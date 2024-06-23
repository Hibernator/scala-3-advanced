package com.rockthejvm.part2afp

object CurryingPAFs {

  // currying
  val superAdder: Int => Int => Int = x => y => x + y

  // HOF that takes one argument and returns another function
  val add3: Int => Int = superAdder(3) // y => 3 + y
  val eight = add3(5) // 8
  val eight_v2 = superAdder(3)(5)

  // curried methods
  def curriedAdder(x: Int)(y: Int): Int = x + y

  // invoking method or function with only 1 parameter is a partial application

  // methods != function values but Scala allows conversion from method to function value - eta-expansion

  // converting methods to functions = eta-expansion
  val add4: Int => Int = curriedAdder(4) // eta-expansion, turns a method into a function value
  val nine = add4(9) // 9

  def increment(x: Int): Int = x + 1
  val aList = List(1, 2, 3)
  val anIncrementedList = aList.map(increment) // also eta-expansion here

  // partial applications with underscores, I can control eta-expansions
  def concatenator(a: String, b: String, c: String): String = a + b + c

  // Like this, a method can be converted into a partially applied function by specifying only some arguments
  val insertName: String => String =
    concatenator(
      "Hello, my name is ",
      _: String,
      " I'm going to show you a nice Scala trick"
    ) // x => concatenator("...", x, "...")

  val danielsGreeting: String = insertName("Daniel") // concatenator("...", "Daniel", "...")

  // (x, y) => concatenator(x, "Daniel", y)
  val fillInBlanks: (String, String) => String = concatenator(_: String, "Daniel", _: String)
  val danielsGreeting_v2: String = fillInBlanks("Hi, ", "how are you?")

  /** Exercises
    *   1. Create as many add7 definitions
    */
  val simpleAddFunction: (Int, Int) => Int = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int): Int = x + y
  def curriedMethod(x: Int)(y: Int): Int = x + y

  // 1 - obtain an add7 function: x => x + 7 out of these 3 definitions
  val add7: Int => Int = (x: Int) => simpleAddFunction(x, 7)
  val add7_1: Int => Int = (x: Int) => simpleAddFunction(7, x)
  val add7_2: Int => Int = simpleAddMethod(7, _)
  val add7_3: Int => Int = simpleAddMethod(_, 7)
  val add7_4: Int => Int = curriedMethod(7)
  val add7_5: Int => Int = curriedMethod(_: Int)(7)
  val add7_6: Int => Int = (x: Int) => simpleAddMethod(x, 7)
  val add7_7: Int => Int = (x: Int) => curriedMethod(7)(x)
  val add7_8: Int => Int = simpleAddFunction.curried(7)

  // 2 - process a list of numbers and return their string representations under different formats
  // step 1: create a curried formatting method with a formatting string and a value
  // step 2: process a list of numbers with various formats
  val piwith2Dec: String = "%8.6f".format(Math.PI)

  def curriedFormatter(fmt: String)(number: Double): String = fmt.format(number)
  val someDecimals = List(Math.PI, Math.E, 1, 9.8, 1.3e-12)

  // methods vs functions + by-name vs 0-lambdas
  def byName(n: => Int): Int = n + 1
  def byLambda(f: () => Int): Int = f() + 1

  def method: Int = 42 // method without argument list
  def parenMethod(): Int = 42 // method with argument list but no arguments inside

  byName(23) // ok
  byName(method) // 43. eta-expanded? NO - method is INVOKED here
  byName(parenMethod()) // 43. simple
//  byName(parenMethod) // not ok, will not compile
  byName((() => 42)()) // ok, I passed a lambda and invoked it right away
//  byName(() => 42) // not ok, because I passed an instance of Function1 but not invoked it

//  byLambda(23) // not ok, not a lambda
//  byLambda(method) // not ok, will not be eta-expanded into a 0-argument lambda because it has no arguments at all
  byLambda(parenMethod) // eta-expansion works
  byLambda(() => 42) // directly compatible with the argument type
  byLambda(() => parenMethod()) // ok, this is how compiler rewrites line 87

  def main(args: Array[String]): Unit = {
    println(piwith2Dec)
    println(someDecimals.map(curriedFormatter("%4.2f")))
    println(someDecimals.map(curriedFormatter("%8.6f")))
    println(someDecimals.map(curriedFormatter("%16.14f")))
  }

}
