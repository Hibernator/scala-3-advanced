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
  infix def ++(another: => LzList[A]): LzList[A] // parameter by name in order not to break lazy evaluation

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
  override infix def ++(another: => LzList[A]): LzList[A] = another

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
  override infix def ++(another: => LzList[A]): LzList[A] =
    LzCons(head, tail ++ another)

  override def foreach(f: A => Unit): Unit = {
    @tailrec
    def foreachTailrec(lzList: LzList[A]): Unit =
      if lzList.isEmpty then ()
      else {
        f(lzList.head)
        foreachTailrec(lzList.tail)
      }

    foreachTailrec(this)
  }

  override def map[B](f: A => B): LzList[B] = LzCons(f(head), tail.map(f))

  // breaks lazy evaluation because the whole tail is evaluated
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
  def from[A](list: List[A]): LzList[A] = list.reverse.foldLeft(LzList.empty[A]) { (currentLzList, newElement) =>
    LzCons(newElement, currentLzList)
  }

  def apply[A](values: A*): LzList[A] = LzList.from(values.toList)

  def fibonacci: LzList[BigInt] = {
    def fibo(first: BigInt, second: BigInt): LzList[BigInt] =
      LzCons(first, fibo(second, first + second))

    fibo(1, 2)
  }

  def naturals: LzList[Int] = generate(1)(n => n + 1)

  private def isPrime(number: Int): Boolean = (2 until number).forall(number % _ > 0)
  def primeNumbersSimple: LzList[Int] = naturals.filter(isPrime)

  def primeNumbersEratosthenes: LzList[Int] = {
    def fromStartingNumber(number: Int, list: LzList[Int]): LzList[Int] = {
      val nextWithExcluded = list.filter(_ % number != 0)
      LzCons(number, fromStartingNumber(nextWithExcluded.head, nextWithExcluded.tail))
    }

    fromStartingNumber(2, generate(2)(_ + 1))
  }

  def primeNubmersEratosthenesDaniel: LzList[Int] = {
    def isPrime(n: Int): Boolean = {
      @tailrec
      def isPrimeTailrec(potentialDivisor: Int): Boolean = {
        if (potentialDivisor < 2) true
        else if (n % potentialDivisor == 0) false
        else isPrimeTailrec(potentialDivisor - 1)
      }

      isPrimeTailrec(n / 2)
    }

    def sieve(numbers: LzList[Int]): LzList[Int] = {
      if (numbers.isEmpty) numbers
      else if (!isPrime(numbers.head)) sieve(numbers.tail)
      else LzCons[Int](numbers.head, sieve(numbers.tail.filter(_ % numbers.head != 0)))
    }

    sieve(LzList.generate(2)(_ + 1))
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

    println("=================")

    println(naturals.head) // 1
    println(naturals.tail.head) // 2
    println(naturals.tail.tail.head) // 3

    val first50k = naturals.take(50000)
//    first50k.foreach(println)
    val first50kList = first50k.toList // still works well
    println(first50kList.take(2))
//    println(first50kList)

    // classics
    println(naturals.map(_ * 2).takeAsList(100))
    println(naturals.flatMap(x => LzList(x, x + 1)).takeAsList(100))
    println(naturals.filter(_ < 10).takeAsList(9))
//    println(naturals.filter(_ < 10).takeAsList(10)) // crash with SO or infinite recursion

    val combinationsLazy = for {
      number <- LzList(1, 2, 3)
      string <- LzList("black", "white")
    } yield s"$number-$string"

    println(combinationsLazy.toList)

    /* Exercises:
         1. Lazy list of Fibonacci numbers
            1, 2, 3, 5, 8, 13, 21, 34 ...
         2. Infinite list of prime numbers
            - filter with isPrime
            - Eratosthenes sieve
            [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,... start with this list
            [2, 3, 5, 7, 9, 11, 13, 15, 17 ... filter out all multiplies of 2
            [2, 3, 5, 7, 11, 13, 17, 19, 23, 25, 29 ... filter out all multiples of 3
            [2, 3, 5, 7, 11, 13, 17, 19, 23, 29 ...
     */

    val fibos = LzList.fibonacci
    println(fibos.takeAsList(100))
    println(LzList.primeNumbersSimple.takeAsList(100))
    println(LzList.primeNumbersEratosthenes.takeAsList(100))
    println(LzList.primeNubmersEratosthenesDaniel.takeAsList(100))
  }
}
