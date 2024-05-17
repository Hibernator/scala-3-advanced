package com.rockthejvm.part2afp

import scala.annotation.nowarn
import scala.annotation.targetName

@nowarn
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

  // exercise: IS THIS a MONAD? What does this monad actually mean, why would we need it?
  // answer: it is a Monad but I have to compare values produced, not lambdas
  // interpretation: ANY computation that might perform side effects
  // description of computation is separated from performance of computation
  // It's a simplified version of IO
  case class PossiblyMonad[A](unsafeRun: () => A) {
    def map[B](f: A => B): PossiblyMonad[B] =
      PossiblyMonad(() => f(unsafeRun()))

    def flatMap[B](f: A => PossiblyMonad[B]): PossiblyMonad[B] =
      PossiblyMonad(() => f(unsafeRun()).unsafeRun()) // when unsafeRun is passed to f, it's not actually performed
  }

  object PossiblyMonad {

    // In bytecode, methods accepting 0-arg lambda and methods accepting parameter by name are erased to the same type
    @targetName("pure") // has to be done to avoid the clash with the auto-generated apply method in the JVM bytecode.
    def apply[A](value: => A): PossiblyMonad[A] = // passed by value in order not to invoke unsafeRun unless necessary
      new PossiblyMonad(() => value) // new is to avoid the tailrec warning
  }

  def possiblyMonadStory(): Unit = {
    val aPossiblyMonad = PossiblyMonad(42)
    val possiblyMonadString: PossiblyMonad[String] = for {
      lang <- PossiblyMonad("Scala")
      version <- PossiblyMonad(3)
    } yield s"$lang-$version"

    val possiblyMonadString_v2: PossiblyMonad[String] =
      PossiblyMonad("Scala").flatMap(lang => PossiblyMonad(3).map(version => s"$lang-$version"))

    val f = (x: Int) => PossiblyMonad(x + 1)
    val g = (x: Int) => PossiblyMonad(2 * x)
    val pure = (x: Int) => PossiblyMonad(x) // PossiblyMonad "constructor"

    // 1. Left identity pure(x).flatMap(f) == f(x)
    val leftIdentity = pure(4).flatMap(f).unsafeRun() == f(4).unsafeRun()
    // comparing without calling unsafeRun yields false because a member of PossiblyMonad is a lambda, not a specific value
    println(leftIdentity)

    // 2. Right identity aMonad.flatMap(pure) == aMonad
    val rightIdentity = PossiblyMonad(4).flatMap(pure).unsafeRun() == PossiblyMonad(4).unsafeRun()
    println(rightIdentity)

    // 3. Associativity aMonad.flatMap(x => f(x).flatMap(g)) == aMonad.flatMap(f).flatMap(g)
    val associativity =
      PossiblyMonad(4).flatMap(x => f(x).flatMap(g)).unsafeRun() == PossiblyMonad(4).flatMap(f).flatMap(g).unsafeRun()
    println(associativity)

    val fs = (x: Int) =>
      PossiblyMonad {
        println("incrementing")
        x + 1
      }

    val gs = (x: Int) =>
      PossiblyMonad {
        println("doubling")
        x * 2
      }

    // testing not only produced result but also the order of computations
    val associativity_v3 =
      PossiblyMonad(4)
        .flatMap(x => fs(x).flatMap(gs))
        .unsafeRun() == PossiblyMonad(4).flatMap(fs).flatMap(gs).unsafeRun()
    println(associativity_v3)
  }
  // PossiblyMonad wraps a computation that can have side effects. It doesn't perform it at construction phase.

  def possiblyMonadExample(): Unit = {
    val aPossiblyMonad = PossiblyMonad {
      println("printing my first possibly monad")
      // do some computations
      42
    }

    val anotherPM = PossiblyMonad {
      println("my second PM")
      "Scala"
    }

    val aResult = aPossiblyMonad.unsafeRun()
    println(aResult)

    // Monads can be combined with map flatMap without actually performing computations
    val aForComprehension = for { // computations are DESCRIBED but not PERFORMED
      num <- aPossiblyMonad
      lang <- anotherPM
    } yield s"$num-$lang"
  }

  def main(args: Array[String]): Unit = {
    possiblyMonadStory()
    possiblyMonadExample()
  }
}
