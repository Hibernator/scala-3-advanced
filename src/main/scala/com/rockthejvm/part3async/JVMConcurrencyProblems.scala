package com.rockthejvm.part3async

object JVMConcurrencyProblems {

  def runInParallel(): Unit = {
    var x = 0

    val thread1 = new Thread(() => {
      x = 1
    })

    val thread2 = new Thread(() => {
      x = 2
    })

    thread1.start()
    thread2.start()

    Thread.sleep(100)
    println(x) // race condition, the problem is that we're using mutable variables
  }

  // it is possible to control threads order of execution via synchronization

  case class BankAccount(var amount: Int)

  def buy(bankAccount: BankAccount, thing: String, price: Int): Unit = {
    /*
      involves 3 steps
        - read old value
        - compute result
        - write new value
     */
    bankAccount.amount -= price // this is not atomic
  }

  def buySafe(bankAccount: BankAccount, thing: String, price: Int): Unit = {
    bankAccount.synchronized { // does not allow multiple threads to run the critical section AT THE SAME TIME
      bankAccount.amount -= price // critical section
    }
  }

  /*
    Example race condition:
    thread1 (shoes)
      - reads amount 50000
      - compute result 50000 - 3000 = 47000
    thread2 (iPhone)
      - reads amount 50000
      - compute result 50000 - 4000 = 46000
    thread1 (shoes)
      - write amount 47000
    thread2 (iPhone)
      - write amount 46000
   */
  def demoBankingProblem(): Unit = {
    (1 to 100_000).foreach { _ =>
      val account = BankAccount(50_000)
      val thread1 = new Thread(() => buy(account, "shoes", 3000))
      val thread2 = new Thread(() => buy(account, "iPhone", 4000))
      thread1.start()
      thread2.start()
      thread1.join()
      thread2.join()
      if (account.amount != 43_000) println(s"AHA! I've just broken the bank: ${account.amount}")
    }
  }

  /*
    Exercises

    1 - create "inception threads"
      thread1 creates
        -> thread2 creates
          -> thread3
      each thread prints "hello from thread $i"
      Print all of them IN REVERSE ORDER

    2 - what's the max/min value of x?

    3 - "sleep fallacy": what's the value of message?
   */

  // 1
  def createThread(n: Int, max: Int): Thread = {
    new Thread(() => {
      if n < max then {
        val spawnedThread = createThread(n + 1, max)
        spawnedThread.start()
        spawnedThread.join()

      }
      println(s"hello from thread $n")
    })
  }

  // 2
  /*
    Max value = 100 - each thread increases x by 1
    Min value = 1
      all threads read x = 0 at the same time
      all threads (in parallel) compute 0 + 1 = 1
      all threads try to write x = 1
   */
  def minMaxX(): Unit = {
    var x = 0
    val threads = (1 to 100).map(_ => new Thread(() => x += 1))
    threads.foreach(_.start())
  }

  // 3
  /*
    Almost always, message = "Scala is awesome"
    It is not guaranteed
    Obnoxious situation (still possible)

    main thread:
      message = "Scala sucks"
      awesomeThread.start()
      sleep(1001) - yields execution
    awesomeThread:
      sleep(1000) - yields execution
    OS gives the CPU to some important thread, takes > 2s
    OS gives the CPU back to the main thread
    main thread:
      println(message) // Scala sucks
    awesome thread:
      message = "Scala is awesome" // too late by now
   */
  def demoSleepFallacy(): Unit = {
    var message = ""
    val awesomeThread = new Thread(() => {
      Thread.sleep(1000)
      message = "Scala is awesome"
    })

    message = "Scala sucks"
    awesomeThread.start()
    Thread.sleep(1001)
    // solution: join the worker thread
    // synchronization doesn't help here
    println(message)
  }

  def main(args: Array[String]): Unit = {
//    runInParallel()
//    demoBankingProblem()
    createThread(1, 50).start()
  }

}
