package com.rockthejvm.part3async

object JVMThreadCommunication {
  def main(args: Array[String]): Unit = {
//    ProdConsV1.start()
    ProdConsV2.start()
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
