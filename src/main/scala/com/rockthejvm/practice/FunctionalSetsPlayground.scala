package com.rockthejvm.practice

import scala.annotation.tailrec
import scala.annotation.targetName

abstract class FSet[A] extends (A => Boolean) {
  // main api
  def contains(elem: A): Boolean
  def apply(elem: A): Boolean = contains(elem)

  @targetName("addOne")
  infix def +(elem: A): FSet[A]
  @targetName("union")
  infix def ++(anotherSet: FSet[A]): FSet[A]

  // "classics"
  def map[B](f: A => B): FSet[B]
  def flatMap[B](f: A => FSet[B]): FSet[B]
  def filter(predicate: A => Boolean): FSet[A]
  def foreach(f: A => Unit): Unit

  // utilities
  @targetName("minus")
  infix def -(elem: A): FSet[A]
  @targetName("diff")
  infix def --(anotherSet: FSet[A]): FSet[A]
  @targetName("intersection")
  infix def &(anotherSet: FSet[A]): FSet[A]

  // "negation" everything but the items in the set. All the elements of type A except the elements in this set
  @targetName("not")
  def unary_! : FSet[A] = new PBSet[A](x => !contains(x))
}

// would be good to have a property-based set
// example of a set described by a property of its elements
// {x in N | x % 2 == 0 } ... all even natural numbers

// Property-based set. It's elements are described purely by their property
// Most general set I can imagine
// The problem is, it's not iterable. Map, flatMap, foreach functions not available
class PBSet[A](property: A => Boolean) extends FSet[A] {
  override def contains(elem: A): Boolean = property(elem)

  @targetName("addOne")
  override infix def +(elem: A): FSet[A] = new PBSet[A](x => property(x) || x == elem)

  @targetName("union")
  override infix def ++(anotherSet: FSet[A]): FSet[A] = new PBSet[A](x => property(x) || anotherSet(x))

  override def map[B](f: A => B): FSet[B] = politelyFail()

  override def flatMap[B](f: A => FSet[B]): FSet[B] = politelyFail()

  override def filter(predicate: A => Boolean): FSet[A] = new PBSet[A](x => property(x) && predicate(x))

  override def foreach(f: A => Unit): Unit = politelyFail()

  @targetName("minus")
  override infix def -(elem: A): FSet[A] = filter(_ != elem)

  @targetName("diff")
  override infix def --(anotherSet: FSet[A]): FSet[A] = filter(!anotherSet)

  @targetName("intersection")
  override infix def &(anotherSet: FSet[A]): FSet[A] = filter(anotherSet)

  // extra utilities (internal)
  private def politelyFail() = throw new RuntimeException("I don't know if this set is iterable...")
}

case class Empty[A]() extends FSet[A] { // Can implement in terms of property-based set PBSet(x => false)
  override def contains(elem: A): Boolean = false

  @targetName("addOne")
  override infix def +(elem: A): FSet[A] = Cons(elem, this)

  @targetName("union")
  override infix def ++(anotherSet: FSet[A]): FSet[A] = anotherSet

  override def map[B](f: A => B): FSet[B] = Empty()

  override def flatMap[B](f: A => FSet[B]): FSet[B] = Empty()

  override def filter(predicate: A => Boolean): FSet[A] = this

  override def foreach(f: A => Unit): Unit = ()

  @targetName("minus")
  override infix def -(elem: A): FSet[A] = this

  @targetName("diff")
  override infix def --(anotherSet: FSet[A]): FSet[A] = this

  @targetName("intersection")
  override infix def &(anotherSet: FSet[A]): FSet[A] = this
}

case class Cons[A](head: A, tail: FSet[A]) extends FSet[A] { // Can also be implemented in terms of property-based set

  override def contains(elem: A): Boolean = head == elem || tail.contains(elem)

  @targetName("addOne")
  override infix def +(elem: A): FSet[A] = if contains(elem) then this else Cons(elem, this)

  @targetName("union")
  override infix def ++(anotherSet: FSet[A]): FSet[A] = tail ++ anotherSet + head

  override def map[B](f: A => B): FSet[B] = tail.map(f) + f(head)

  override def flatMap[B](f: A => FSet[B]): FSet[B] = tail.flatMap(f) ++ f(head)

  override def filter(predicate: A => Boolean): FSet[A] =
    if predicate(head) then Cons(head, tail.filter(predicate)) else tail.filter(predicate)

  override def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }

  @targetName("minus")
  override infix def -(elem: A): FSet[A] = if head == elem then tail else tail - elem + head

  @targetName("diff")
//  override infix def --(anotherSet: FSet[A]): FSet[A] = filter(x => !anotherSet(x)) // the first version, without negation
  override infix def --(anotherSet: FSet[A]): FSet[A] = filter(!anotherSet)

  @targetName("intersection")
  override infix def &(anotherSet: FSet[A]): FSet[A] = filter(anotherSet) // intersection == filtering
}

object FSet {
  def apply[A](values: A*): FSet[A] = {

    @tailrec
    def buildSet(valuesSeq: Seq[A], acc: FSet[A]): FSet[A] =
      if valuesSeq.isEmpty then acc else buildSet(valuesSeq.tail, acc + valuesSeq.head)

    buildSet(values, Empty())
  }
}

// All sets in Scala extend a function A => Boolean
object FunctionalSetsPlayground {

  def main(args: Array[String]): Unit = {
    val first5 = FSet(1, 2, 3, 4, 5)
    val someNumbers = FSet(4, 5, 6, 7, 8)
    println(first5.contains(5)) // true
    println(first5.contains(6)) // false
    println((first5 + 10).contains(10)) // true
    println(first5.map(_ * 2).contains(10)) // true
    println(first5.map(_ % 2).contains(1)) // true
    println(first5.flatMap(x => FSet(x, x + 1)).contains(7)) // false

    val aSet = Set(1, 2, 3)
    val aList = (1 to 10).toList
    println(aList.filter(aSet)) // I can use a standard library set as an argument to the filter function

    println((first5 - 3).contains(3)) // false
    println((first5 -- someNumbers).contains(4)) // false
    println((first5 & someNumbers).contains(4)) // true

    val naturals = new PBSet[Int](_ => true)
    println(naturals.contains(43546)) // true
    println(!naturals.contains(0)) // false
    println((!naturals + 1 + 2 + 3).contains(3)) // true
//    println(!naturals.map(_ + 1)) // throw
  }
}
