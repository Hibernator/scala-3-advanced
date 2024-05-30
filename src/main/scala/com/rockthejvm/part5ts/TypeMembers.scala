package com.rockthejvm.part5ts

object TypeMembers {

  class Animal
  class Dog extends Animal
  class Cat extends Animal

  class AnimalCollection {
    // val, var, def, class, trait, object
    type AnimalType // abstract type member, can be overridden in a subclass
    type BoundedAnimal <: Animal // abstract type member with a type bound
    type SuperBoundedAnimal >: Dog <: Animal
    type AnimalAlias = Cat // type alias, often used to alias complex/nested types
    type NestedOption = List[Option[Option[Int]]]
  }

  class MoreConcreteAnimalCollection extends AnimalCollection {
    override type AnimalType = Dog
  }

  // using type members
  val ac = new AnimalCollection
  val anAnimal: ac.AnimalType =
    ??? // every instance of AnimalCollection can have its own AnimalType and it belongs to that instance

  // Each type member must be exactly ONE concrete type (for the instance)
  // val cat: ac.BoundedAnimal = new Cat // illegal, because BoundedAnimal could be a Dog, compiler can't guarantee that it's a Cat
  val aDog: ac.SuperBoundedAnimal =
    new Dog // works, because Dog <: SuperBoundedAnimal, and there is nothing between Dog and Animal
  val aCat: ac.AnimalAlias = new Cat // ok, because Cat == AnimalAlias

  // Why are type members useful
  // To establish relationships between types
  // To be able to reuse the same piece of code on multiple unrelated types (same goal as generics)
  // Type members can be considered an alternative to generics

  class LList[T] {
    def add(element: T): LList[T] = ???
  }

  class MyList {
    type T
    def add(element: T): MyList = ???
  }
  // every instance of MyList will need to have a proper definition of the abstract type T (downside)
  // .type
  type CatType = aCat.type // every value can present it's type like this and have it attached to a type alias
  val newCat: CatType = aCat

  def main(args: Array[String]): Unit = {}
}
