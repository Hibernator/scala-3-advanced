package com.rockthejvm.part5ts

object SelfTypes {

  trait Instrumentalist {
    def play(): Unit
  }

  trait Singer {
    // name can be anything, usually called self
    self: Instrumentalist => // self-type: whoever implements Singer, MUST also implement Instrumentalist
    // it's not a lambda

    // rest of API
    def sing(): Unit
  }

  class LeadSinger extends Singer with Instrumentalist { // works, because the self-type requirement is satisfied
    override def sing(): Unit = ???

    override def play(): Unit = ???
  }

  // doesn't work because does not extend Instrumentalist
//  class Vocalist extends Singer {
//  }

  val jamesHetfield = new Singer with Instrumentalist { // anonymous type works
    override def sing(): Unit = ???

    override def play(): Unit = ???
  }

  class Guitarist extends Instrumentalist {
    override def play(): Unit = println("some guitar solo")
  }

  val ericClapton = new Guitarist with Singer: // works because Guitarist <: Instrumentalist
    override def sing(): Unit = println("layla")

  // self-types vs inheritance
  class A
  class B extends A // B "is an" A

  trait T
  trait S { self: T => } // S "requires a" T

  // self-types for dependency-injection = "cake pattern", compile-time DI
  // can get restrictive and complicated in larger code bases

  abstract class Component {
    // main general API
  }

  // two alternatives for Component
  class ComponentA extends Component
  class ComponentB extends Component

  // regular dependency injection, passing the right implementation at runtime
  class DependentComponent(val component: Component)

  // cake pattern - abstract classes and traits are defined in layers (like a cake)
  // Each layer depends on the previous layer
  trait ComponentLayer1 {
    // API
    def actionLayer1(x: Int): String
  }

  trait ComponentLayer2 {
    self: ComponentLayer1 =>

    // some other API
    def actionlayer2(x: String): Int
  }

  trait Application {
    self: ComponentLayer1 & ComponentLayer2 =>

    // my main API
  }

  // example: a photo taking application API in the style of Instagram
  // layer 1 - small components
  trait Picture extends ComponentLayer1
  trait Stats extends ComponentLayer1

  // layer 2 - compose components from layer 1
  trait ProfilePage extends ComponentLayer2 with Picture
  trait Analytics extends ComponentLayer2 with Stats

  // layer 3 - application
  trait AnalyticsApp extends Application with Analytics
  // dependencies are specified in layers, like baking a cake
  // when you put the pieces together, you can pick a possible implementation from each layer

  // self-types: preserve the "this" instance
  class SingerWithInnerClass {
    self => // self-type with no type requirement, self == this

    class Voice {
      def sing() = this.toString // this == the voice, self needs to be used to refer to the outer instance
    }
  }

  // cyclical inheritance

  // with normal inheritance doesn't work
//  class X extends Y
//  class Y extends X

  // cyclical dependencies
  // means that any implementation of either X or Y must implement the other one as well
  trait X { self: Y => }
  trait Y { self: X => }
  trait Z extends X with Y

  def main(args: Array[String]): Unit = {}
}
