package com.rockthejvm.part1as

object AdvancedPatternMatching {

  /*
    PM:
    - constants
    - objects
    - wildcards
    - variables
    - infix patterns
    - lists
    _ case classes
   */

  class Person(val name: String, val age: Int) {}

  // companion object optional result pattern
  /*
    - subject of matching - parameter of unapply method
    - result of matching (case) - 'objectName(tuple of type parameters of return option)'
    - matching doesn't have to be on the class in question at all, I just use the object name in the case statement
   */
  object Person {
    def unapply(person: Person): Option[(String, Int)] = // person match { case Person(string, int) => }
      if person.age < 21 then None else Some((person.name, person.age))

    def unapply(age: Int): Option[String] = // int match { case Person(string) => ...
      if age < 21 then Some("minor") else Some("legally allowed to drink")

  }

  val daniel = Person("Daniel", 102)
  val danielPM = daniel match // Person.unapply(daniel) => Option((n, a))
    case Person(n, a) => s"Hi there, I'm $n" // the values in the unapply method Option tuple are returned

  val danielsLegalStatus = daniel.age match
    case Person(status) => s"Daniel's legal drinking status is $status"

  // boolean patterns
  /*
    - multiple unapply method per object with different parameter types
    - matching subject - unapply method parameter
    - matching result - just 'object()', whether it's matched or not is based on the boolean result of unapply method
   */
  object even {
    def unapply(arg: Int): Boolean = arg % 2 == 0
    def unapply(arg: BigDecimal): Boolean = arg % 2 == 0
  }

  object singleDigit {
    def unapply(arg: Int): Boolean = arg > -10 && arg < 10
  }

  val n: Int = 43
  val mathProperty = n match {
    case even()        => "an even number"
    case singleDigit() => "a one digit number"
    case _             => "no special property"
  }

  val bigDecimalN = BigDecimal("2.0")
  val bigDecimalProperty = bigDecimalN match
    case even() => "an even bigDecimal"

  // infix patterns - for case classes with 2 members
  infix case class Or[A, B](a: A, b: B)
  val anEither = Or(2, "two")
  val humanDescriptionOfEither = anEither match {
    case number Or string => s"$number is written as $string"
  }

  val aList = List(1, 2, 3)
  val listPM = aList match
    case 1 :: rest => "a list starting with 1"
    case _         => "some uninteresting list"

  // decompose sequences
  val vararg = aList match
    case List(1, _*) => "list starting with 1"
    case _           => "some other list"

//  abstract class MyList[A] {
//    def head: A = throw new NoSuchElementException()
//    def tail: MyList[A] = throw new NoSuchElementException()
//  }
//
//  case class Empty[A]() extends MyList[A]
//  case class Cons[A](override val head: A, override val tail: MyList[A]) extends MyList[A]
//
//  object MyList {
//    // now it's a stack-recursive function, can rewrite to a tail-recursive with help of inner helper function
//    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] =
//      if list == Empty then Some(Seq.empty)
//      else unapplySeq(list.tail).map(restOfSequence => list.head +: restOfSequence)
//  }
//
//  val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty())))
//  val varargCustom = myList match {
//    case MyList(1, _*) => "list starting with 1"
//    case _             => "some other list"
//  }

  // Above, the unapply methods always return an option.
  // In fact, a type with get and isEmpty methods needs to be returned
  // 99% of time Option works well

  // custom return type for unapply
  abstract class Wrapper[T] {
    def isEmpty: Boolean
    def get: T
  }

  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
      override def isEmpty: Boolean = false
      override def get: String = person.name
    }
  }

  // during the pattern match, compiler looks at the return type of the get method
  val weirdPersonPM = daniel match
    case PersonWrapper(name) => s"Hey, my name is $name"

  def main(args: Array[String]): Unit = {
    println(danielPM)
    println(danielsLegalStatus)
    println(mathProperty)
    println(bigDecimalProperty)
  }
}
