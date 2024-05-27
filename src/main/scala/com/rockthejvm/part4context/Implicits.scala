package com.rockthejvm.part4context

import scala.language.implicitConversions

object Implicits {

  // given/using clauses - ability to pass arguments automatically (implicitly) by the compiler

  trait Semigroup[A] { // basically a glorified combiner
    def combine(x: A, y: A): A
  }

  def combineAll[A](list: List[A])(implicit semigroup: Semigroup[A]): A =
    list.reduce(semigroup.combine)

  implicit val intSemigroup: Semigroup[Int] = new Semigroup[Int] {
    override def combine(x: Int, y: Int): Int = x + y
  }

  val sumOf10 = combineAll((1 to 10).toList)

  // implicit arg -> using clause
  // implicit val -> given declaration

  // extension methods = implicit classes
  // implicit class -> extension methods

  implicit class MyRichInteger(number: Int) {
    // extension methods here
    def isEven = number % 2 == 0
  }

  val is23Even = 23.isEven // new MyRichInteger(23).isEven

  // implicit conversions - SUPER DANGEROUS

  case class Person(name: String) {
    def greet(): String = s"Hi, my name is $name"
  }

  implicit def string2Person(x: String): Person = Person(x)
  val danielSaysHi = "Daniel".greet() // string2Person("Daniel").greet()

  // main goal of implicit defs was to synthesize NEW implicit values
  implicit def semigroupOfOption[A](implicit semigroup: Semigroup[A]): Semigroup[Option[A]] = new Semigroup[Option[A]] {
    override def combine(x: Option[A], y: Option[A]): Option[A] = for {
      valueX <- x
      valueY <- y
    } yield semigroup.combine(valueX, valueY)
  }

  // given semigroupOfOption[A](using semigroup: Semigroup[A]): Semigroup[Option[A]] with ...

  // organizing implicits is the same as organizing contextual abstractions
  // import yourPackage.* - also imports implicits but not givens

  /*
    Why implicits were phased out:
    - the implicit keyword has many different meanings
    - conversions are easy to abuse
    - implicits are very hard to track down while debugging (givens also not trivial, but they are explicitly imported)
   */

  /*
    Contextual abstractions recommendations:
    - given/using clauses
    - extension methods
    - explicitly declared implicit conversions
   */

  def main(args: Array[String]): Unit = {
    println(sumOf10)
  }
}
