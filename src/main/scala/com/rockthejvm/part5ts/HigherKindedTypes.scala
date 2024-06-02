package com.rockthejvm.part5ts

import scala.util.Try

object HigherKindedTypes {

  // in essence, it's a generic type where some of the type arguments are also generic

  class HigherKindedType[F[_]] // higher-kinded type
  class HigherKindedType2[F[_], G[_], A]

  val higherKindedExample = new HigherKindedType[List]
  val higherKindedExample2 = new HigherKindedType2[List, Option, String]

  // can use hkts for methods as well - see functor example below

  // why are they useful: abstract libraries, e.g. Cats

  // example: Functor
  val aList = List(1, 2, 3)
  val anOption = Option(2)
  val aTry = Try(42)

  val anIncrementedList = aList.map(_ + 1) // List(2, 3, 4)
  val anIncrementedOption = anOption.map(_ + 1) // Some(3)
  val anIncrementedTry = aTry.map(_ + 1) // Success(43)

  // Functor: the ability of mapping values in a data structure through a function and obtaining a new structure of the same type
  // In Scala, Functor is type class which gives the capability of mapping value in a data structure through a function and obtaining a data structure of the same type

  // "duplicated" APIs
  def do10xList(list: List[Int]): List[Int] = list.map(_ * 10)
  def do10xOption(option: Option[Int]): Option[Int] = option.map(_ * 10)
  def do10xTry(theTry: Try[Int]): Try[Int] = theTry.map(_ * 10)

  // DRY principle - above code violates it

  // Solution - higher-kinded type class

  // step 1: type class definition (trait)
  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
    // specific example - map[A, B](listA: List[A])(f: A => B): List[B]
  }

  // step 2: type class instances as given values
  given listFunctor: Functor[List] with {
    override def map[A, B](list: List[A])(f: A => B): List[B] = list.map(f)
  }

  given optionFunctor: Functor[Option] with {
    override def map[A, B](option: Option[A])(f: A => B): Option[B] = option.map(f)
  }

  given tryFunctor: Functor[Try] with {
    override def map[A, B](someTry: Try[A])(f: A => B): Try[B] = someTry.map(f)
  }

  // step 3: "user-facing" API
  def do10x[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
    functor.map(container)(_ * 10)

  // step 4: extension methods
  extension [F[_], A](container: F[A])(using functor: Functor[F]) def map[B](f: A => B) = functor.map(container)(f)

  // with the extension method in place, a better/shorter user-facing API is possible, with the context bound
  def do10x_v2[F[_]: Functor](container: F[Int]): F[Int] =
    container.map(_ * 10) // map is an extension method

  /*
    Exercise:
    - implement a new type class on the same structure as Functor
    - it should simplify three methods below
    - in the general API, must use for-comprehensions
   */

  def combineList[A, B](listA: List[A], listB: List[B]): List[(A, B)] =
    for {
      a <- listA
      b <- listB
    } yield (a, b)

  def combineList_v2[A, B](listA: List[A], listB: List[B]): List[(A, B)] =
    listA.flatMap { a =>
      listB.map { b =>
        (a, b)
      }
    }

  def combineOption[A, B](optionA: Option[A], optionB: Option[B]): Option[(A, B)] =
    for {
      a <- optionA
      b <- optionB
    } yield (a, b)

  def combineTry[A, B](tryA: Try[A], tryB: Try[B]): Try[(A, B)] =
    for {
      a <- tryA
      b <- tryB
    } yield (a, b)

  // 1 - type class definition - in addition to map we need flatMap for for-comprehension
  trait Monad[F[_]] extends Functor[F] {
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  // 2 - define given instances of type class
  given monadList: Monad[List] with {
    override def map[A, B](list: List[A])(f: A => B): List[B] = list.map(f)

    override def flatMap[A, B](list: List[A])(f: A => List[B]): List[B] = list.flatMap(f)
  }

  // 3 - user-facing API without for-comprehension

  def combine[F[_], A, B](containerA: F[A], containerB: F[B])(using magic: Monad[F]): F[(A, B)] =
    magic.flatMap(containerA) { a =>
      magic.map(containerB) { b =>
        (a, b)
      }
    }

  // 4 - extension method for the container
  extension [F[_], A](container: F[A])(using magic: Monad[F]) {
    // no need to define map, because it's already defined in another extension method
    def flatMap[B](f: A => F[B]): F[B] = magic.flatMap(container)(f)
  }

  def combine_v2[F[_]: Monad, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] = {
    for {
      a <- containerA
      b <- containerB
    } yield (a, b)
  }

  def main(args: Array[String]): Unit = {
    println(do10x(List(1, 2, 3)))
    println(combineList(List(1, 2, 3), List("a", "b", "c")))
    println(combineList_v2(List(1, 2, 3), List("a", "b", "c")))

    println(combine(List(1, 2, 3), List("a", "b", "c")))
    println(combine_v2(List(1, 2, 3), List("a", "b", "c")))
  }
}
