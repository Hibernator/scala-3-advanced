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

  def main(args: Array[String]): Unit = {
//    println(aFuture.value) // inspect the value of the future RIGHT NOW
//    Thread.sleep(2000)
//    executor.shutdown()

//    sendMessageToBestFriend_v3("rtjvm.id.2-jane", "Hey best friend, nice to talk to you again!")
//
//    println("purchasing...")
//    println(BankingApp.purchase("daniel-234", "shoes", "merchant-987", 3.56))
//    println("purchase complete")

    demoPromises()

    Thread.sleep(2000)
    executor.shutdown()
  }

}
