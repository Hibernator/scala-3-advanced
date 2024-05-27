package com.rockthejvm.part5ts

object AdvancedInheritance {

  // 1 - composite types can be used on their own

  // example - data streaming app
  trait Writer[T] { // writes data to a sink
    def write(value: T): Unit
  }

  trait Stream[T] {
    def foreach(f: T => Unit): Unit
  }

  trait Closeable {
    def close(status: Int): Unit
  }

  // class MyDataStream extends Writer[String] with Stream[String] with Closeable { ... } // composite class
  // this type (after extends) can be used STANDALONE

  def processStream[T](stream: Writer[T] & Stream[T] & Closeable): Unit = {
    stream.foreach(println)
    stream.close(0)
  }

  // 2 - diamond problem

  trait Animal {
    def name: String
  }

  trait Lion extends Animal {
    override def name: String = "Lion"
  }

  trait Tiger extends Animal {
    override def name: String = "Tiger"
  }

  class Liger extends Lion with Tiger

  def demoLiger(): Unit = {
    val liger = new Liger
    println(liger.name)
  }

  /*
    Pseudo-definition:
    class Liger extends Animal
    with { override def name: String = "Lion" }
    with { override def name: String = "Tiger" }

    The last override always gets picked.
   */

  // 3 - the super problem

  trait Cold { // cold colors
    def print() = println("cold")
  }

  trait Green extends Cold {
    override def print(): Unit = {
      println("green")
      super.print()
    }
  }

  trait Blue extends Cold {
    override def print(): Unit = {
      println("blue")
      super.print()
    }
  }

  class Red {
    def print() = println("red")
  }

  class White extends Red with Green with Blue {
    override def print(): Unit = {
      println("white")
      super.print()
    }
  }

  def demoColorInheritance(): Unit = {
    val white = new White
    white.print()
  }

  /*
    Expected result
    - white
    - red

    Actual result
    - white
    - blue
    - green
    - cold
    NO RED!!!
    WTF?

    White = Red with Green with Blue with body of White
    White = AnyRef with body of Red with (AnyRef with body of Cold with body of Green) with (AnyRef with body of Cold with body of Blue) with body of White
    White = AnyRef with body of Red with body of Cold with body of Green with body of Blue with body of White
    This is Type Linearization, compiler does it
    super keyword moves from left to right
   */

  def main(args: Array[String]): Unit = {
    demoLiger()
    demoColorInheritance()
  }
}
