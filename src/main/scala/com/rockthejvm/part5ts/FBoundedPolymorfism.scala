package com.rockthejvm.part5ts

object FBoundedPolymorfism {

  object Problem {
    trait Animal {
      def breed: List[Animal]
    }

    class Cat extends Animal {
      // right now, nothing prevents me to return dogs here
      override def breed: List[Animal] = List(new Cat, new Dog) // problem here!
    }

    class Dog extends Animal {
      override def breed: List[Animal] = List(new Dog, new Dog, new Dog)
    }

    // we lost type safety of the breed method
  }

  object NaiveSolution {
    trait Animal {
      def breed: List[Animal]
    }

    class Cat extends Animal {
      // right now, nothing prevents me to return dogs here
      override def breed: List[Cat] = List(new Cat, new Cat) // can't return Dog anymore
    }

    class Dog extends Animal {
      override def breed: List[Dog] = List(new Dog, new Dog, new Dog)
    }

    // I have to write the proper type signatures and I could make a mistake
    // Would be better if compiler could help with it
  }

  // f-bounded polymorfism and recursive types solution
  object FBP {
    trait Animal[A <: Animal[A]] { // recursive type, F-bounded polymorfism. Also possible in Java
      def breed: List[Animal[A]]
    }

    class Cat extends Animal[Cat] {
      // here compiler won't allow returning Dogs
      override def breed: List[Animal[Cat]] = List(new Cat, new Cat)
    }

    class Dog extends Animal[Dog] {
      override def breed: List[Animal[Dog]] = List(new Dog, new Dog, new Dog)
    }

    // it is possible to mess up FPB, it's not a bullet-proof solution
    class Crocodile extends Animal[Dog] {
      override def breed: List[Animal[Dog]] = ??? // list of dogs
    }
  }

  // example: ORM libraries
  trait Entity[E <: Entity[E]]

  // example: Java sorting library
  class Person extends Comparable[Person] { // FBP, recursive type
    override def compareTo(o: Person): Int = ???
  }

  // FBP + self types. Provides better guarantees, can't mess up as above
  object FBPSelf {
    trait Animal[A <: Animal[A]] { self: A =>
      def breed: List[Animal[A]]
    }

    class Cat extends Animal[Cat] { // Cat == Animal[Cat]
      // here compiler won't allow returning Dogs
      override def breed: List[Animal[Cat]] = List(new Cat, new Cat)
    }

    class Dog extends Animal[Dog] {
      override def breed: List[Animal[Dog]] = List(new Dog, new Dog, new Dog)
    }

//    class Crocodile extends Animal[Dog] { // illegal, because Crocodile must also extend Dog
//      override def breed: List[Animal[Dog]] = ???
//    }

    // I can go one level deeper to mess up
    trait Fish extends Animal[Fish]

    class Cod extends Fish {
      override def breed: List[Animal[Fish]] = List(new Cod, new Cod)
    }

    class Shark extends Fish {
      override def breed: List[Animal[Fish]] = List(new Cod) // legal but wrong
    }

    // solution level 2
    trait FishLevel2[A <: FishLevel2[A]] extends Animal[FishLevel2[A]] { self: A => }

    class Tuna extends FishLevel2[Tuna] {
      override def breed: List[Animal[FishLevel2[Tuna]]] = List(new Tuna)
    }

//    class Swordfish extends FishLevel2[Swordfish] {
//      override def breed: List[Animal[FishLevel2[Swordfish]]] = List(new Tuna) // illegal because of the self type
//    }

    // this solution can be repeated infinite levels down but code may be hard to read
  }

  def main(args: Array[String]): Unit = {}
}
