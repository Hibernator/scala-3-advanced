package com.rockthejvm.part3async

import java.util.concurrent.Executors

object JVMConcurrencyIntro {

  def basicThreads(): Unit = {
    val runnable = new Runnable:
      override def run(): Unit = {
        println("waiting")
        Thread.sleep(2000)
        println("running on some thread")
      }

    // threads on the JVM
    val aThread = new Thread(runnable)
    aThread.start() // will run the runnable on a JVM thread, non-blocking operation, the current thread will continue
    // JVM thread == OS thread. It's now changed via project Loom
    aThread.join() // block the current thread until aThread finishes
  }

  // order of operations among several threads is never guaranteed
  // different runs = different results
  def orderOfExecution(): Unit = {
    val threadHello = new Thread(() => (1 to 5).foreach(_ => println("hello")))
    val threadGoodbye = new Thread(() => (1 to 5).foreach(_ => println("goodbye")))
    threadHello.start()
    threadGoodbye.start()
  }

  // Threads are usually never started by hand
  // Instead thread pools / executors are used

  // Executors
  def demoExecutors(): Unit = {
    val threadPool = Executors.newFixedThreadPool(4) // 4 threads with internal scheduler
    // submit a computation, non-blocking on the main thread
    threadPool.execute(() => println("something in the thread pool"))

    threadPool.execute { () =>
      Thread.sleep(1000)
      println("done after one second")
    }

    threadPool.execute { () =>
      Thread.sleep(1000)
      println("almost done")
      Thread.sleep(1000)
      println("done after 2 seconds")
    }

    // thread pool needs to be shut down, otherwise the application never finishes
    threadPool.shutdown() // previously submitted tasks are executed but no new accepted
//    threadPool.execute(() => println("this should not appear")) // should throw and exception in calling thread
  }

  def main(args: Array[String]): Unit = {
//    runnable.run() // invoked in the main thread, nothing parallel
//    orderOfExecution()
    demoExecutors()
  }
}
