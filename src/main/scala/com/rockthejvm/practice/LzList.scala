package com.rockthejvm.practice

import scala.annotation.{tailrec, targetName}

// Write lazily evaluated, potentially INFINITE linked list
abstract class LzList[A] {
  def isEmpty: Boolean
  def head: A
  def tail: LzList[A]

  // utilities
  @targetName("prepend")
  def #::(element: A): LzList[A] // prepending
  @targetName("concatenate")
  def ++(another: LzList[A]): LzList[A] // TODO warning

  // classics
  def foreach(f: A => Unit): Unit
  def map[B](f: A => B): LzList[B]
  def flatMap[B](f: A => LzList[B]): LzList[B]
  def filter(predicate: A => Boolean): LzList[A]
  def withFilter(predicate: A => Boolean): LzList[A] = filter(predicate)

  def take(n: Int): LzList[A] // takes first n elements from this lazy list
  def takeAsList(n: Int): List[A] = take(n).toList
  def toList: List[A] = {
    @tailrec
    def toListAux(remaining: LzList[A], acc: List[A]): List[A] =
      if remaining.isEmpty then acc.reverse else toListAux(remaining.tail, remaining.head :: acc)

    toListAux(this, List())
  } // risky on potentially infinite collections
}

case class LzEmpty[A]() extends LzList[A] {
  override def isEmpty: Boolean = true

  override def head: A = throw new NoSuchElementException("Empty list")

  override def tail: LzList[A] = throw new NoSuchElementException("Empty list")

  @targetName("prepend")
  override def #::(element: A): LzList[A] = LzCons(element, this)

  @targetName("concatenate")
  override def ++(another: LzList[A]): LzList[A] = another

  override def foreach(f: A => Unit): Unit = ()

  override def map[B](f: A => B): LzList[B] = LzEmpty()

  override def flatMap[B](f: A => LzList[B]): LzList[B] = LzEmpty()

  override def filter(predicate: A => Boolean): LzList[A] = this

  override def take(n: Int): LzList[A] =
    if n == 0 then this
    else throw new RuntimeException(s"Cannot take $n elements from an empty lazy list.")

}

// has to be regular class, not case class because fields are passed by name to allow lazy evaluation
class LzCons[A](hd: => A, tl: => LzList[A]) extends LzList[A] {

  override def isEmpty: Boolean = false

  // hint: use call by need
  override lazy val head: A = hd
  override lazy val tail: LzList[A] = tl // ideally we refer to this as little as possible

  @targetName("prepend")
  override def #::(element: A): LzList[A] = LzCons(element, this)

  @targetName("concatenate")
  override def ++(another: LzList[A]): LzList[A] = // TODO warning
    if tail.isEmpty then LzCons(head, another)
    else if another.isEmpty then this
    else LzCons(head, tail ++ another)

  override def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }

  override def map[B](f: A => B): LzList[B] = LzCons(f(head), tail.map(f))

  override def flatMap[B](f: A => LzList[B]): LzList[B] = f(head) ++ tail.flatMap(f)

  override def filter(predicate: A => Boolean): LzList[A] =
    if predicate(head) then LzCons(head, tail.filter(predicate)) else tail.filter(predicate)
  // TODO warning (else part would cause problems if no elements adhere to the predicate)

  override def take(n: Int): LzList[A] =
    if n <= 0 then LzEmpty()
    else if n == 1 then LzCons(head, LzEmpty())
    else LzCons(hd, tail.take(n - 1))
    // preserves lazy evaluation

}

object LzList {
  def empty[A]: LzList[A] = LzEmpty() // so that compiler doesn't complain in the from method

  def generate[A](start: A)(generator: A => A): LzList[A] =
    LzCons(start, LzList.generate(generator(start))(generator))

  // foldLeft seems wrong
  def from[A](list: List[A]): LzList[A] = list.foldLeft(LzList.empty[A]) { (currentLzList, newElement) =>
    LzCons(newElement, currentLzList)
  }
}

object LzListPlayground {
  def main(args: Array[String]): Unit = {
    val naturals = LzList.generate(1)(n => n + 1) // INFINITE list of natural numbers
    println(naturals.take(100).toList)
    naturals.take(100).foreach(println)
    naturals.take(100).flatMap(x => LzCons(x.toDouble, LzCons(x.toDouble, LzEmpty()))).foreach(println)
    naturals.filter(_ % 2 == 0).take(100).foreach(println)
    (-2 #:: naturals).filter(_ % 2 == 0).take(10).takeAsList(5).foreach(println)
    (naturals ++ LzList.from(List(1, 2, 3))).take(5).foreach(println)
    LzList.from(List(1, 2, 3, 4, 5)).foreach(println)
  }
}
