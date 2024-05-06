package com.rockthejvm.part1as

import scala.annotation.targetName
import scala.util.Try

object DarkSugars {

  // 1 - sugar for methods with one argument. Single-argument curly brace pattern
  def singleArgMethod(arg: Int): Int = arg + 1

  val aMethodCall = singleArgMethod({
    // long code
    42
  })

  val aMethodCall_v2 = singleArgMethod {
    // long code
    42
  }

  // example: Try, Future
  val aTryInstance = Try {
    throw new RuntimeException()
  }

  // with hofs
  val anIncrementedList = List(1, 2, 3).map { x =>
    // code block
    x + 1
  }

  // 2 - single abstract method pattern (since Scala 2.12)
  trait Action {
    def act(x: Int): Int
  }

  val anAction = new Action {
    override def act(x: Int): Int = x + 1
  }

  val anotherAction: Action = (x: Int) => x + 1 // new Action { def act(x: Int) = x + 1

  // example: Runnable
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("Hi, Scala, from another thread")
  })

  val aSweeterThread = new Thread(() => println("Hi, Scala"))

  // 3 - methods ending in a : are RIGHT-ASSOCIATIVE
  private val aList = List(1, 2, 3)
  val aPrependedList = 0 :: aList // aList.::(0)
  val aBigList = 0 :: 1 :: 2 :: List(3, 4) // List(3, 4).::(2).::(1).::(0)

  class MyStream[T] {
    @targetName("arrowDef")
    infix def -->:(value: T): MyStream[T] = this
  }

  val myStream: MyStream[Int] = 1 -->: 2 -->: 3 -->: 4 -->: MyStream[Int]

  // 4 - multi-word identifiers
  class Talker(name: String) {
    infix def `and then said`(gossip: String): Unit = println(s"$name said $gossip")
  }

  val daniel: Talker = Talker("Daniel")
  val danielsStatement: Unit = daniel `and then said` "I love Scala"

  // example: HTTP libraries
  object `Content-Type` {
    val `application/json` = "application/JSON"
  }

  // 5 - infix types

  // for more readable bytecode + Java interop
  @targetName("Arrow") // uses this in the bytecode and I can refer to it from other libraries or JVM languages
  infix class -->[A, B]
  val compositeType: Int --> String = new -->[Int, String] // useful for algebraic types (Cats)

  // 6 - update(), applicable to mutable containers
  val anArray = Array(1, 2, 3, 4)
  anArray.update(2, 45)
  anArray(2) = 45 // same as above, anArray[2] = 45 in Java

  // 7 - mutable fields
  class Mutable {
    private var internalMember: Int = 0
    def member: Int = internalMember // "getter"
    def member_=(value: Int): Unit = internalMember = value // "setter"
  }

  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // aMutableContainer.member_=(42)

  // 8 - variable arguments (varargs)
  def methodWithVarargs(args: Int*) = {
    // return the number of arguments supplied
    // the arguments can be treated as a Seq, have same API
    args.length
  }

  val callWithZeroArgs = methodWithVarargs()
  val callWithOneArgs = methodWithVarargs(78)
  val callWithTwoArgs = methodWithVarargs(12, 34)

  val aCollection = List(1, 2, 3)
  val callWithDynamicArgs = methodWithVarargs(aCollection*) // unwraps the content of the list and passes as varargs

  def main(args: Array[String]): Unit = {}
}
