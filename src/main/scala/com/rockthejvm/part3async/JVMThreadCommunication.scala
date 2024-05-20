package com.rockthejvm.part3async

import scala.collection.mutable
import scala.util.Random

object JVMThreadCommunication {
  def main(args: Array[String]): Unit = {
//    ProdConsV1.start()
//    ProdConsV2.start()
    ProdConsV3.start(4)
  }
}

// example: the producer-consumer problem

class SimpleContainer {
  private var value: Int = 0

  def isEmpty: Boolean = value == 0
  def set(newValue: Int): Unit = value = newValue

  def get: Int = {
    val result = value
    value = 0
    result
  }
}

// PC part 1: one producer, one consumer
object ProdConsV1 {
  def start(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      // busy waiting
      // very poor implementation because it blocks the CPU
      while (container.isEmpty) {
        println("[consumer] waiting for a value...")
      }

      println(s"[consumer] I  have consumed a value: ${container.get}")
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 42
      println(s"[producer] I am producing, after LONG work, the value $value")
      container.set(value)
    })

    consumer.start()
    producer.start()
  }
}

// wait + notify
object ProdConsV2 {
  def start(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")

      container.synchronized { // block all other threads trying to "lock" this object
        // thread-safe code
        if (container.isEmpty)
          container.wait() // release the lock and suspend the thread
        // the thread now is blocked but without using the CPU cycles, it's dormant
        // some other thread will have to awaken the current thread by calling notify on the same instance (container)

        // reacquire the lock here
        // continue execution
        println(s"[consumer] I  have consumed a value: ${container.get}")
      }

    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 42

      container.synchronized {
        println(s"[producer] I am producing, after LONG work, the value $value")
        container.set(value)
        container.notify() // awaken ONE suspended thread on this object
        // in case there are multiple threads waiting for notification, one will be randomly selected
        // the selected thread will continue only after the current thread releases the lock
      } // release the lock
    })

    consumer.start()
    producer.start()
  }

}

// insert a larger container (queue)
// producer -> [ _ _ _ ] -> consumer
// producer will insert values into the queue as long as there is space available, producer will retrieve values
// producer will wait if the queue is full
// consumer will pause if the queue is empty
object ProdConsV3 {
  def start(containerCapacity: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]

    val consumer = new Thread(() => {
      val random = new Random(System.nanoTime())

      while (true) {
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] buffer empty, waiting...")
            buffer.wait()
          }

          // buffer is not empty
          val x = buffer.dequeue()
          println(s"[consumer] I've just consumer $x")

          // producer, give me more elements
          buffer.notify()
        }
        Thread.sleep(random.nextInt(500)) // sleep at most half a second
      }
    })

    val producer = new Thread(() => {
      val random = new Random(System.nanoTime())
      var counter = 0

      while (true) {
        buffer.synchronized {
          if (buffer.size == containerCapacity) {
            println("[producer] buffer full, waiting...")
            buffer.wait()
          }

          // buffer is not full
          val newElement = counter
          counter += 1
          println(s"[producer] I'm producing $newElement")
          buffer.enqueue(newElement)

          // consumer, don't be lazy
          buffer.notify() // wakes up the consumer if it's asleep. NOOP if there is no thread waiting for it
        }

        Thread.sleep(random.nextInt(500))
      }
    })

    consumer.start()
    producer.start()
  }
}
