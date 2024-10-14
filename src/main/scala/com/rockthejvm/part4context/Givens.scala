package com.rockthejvm.part4context

object Givens {

  // list sorting
  val aList = List(4, 2, 3, 1)
  val anOrderedList = aList.sorted // (ascendingOrdering)

  given descendingOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  val anInverseOrderedList = aList.sorted(descendingOrdering)

  // custom sorting
  case class Person(name: String, age: Int)
  val people = List(Person("Alice", 29), Person("Sarah", 34), Person("Jim", 23))

  given personOrdering: Ordering[Person] = new Ordering[Person]:
    override def compare(x: Person, y: Person): Int = x.name.compareTo(y.name)

  // automatic injection of arguments by compiler
  val sortedPeople = people.sorted // (personOrdering) <- automatically passed by the compiler

  object PersonAltSyntax {
    // not interfering with the original ordering because it's in a different scope
    // alternative syntax of defining a given value that implements a trait
    given personOrdering: Ordering[Person] with {
      override def compare(x: Person, y: Person): Int = x.name.compareTo(y.name)
    }
  }

  // using clause
  trait Combinator[A] {
    def combine(x: A, y: A): A
  }

  def combineAll[A](list: List[A])(using combinator: Combinator[A]): A =
    list.reduce(combinator.combine)

  /*
    Goal:
      combineAll(List(1, 2, 3, 4))
      combineAll(people)
   */

  given intCombinator: Combinator[Int] with {
    override def combine(x: Int, y: Int): Int = x + y
  }

  val firstSum = combineAll(List(1, 2, 3, 4)) // (someCombinator[Int]) <-- passed by the compiler automatically
//  val combineAllPeople = combineAll(people) // doesn't compile - no Combinator[Person] in scope

  // combineAll can only be called in the presence of the right context (having a combinator)
  // I can make some logic available for certain types but not for others

  // context bound

  // no need to name the implicit parameter because it's not explicitly used
  def combineInGroupsOf3[A](list: List[A])(using Combinator[A]): List[A] =
    list.grouped(3).map(group => combineAll(group) /*given Combinator[A] passed by the compiler*/ ).toList

  // A : Combinator => there is a given Combinator[A] in scope
  def combineInGroupsOf3_v2[A: Combinator](list: List[A]): List[A] =
    list.grouped(3).map(group => combineAll(group) /*given Combinator[A] passed by the compiler*/ ).toList

  // synthesize new given instances based on the existing ones
  given listOrdering(using intOrdering: Ordering[Int]): Ordering[List[Int]] with {
    override def compare(x: List[Int], y: List[Int]): Int = x.sum - y.sum
  }

  val listOfLists = List(List(1, 2), List(1, 1), List(3, 4, 5))
  val nestedListsOrdered = listOfLists.sorted

  // ... with generics

  // available for any type A that has ordering and combinator defined
  given listOrderingBasedOnCombinator[A](using ord: Ordering[A])(using combinator: Combinator[A]): Ordering[List[A]]
  with {
    override def compare(x: List[A], y: List[A]): Int =
      ord.compare(combineAll(x), combineAll(y))
  }

  // pass a regular value in place of given
  val myCombinator = new Combinator[Int] {
    override def combine(x: Int, y: Int): Int = x * y
  }
  val listProduct = combineAll(List(1, 2, 3, 4))(using myCombinator)

  /*
    Exercises:
    1 - create a given for ordering Option[A] if you can order A
    2 - create a summoning method that fetches the given value of your particular type
   */

  given optionOrdering[A: Ordering]: Ordering[Option[A]] with {
    override def compare(x: Option[A], y: Option[A]): Int = (x, y) match {
      case (None, None) => 0
      case (_, None)    => 1
      case (None, _)    => -1
//      case (Some(xVal), Some(yVal)) => fetchGivenValue[Ordering[A]].compare(xVal, yVal)
      case (Some(xVal), Some(yVal)) => summon[Ordering[A]].compare(xVal, yVal)
    }
  }

  def fetchGivenValue[A](using givenValue: A): A = givenValue

  def main(args: Array[String]): Unit = {
    println(anOrderedList) // [1, 2, 3, 4] (before turning descendingOrdering to a given instance)
    println(anInverseOrderedList) // [4, 3, 2, 1]
    println(List(Option(1), Option.empty[Int], Option(3), Option(-1000)).sorted)
  }
}
