package com.rockthejvm.practice

import java.util.Date

object JSONSerialization {

  /*
    Domain objects: Users, posts, feeds
    Serialize to JSON
   */

  case class User(name: String, age: Int, email: String)
  case class Post(content: String, createdAt: Date)
  case class Feed(user: User, posts: List[Post])

  /*
    1 - intermediate data: numbers, strings, lists, dates, objects
    2 - type class to convert data to intermediate data
    3 - serialize to JSON
   */

  sealed trait JSONValue {
    def stringify: String
  }

  final case class JSONString(value: String) extends JSONValue {
    override def stringify: String = "\"" + value + "\""
  }

  final case class JSONNumber(value: Int) extends JSONValue {
    override def stringify: String = value.toString
  }

  final case class JSONArray(values: List[JSONValue]) extends JSONValue {
    override def stringify: String = values.map(_.stringify).mkString("[", ",", "]") // ["string", 3. ... ]
  }

  final case class JSONObject(values: Map[String, JSONValue]) extends JSONValue {
    override def stringify: String = values
      .map((key, value) => "\"" + key + "\":" + value.stringify)
      .mkString("{", ",", "}")
  }

  /*
    {
      "name": "John",
      "age": 22,
      "friends" : [...],
      "latestPost": { ... }
    }
   */

  val data: JSONObject = JSONObject(
    Map(
      "user" -> JSONString("Daniel"),
      "posts" -> JSONArray(
        List(
          JSONString("Scala is awesome!"),
          JSONNumber(42)
        )
      )
    )
  )

  // part 2 - type class pattern
  // 1 - type class definition
  trait JSONConverter[T] {
    def convert(value: T): JSONValue
  }

  // 2 - type class instances for String, Int, Date, User, Post, Feed
  given stringConverter: JSONConverter[String] with {
    override def convert(value: String): JSONValue = JSONString(value)
  }

  given intConverter: JSONConverter[Int] with {
    override def convert(value: Int): JSONValue = JSONNumber(value)
  }

  given dateConverter: JSONConverter[Date] with {
    override def convert(value: Date): JSONValue = JSONString(value.toString)
  }

  given userConverter: JSONConverter[User] with {
    override def convert(user: User): JSONValue = JSONObject(
      Map(
        "name" -> JSONConverter[String].convert(user.name), // this is why JSONSyntax.apply method is needed
        "age" -> JSONConverter[Int].convert(user.age),
        "email" -> JSONConverter[String].convert(user.email)
      )
    )
  }

  given postConverter: JSONConverter[Post] with {
    override def convert(post: Post): JSONValue = JSONObject(
      Map(
        "content" -> JSONConverter[String].convert(post.content),
        "createdAt" -> JSONConverter[String].convert(post.createdAt.toString)
      )
    )
  }

  given feedConverter: JSONConverter[Feed] with {
    override def convert(feed: Feed): JSONValue = JSONObject(
      Map(
        "user" -> JSONConverter[User].convert(feed.user),
        "posts" -> JSONArray(feed.posts.map(JSONConverter[Post].convert))
      )
    )
  }

  // 3 - user-facing API
  object JSONConverter {
    def convert[T](value: T)(using converter: JSONConverter[T]): JSONValue = converter.convert(value)

    def apply[T](using instance: JSONConverter[T]): JSONConverter[T] = instance
  }

  // example
  val now = new Date(System.currentTimeMillis())
  val john = User("John", 34, "john@rockthejvm.con")
  val feed = Feed(
    john,
    List(
      Post("Hello, I'm learning type classes", now),
      Post("Look at this cute puppy!", now)
    )
  )

  // 4 - extension methods
  object JSONSyntax {
    extension [T](value: T) {
      def toIntermediate(using converter: JSONConverter[T]): JSONValue =
        converter.convert(value)

      def toJSON(using converter: JSONConverter[T]): String = toIntermediate.stringify
    }
  }

  def main(args: Array[String]): Unit = {
    import JSONSyntax.*

    println(data.stringify)
    println(JSONConverter.convert(feed).stringify)
    println(feed.toIntermediate.stringify)
    println(feed.toJSON) // ultimate expressiveness
  }
}
