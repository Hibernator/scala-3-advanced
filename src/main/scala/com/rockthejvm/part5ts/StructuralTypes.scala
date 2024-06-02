package com.rockthejvm.part5ts

import reflect.Selectable.reflectiveSelectable

object StructuralTypes {

  type SoundMaker = { // structural type, defined by its content, not name
    def makeSound(): Unit
  }

  class Dog {
    def makeSound(): Unit = println("bark!")
  }

  class Car {
    def makeSound(): Unit = println("vroom!")
  }

  val dog: SoundMaker = new Dog // works
  val car: SoundMaker = new Car

  /*
    duck typing
    - relying on type test called duck test
      if something walks like a duck, quacks like a duck, looks like a duck then it's probably a duck
    - normally only available in dynamic languages like Python or Javascript
    - here it's compile-time duck typing (but invoking methods uses reflection - slow)
   */

  // type refinements
  abstract class Animal {
    def eat(): String
  }

  type WalkingAnimal = Animal { // refined type - extending an existing type
    def walk(): Int
  }

  // why we need structural types: creating type-safe APIs for existing types following the same structure, but having no connection to each other

  type JavaCloseable = java.io.Closeable
  class CustomCloseable {
    def close(): Unit = println("ok ok, I'm closing")
    def closeSilently(): Unit = println("not making a sound, I promise")
  }
  // I want an API that handles both of the above

  // I could try using a union type
//  def closeResource(closeable: JavaCloseable | CustomCloseable): Unit =
//    closeable.close() // illegal, compiler can't figure out similarity between Java and Custom Closeable

  // solution: structural type
  type UnifiedCloseable = {
    def close(): Unit
  }

  def closeResource(closeable: UnifiedCloseable): Unit = closeable.close() // here I can pass both types

  val jCloseable = new JavaCloseable {
    override def close(): Unit = println("closing Java resource")
  }
  val customCloseable = new CustomCloseable

  // I can even define anonymous structural type
  def closeResource_v2(closeable: { def close(): Unit }): Unit = closeable.close()

  def main(args: Array[String]): Unit = {
    // invoking methods of structural types is done via reflection - inspecting methods and fields at runtime - very costly operation
    dog.makeSound()
    car.makeSound()

    closeResource(jCloseable)
    closeResource(customCloseable)
  }
}
