package com.rockthejvm.part5ts

import scala.annotation.nowarn

object Variance {

  class Animal
  @nowarn
  class Dog(name: String) extends Animal

  // Variance question for List: if Dog extends Animal, then should a List[Dog] "extend" List[Animal]?
  // for List, YES - List is COVARIANT

  val lassie = new Dog("Lassie")
  val hachi = new Dog("Hachi")
  val laika = new Dog("Laika")

  val anAnimal: Animal = lassie // ok, because Dog <: Animal
  val myDogs: List[Animal] = List(lassie, hachi, laika) // ok - List is COVARIANT: a list of dogs is a list of animals

  // how to define covariant types
  class MyList[+A] // MyList is COVARIANT in A
  val aListOfAnimals: MyList[Animal] = new MyList[Dog]

  // if NO, then the type is INVARIANT
  trait Semigroup[A] { // no marker means INVARIANT
    def combine(x: A, y: A): A
  }

  // Java generics - everything generic in Java is invariant
//  val aJavaList: java.util.ArrayList[Animal] = new java.util.ArrayList[Dog] // type mismatch: Java generics are all INVARIANT

  // Hell NO - CONTRAVARIANCE
  // if Dog <: Animal, then Vet[Animal] <: Vet[Dog]
  trait Vet[-A] { // contravariant in A
    def heal(animal: A): Boolean
  }

  val myVet: Vet[Dog] =
    new Vet[Animal] { // Vet[Animal] is actually better than required because it can treat any animal
      override def heal(animal: Animal): Boolean = {
        println("Hey there, you're all good...")
        true
      }
    }
  // if the vet can treat any animal, she/he can treat a dog too
  val healLaika = myVet.heal(laika)

  // During assignment:
  // - left-hand side is the minimum requirement
  // - right-hand side is something better, that satisfies more requirements (or at least required minimum), has more functionality

  /*
    How to decide on variance of a generic type?
    Rule of thumb:
    - if the type PRODUCES, STORES or RETRIEVES a value (e.g. a list), then it should be COVARIANT
    - if the type ACTS ON or CONSUMES a value (e.g. a vet), then it should be CONTRAVARIANT
    - otherwise, INVARIANT
   */

  // Exercises
  // 1 - which types should be invariant, covariant, contravariant
  class RandomGenerator[+A] // produces values, should be Covariant
  class MyOption[+A] // similar to Option[A], it's covariant because it's similar to list
  class JSONSerializer[-A] // consumes values and turns them into strings: contravariant
  trait MyFunction[-A, +B] // similar to Function1[A, B]

  // 2 - Add variance modifiers to this "library"
  abstract class LList[+A] {
    def head: A
    def tail: LList[A]
  }

  // this is the original EmptyList
//  case class EmptyList[+A]() extends LList[A] {
//    override def head: A = throw new NoSuchElementException()
//
//    override def tail: LList[A] = throw new NoSuchElementException()
//  }

  // since all empty lists are the same, it can be an object
  case object EmptyList extends LList[Nothing] {
    override def head: Nothing = throw new NoSuchElementException()

    override def tail: LList[Nothing] = throw new NoSuchElementException()
  }

  case class Cons[+A](override val head: A, override val tail: LList[A]) extends LList[A]

  val aList: LList[Int] = EmptyList
  val anotherList: LList[String] = EmptyList
  // Nothing <: A, therefore LList[Nothing] <: LList[A]

  def main(args: Array[String]): Unit = {}
}
