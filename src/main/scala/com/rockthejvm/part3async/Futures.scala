package com.rockthejvm.part3async

import java.util.concurrent.{ExecutorService, Executors}
import scala.annotation.nowarn
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Random, Success, Try}

object Futures {

  // Future is a computation that will be completed at some point in the future (probably on another thread)

  def calculateMeaningOfLife(): Int = {
    // simulate long compute
    Thread.sleep(1000)
    42
  }

  // thread pool (Java-specific)
  private val executor: ExecutorService = Executors.newFixedThreadPool(4)
  // thread pool (Scala-specific)
  given executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)

  // a future = an async computation that will finish at some point
  val aFuture: Future[Int] = Future.apply(calculateMeaningOfLife()) // given executionContext will be passed here

  // Option[Try[Int]], because
  // - we don't know if we have a value
  // - if we do, that can be a failed computation
  val futureInstantResult: Option[Try[Int]] = aFuture.value

  // callbacks
  aFuture.onComplete {
    case Success(value)     => println(s"I've completed with the meaning of life: $value")
    case Failure(exception) => println(s"My async computation failed: $exception")
  } // evaluated on SOME other thread

  /*
    Functional composition
   */

  case class Profile(id: String, name: String) {
    def sendMessage(anotherProfile: Profile, message: String): Unit =
      println(s"${this.name} sending message to ${anotherProfile.name}: $message")
  }

  object SocialNetwork {
    // "database"
    val names = Map(
      "rtjvm.id.1-daniel" -> "Daniel",
      "rtjvm.id.2-jane" -> "Jane",
      "rtjvm.id.3-mark" -> "Mark"
    )

    val friends = Map(
      "rtjvm.id.2.jane" -> "rtjvm.id.3-mark"
    )

    val random = new Random()

    // "API"
    def fetchProfile(id: String): Future[Profile] = Future {
      // fetch something from the database
      Thread.sleep(random.nextInt(300)) // simulate time delay
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bestFriendId = friends(profile.id)
      Profile(bestFriendId, names(bestFriendId))
    }
  }

  // problem: sending a message to my best friend
  def sendMessageToBestFriend(accountId: String, message: String): Unit = {
    // call fetchProfile
    // call fetchBestFriend
    // call profile.sendMessage(bestFriend)
    val profileFuture = SocialNetwork.fetchProfile(accountId)
    profileFuture.onComplete {
      case Success(profile) =>
        val friendProfileFuture = SocialNetwork.fetchBestFriend(profile)
        friendProfileFuture.onComplete {
          case Success(friendProfile) => profile.sendMessage(friendProfile, message)
          case Failure(e)             => e.printStackTrace()
        }
      case Failure(ex) => ex.printStackTrace()
    }
  }

  // onComplete is a hassle
  // solution: functional composition

  val janeProfileFuture = SocialNetwork.fetchProfile("rtjvm.id.2-jane")
  val janeFuture: Future[String] =
    janeProfileFuture.map(profile => profile.name) // map transforms the value contained inside, asynchronously
  val janesBestFriend: Future[Profile] = janeProfileFuture.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
  val janesBestFriendFilter: Future[Profile] = janesBestFriend.filter(profile => profile.name.startsWith("Z"))

  @nowarn
  def sendMessageToBestFriend_v2(accountId: String, message: String): Unit = {
    val profileFuture = SocialNetwork.fetchProfile(accountId)
    val action = profileFuture.flatMap { profile => // Future[Unit]
      SocialNetwork.fetchBestFriend(profile).map { bestFriend => // Future[Unit]
        profile.sendMessage(bestFriend, message) // unit
      }
    }
  }

  def sendMessageToBestFriend_v3(accountId: String, message: String): Unit = {
    for {
      profile <- SocialNetwork.fetchProfile(accountId)
      bestFriend <- SocialNetwork.fetchBestFriend(profile)
    } yield profile.sendMessage(bestFriend, message) // identical to v2
  }

  // fallbacks
  val profileNoMatterWhat: Future[Profile] = SocialNetwork.fetchProfile("unknown id").recover { case e: Throwable =>
    Profile("rtjvm.id.0-dummy", "Forever alone")
  }

  val aFetchedProfileNoMatterWhat: Future[Profile] = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("rtjvm.id.0-dummy")
  }

  val fallBackProfile: Future[Profile] =
    SocialNetwork.fetchProfile("unknown id").fallbackTo(SocialNetwork.fetchProfile("rtjvm.id.0-dummy"))

  // Difference between recoverWith and fallbackTo if both futures fail
  // recoverWith - second exception returned
  // fallbackTo - first exception returned

  /*
    Block for a future
   */
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    // "APIs"
    def fetchuser(name: String): Future[User] = Future {
      // simulate DB fetching
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      // simulate payment
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "SUCCESS")
    }

    // "external API" - synchronous
    def purchase(username: String, item: String, merchantName: String, price: Double): String = {
      /*
        1. fetch user
        2. create transaction
        3. WAIT for the transaction to finish
       */
      val transactionStatusFuture: Future[String] = for {
        user <- fetchuser(username)
        transaction <- createTransaction(user, merchantName, price)
      } yield transaction.status

      // blocking call
      Await.result(transactionStatusFuture, 2.seconds) // throws TimeoutException if the future doesn't finish in 2s
    }
  }

  /*
    Promises - data structure that allows manual control of completion of a future
      - completable/controllable wrapper of a future
      - useful for passing around to different threads so that they can be completed manually on a different thread
      - exposes the inner future
    The usual pattern is
      Thread 1 - creates an empty promise, knows how to handle the result
      Thread 2 - has a reference to the promise, fulfills or fails the promise
        - will trigger the completion on Thread 1
      Allows communication between the futures in a purely functional way
   */
  def demoPromises(): Unit = {
    val promise = Promise[Int]()
    val futureInside: Future[Int] = promise.future

    // thread 1 - "consumer": monitor the future for completion
    futureInside.onComplete {
      case Success(value) => println(s"[consumer]I've just been completed with $value")
      case Failure(ex)    => ex.printStackTrace()
    }

    // thread 2 = "producer"
    val producerThread = new Thread(() => {
      println("[producer]Crunching numbers...")
      Thread.sleep(1000)
      // "fulfill" the promise
      promise.success(42)
      println("[producer]I'm done")
    })

    producerThread.start()
  }

  /*
    Exercises
    1. Fulfill a future IMMEDIATELY with a value
    2. Futures in sequence: make sure the first future has been completed before returning the second
    3. create a method first(fa, fb) => return a new Future with the value of the first (fastest) Future to complete
    4. last(fa, fb) => return a new Future with the value of the LAST (slowest) Future to complete
    5. Retry an action returning a Future until a predicate holds true
   */

  // 1
  def completeImmediately[A](value: A): Future[A] = Future(value) // not really immediate, it's async
  def completeImmediately_v2[A](value: A): Future[A] = Future.successful(value) // synchronous completion
  val completedFuture: Future[String] = completeImmediately("Value")

  // 2
  def inSequence[A, B](first: Future[A], second: Future[B]): Future[B] = first.flatMap(_ => second)

  // 3
  def first[A](f1: Future[A], f2: Future[A]): Future[A] = Future.firstCompletedOf(Seq(f1, f2))

  // Promise EXTENDS future, so promise.future returns itself with a Future type instead of Promise
  def first_v2[A](f1: Future[A], f2: Future[A]): Future[A] = {
    val promise = Promise[A]()
    f1.onComplete(promise.tryComplete) // tryComplete is better than complete to avoid throwing an exception
    f2.onComplete(promise.tryComplete)

    promise.future // this returns actual promise itself but as a type of Future
  }

  // 4
  def last[A](f1: Future[A], f2: Future[A]): Future[A] = { // for some reason this doesn't really work
    val firstCompleted = Future.firstCompletedOf(Seq(f1, f2))
    if firstCompleted == f1 then f2 else f1
  }

  def last_v2[A](f1: Future[A], f2: Future[A]): Future[A] = {
    val bothPromise = Promise[A]()
    val lastPromise = Promise[A]()

    def checkAndComplete(result: Try[A]): Unit =
      if !bothPromise.tryComplete(result) then lastPromise.complete(result)

    f1.onComplete(checkAndComplete)
    f2.onComplete(checkAndComplete)

    lastPromise.future
  }

  // 5
  def retryUntil[A](action: () => Future[A], predicate: A => Boolean): Future[A] = {
    val future = action()
    future.filter(predicate).recoverWith { case _ =>
      retryUntil(action, predicate)
    }
  }
  // first I run the action that returns the future
  // if the predicate passes on the value of the future, return the future
  // repeat as many times as the predicate is false

  def testRetries(): Unit = {
    val random = new Random()
    val action: () => Future[Int] = () =>
      Future {
        Thread.sleep(100)
        val nextValue = random.nextInt(100)
        println(s"Generated $nextValue")
        nextValue
      }

    val predicate = (x: Int) => x < 10
    retryUntil(action, predicate).foreach(finalResult => println(s"Settled at $finalResult"))
  }

  def main(args: Array[String]): Unit = {
//    println(aFuture.value) // inspect the value of the future RIGHT NOW
//    Thread.sleep(2000)
//    executor.shutdown()

//    sendMessageToBestFriend_v3("rtjvm.id.2-jane", "Hey best friend, nice to talk to you again!")
//
//    println("purchasing...")
//    println(BankingApp.purchase("daniel-234", "shoes", "merchant-987", 3.56))
//    println("purchase complete")

//    demoPromises()

    lazy val fast = Future {
      Thread.sleep(100)
      1
    }

    lazy val slow = Future {
      Thread.sleep(2000)
      2
    }

    first(fast, slow).foreach(result => println(s"FIRST: $result"))
    last(fast, slow).foreach(result => println(s"LAST: $result"))

    testRetries()

    Thread.sleep(2000)
//    executor.shutdown()
  }

}
