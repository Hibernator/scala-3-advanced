package com.rockthejvm.part4context

object ExtensionMethods {

  // Enhancing and adding functionality to existing types after they've been defined

  case class Person(name: String) {
    def greet: String = s"Hi, my name is $name, nice to meet you!"
  }

  extension (string: String) def greetAsPerson: String = Person(string).greet

  val danielGreeting = "Daniel".greetAsPerson

  // generic extension extension methods with generic argument
  extension [A](list: List[A]) def ends: (A, A) = (list.head, list.last)

  val aList = List(1, 2, 3, 4)
  val firstLast = aList.ends

  // reason for extension methods: make APIs very expressive
  // reason 2: enhance CERTAIN types with new capabilities
  // => super powerful code
  trait Combinator[A] { // in math terms, it's a Semigroup
    def combine(x: A, y: A): A
  }

  // I can choose to enhance only the types that have the needed given instances in scope
  // therefore extension is also a contextual abstraction
  extension [A](list: List[A])
    def combineAll(using combinator: Combinator[A]): A =
      list.reduce(combinator.combine)

  given intCombinator: Combinator[Int] with {
    override def combine(x: Int, y: Int): Int = x + y
  }

  val firstSum = aList.combineAll // only available for List[Int], not other lists
  val someStrings = List("I", "love", "Scala")
//  val stringsSum = someStrings.combineAll // doesn't compile - no given Combinator[String] in scope

  // grouping extensions together
  object GroupedExtensions {
    extension [A](list: List[A]) {
      def ends: (A, A) = (list.head, list.last)

      def combineAll(using combinator: Combinator[A]): A =
        list.reduce(combinator.combine)
    }
  }

  // call extension methods directly on an argument
  val firstLast_v2 = ends(aList) // same as aList.ends (compiler rewrites it like this)

  /*
    Exercises
    1. Add an isPrime method to the Int type
      Should be able to write 7.isPrime
    2. Add extensions to Tree:
      - map(f: A => B): Tree[B]
      - forall(predicate: A => Boolean): Boolean
      - sum => sum of all elements of the tree. Even better, do like combineAll, to enable for tree of any type
   */

//  extension (number: Int) def isPrime: Boolean = (2 until number).forall(number % _ > 0)
  extension (number: Int)
    def isPrime: Boolean = {
      def isPrimeAux(potentialDivisor: Int): Boolean =
        if (potentialDivisor > number / 2) true
        else if (number % potentialDivisor == 0) false
        else isPrimeAux(potentialDivisor + 1)

      assert(number >= 0)
      if (number == 0 || number == 1) false
      else isPrimeAux(2)
    }

  // "library code" - I can't change it
  sealed abstract class Tree[A]
  case class Leaf[A](value: A) extends Tree[A]
  case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

  extension [A](tree: Tree[A]) {
    def map[B](f: A => B): Tree[B] =
      tree match {
        case Leaf(value)         => Leaf(f(value))
        case Branch(left, right) => Branch(left.map(f), right.map(f))
      }

    def forall(predicate: A => Boolean): Boolean =
      tree match {
        case Branch(left, right) => left.forall(predicate) && right.forall(predicate)
        case Leaf(value)         => predicate(value)
      }

    def combineAll(using combinator: Combinator[A]): A =
      tree match {
        case Leaf(value)         => value
        case Branch(left, right) => combinator.combine(left.combineAll, right.combineAll)
      }
  }

  extension (tree: Tree[Int]) { // only available for Tree[Int], not Tree containing elements of other types
    def sum: Int =
      tree match {
        case Leaf(value)         => value
        case Branch(left, right) => left.sum + right.sum
      }
  }

  val tree = Branch(Branch(Leaf(3), Leaf(1)), Leaf(10))

  def main(args: Array[String]): Unit = {
    println(danielGreeting)
    println(firstLast)
    println(firstSum)
    println(2003.isPrime)
    println(4.isPrime)
    println(17.isPrime)
    println(16.isPrime)
    println(tree.map(_ + 1))
    println(tree.forall(_ > 0))
    println(tree.forall(_ > 1))
    println(tree.combineAll)
    println(tree.sum)
  }
}
