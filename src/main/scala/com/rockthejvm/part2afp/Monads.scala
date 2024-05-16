package com.rockthejvm.part2afp

object Monads {

  def listStory(): Unit = {
    val aList = List(1, 2, 3)
    val listMultiply = for {
      x <- List(1, 2, 3)
      y <- List(4, 5, 6)
    } yield x * y
    // for-comprehensions = chains of map + flatMap

    val listMultiply_2 = List(1, 2, 3).flatMap(x => List(4, 5, 6).map(y => x * y))

    val f = (x: Int) => List(x, x + 1) // applicable for flatMap
    val g = (x: Int) => List(x, x * 2) // applicable for flatMap
    val pure = (x: Int) => List(x) // same as list "constructor"

    // properties of a list and flatMap

    // prop1: left identity
    val leftIdentity = pure(42).flatMap(f) == f(42) // for every x and for every f

    // prop 2: right identity
    val rightIdentity = aList.flatMap(pure) == aList // for every list

    // prop3: associativity
    /*
      [1, 2, 3].flatMap(x => [x, x + 1]) = [1, 2, 2, 3, 3, 4]
      [1, 2, 2, 3, 3, 4].flatMap(x => [x, x * 2]) = [1,2, 2,4,   2,4, 3,6,   3,6, 4,8]
      [1,2,3].flatMap(f).flatMap(g) = [1,2, 2,4, 2,4, 3,6, 3,6, 4,8]

      [1,2,2,4] = f(1).flatMap(g)
      [2,4,3,6] = f(2).flatMap(g)
      [3,6,4,8] = f(3).flatMap(g)
      [1,2, 2,4, 2,4, 3,6, 3,6, 4,8] = f(1).flatMap(g) ++ f(2).flatMap(g) ++ f(3).flatMap(g)

      [1,2,3].flatMap(x => f(x).flatMap(g)) == [1,2,3].flatMap(f).flatMap(g)
     */

    // associativity and flatMap guarantees that for every element of the list, f will be applied first and then g to the result of every f application
    // associativity guarantees order of application of f and g. It's guaranteed by flatMap mechanics
    val associativity = aList.flatMap(f).flatMap(g) == aList.flatMap(x => f(x).map(g))

  }

  def optionStory(): Unit = {
    val anOption = Option(42)
    val optionString = for {
      lang <- Option("scala")
      version <- Option(3)
    } yield s"$lang-$version"

    val optionString_v2 = Option("Scala").flatMap(lang => Option(3).map(ver => s"$lang-$ver"))

    val f = (x: Int) => Option(x + 1)
    val g = (x: Int) => Option(2 * x)
    val pure = (x: Int) => Option(x) // Option "constructor"

    // properties of option and flatMap

    // prop 1: left-identity
    val leftIdentity = pure(42).flatMap(f) == f(42) // for any x, for any f

    // prop 2: right-identity
    val rightIdentity = anOption.flatMap(pure) == anOption // for any Option

    // prop 3: associativity
    /*
      anOption.flatMap(f).flatMap(g) = Option(42).flatMap(x => Option(x + 1)).flatMap(x => Option(2 * x))
      = Option(43)flatMap(x => Option(2*  x))
      = Option(86)

      anOption.flatMap(x => f(x).flatMap(g)) = Option(42).flatMap(x => Option(x + 1).flatMap(y => 2 * y))
      = Option(42).flatMap(x => 2 * x + 2)
      = Option(86)
     */

    // associativity and flatMap guarantees that f and g will be applied to the option value one at a time
    val associativity = anOption.flatMap(f).flatMap(g) == anOption.flatMap(x => f(x).flatMap(g)) // for any Option, f, g
  }

  // The above 3 properties apply to wide range of data structures called Monads and are essential for them to maintain their use
  // MONADS = data structures with ability to chain dependent computations
  // Monads are data structures and computations that can wrap existing values in bigger data structures such that the flatMap method on those datatypes uphold the above properties

  def main(args: Array[String]): Unit = {}
}
