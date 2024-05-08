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
}

case class Empty[A]() extends FSet[A] {
  override def contains(elem: A): Boolean = false

  @targetName("addOne")
  override infix def +(elem: A): FSet[A] = Cons(elem, this)

  @targetName("union")
  override infix def ++(anotherSet: FSet[A]): FSet[A] = anotherSet

  override def map[B](f: A => B): FSet[B] = Empty()

  override def flatMap[B](f: A => FSet[B]): FSet[B] = Empty()

  override def filter(predicate: A => Boolean): FSet[A] = this

  override def foreach(f: A => Unit): Unit = ()
}

case class Cons[A](head: A, tail: FSet[A]) extends FSet[A] {

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
    println(first5.contains(5)) // true
    println(first5.contains(6)) // false
    println((first5 + 10).contains(10)) // true
    println(first5.map(_ * 2).contains(10)) // true
    println(first5.map(_ % 2).contains(1)) // true
    println(first5.flatMap(x => FSet(x, x + 1)).contains(7)) // false
  }
}
