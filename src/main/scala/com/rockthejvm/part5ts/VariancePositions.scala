package com.rockthejvm.part5ts

import scala.collection.mutable

object VariancePositions {

  class Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal

  // 1 - type bounds
  class Cage[A <: Animal] // A must be a subtype of Animal
//  val aCage = new Cage[String] // not ok, String is not a subtype of Animal
  val aRealCage = new Cage[Dog] // ok, Dog <: Animal

  class WeirdContainer[A >: Animal] // A must be a supertype of Animal

  // 2 - variance positions

  // types of val fields are in COVARIANT position
//  class Vet[-T](val favoriteAnimal: T) // this is illegal
  // contravariant types cannot have value fields typed with T
  // on the other hand, covariant types usually have a value of type T

  /*
    Why is the above thing illegal?
    Let's assume, that above class compiled
    val garfield = new Cat
    val theVet: Vet[Animal] = new Vet[Animal](garfield)
    val aDogVet: Vet[Dog] = theVet // possible, because theVet is Vet[Animal] (has more functionality)
    val aDog: Dog = aDogVet.favoriteAnimal // compiler must guarantee this to be a dog, but in fact it's a cat - type conflict!
    The compiler has to guarantee the whole above code which is impossible. Therefore the Vet as defined above is forbidden
   */

  // types of var fields are also in COVARIANT position, same reason

  // types of var fields are in CONTRAVARIANT position - contrast to the above
  // Var fields are only applicable to INVARIANT types
//  class MutableOption[+T](var contents: T) // illegal

  /*
    Why is the above thing illegal?
    Let's assume it compiled
    val maybeAnimal: MutableOption[Animal] = new MutableOption[Dog](new Dog)
    maybeAnimal.contents = new Cat // type conflict, compiler cannot guarantee the correct usage of the var field
   */

  // types of method arguments are in CONTRAVARIANT position

  // this is illegal because the type is covariant but method arguments are in contravariant position
//  class MyList[+T] {
//    def add(element: T): MyList[T] = ???
//  }

  /*
    Why is the above illegal?
    Let's assume it compiled
    val animals: MyList[Animal] = new MyList[Cat]
    val biggerListOfAnimals = animals.add(new Dog) // type conflict
   */

  class Vet[-T] {
    def heal(animal: T): Boolean = true // this works fine
  }

  // method return types are in COVARIANT position
//  abstract class Vet2[-T] {
//    def rescueAnimal(): T
//  }

  /*
    Why is the above class illegal?
    Let's assume it compiled
    val vet: Vet2[Animal] = new Vet2[Animal] {
      override def rescueAnimal(): Animal = new Cat
    }
    val lassieVet: Vet2[Dog] = vet // Vet2[Animal]
    val rescueDog: Dog = lassiesVet.rescueAnimal() // must return a Dog but it actually returns a Cat - type conflict
   */

  // Solving variance position problems

  abstract class LList[+A] {
    def head: A
    def tail: LList[A]

    // widen the type of the list, compiler will infer the needed type to the common ancestor
    def add[B >: A](element: B): LList[B]
  }

  // val animals: List[Cat] = list of cats
  // van newAnimals: List[Animal] = animals.add(new Dog) // this will work because both dogs and cats are animals
  // in fact, I can add anything to the list, because there will always be a common ancestor

  class Vehicle
  class Car extends Vehicle
  class Supercar extends Car

  class RepairShop[-A <: Vehicle] {

    // narrowing the return type, compiler will return the most specific type
    def repair[B <: A](vehicle: B): B = vehicle
  }

  val myRepairShop: RepairShop[Car] = new RepairShop[Vehicle]
  val myBeatupVW = new Car
  val freshCar = myRepairShop.repair(myBeatupVW) // works, returns a car
  val damagedFerrari = new Supercar
  val freshFerrari = myRepairShop.repair(damagedFerrari) // works, returns a Supercar

  def main(args: Array[String]): Unit = {
    val newList: Seq[Dog] = Seq(new Dog)
    val appendedCat: Seq[Animal] = newList.appended(new Cat)

  }
}
