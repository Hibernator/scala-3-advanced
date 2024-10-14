package com.rockthejvm.part4context

object TypeClasses {

  /*
    Small library to serialize data to a standard format (HTML)
   */

  // Version 1: the object-oriented way
  trait HTMLWritable {
    def toHtml: String
  }

  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    override def toHtml: String = s"<div>$name ($age yo) <a href=$email/></div>"
  }

  val bob: User = User("Bob", 43, "bob@rockthjvm.com")
  val bob2Html = bob.toHtml
  // same has to be done for other data structures that we want to serialize

  /*
    Drawbacks:
    - only available for the types that WE write
    - can only provide ONE implementation
   */

  // Version 2: pattern matching
  object HTMLSerializerPM {
    def serializeToHtml(value: Any): String = value match
      case User(name, age, email) => s"<div>$name ($age yo) <a href=$email/></div>"
      case _                      => throw new IllegalAccessException("data structure not supported")
  }

  /*
    Drawbacks:
    - lost type safety
    - need to modify a SINGLE piece of code every time when serialization support for a new type is needed
    - still only ONE implementation
   */

  // Version 3 - type class
  // part 1 - type class definition
  // denotes capability of serializing a type to String
  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

  // part 2 - instantiate type class instances for supported types
  given userSerializer: HTMLSerializer[User] with {
    override def serialize(value: User): String = {
      val User(name, age, email) = value
      s"<div>$name ($age yo) <a href=$email/></div>"
    }
  }

  val bob2Html_v2 = userSerializer.serialize(bob)

  /*
    Benefits:
    - can define serializers for other types OUTSIDE the "library"
    - multiple serializers for the same type, pick whichever you want
   */

  import java.util.Date
  given dateSerializer: HTMLSerializer[Date] with {
    override def serialize(date: Date): String = s"<div>${date.toString}</div>"
  }

  object SomeOtherSerializerFunctionality { // organize givens properly
    given partialUserSerializer: HTMLSerializer[User] with {
      override def serialize(user: User): String = s"<div>${user.name}</div>"
    }
  }

  // part 3 - using the type class (user-facing API for clients of the library)
  object HTMLSerializer {
    def serialize[T](value: T)(using serializer: HTMLSerializer[T]): String =
      serializer.serialize(value)

    // summon-like method, used in bob_v4
    def apply[T](using serializer: HTMLSerializer[T]): HTMLSerializer[T] = serializer
  }

  // all 3 parts of the pattern are decoupled from each other: type class trait, instances, the API to serialize

  val bob2Html_v3 = HTMLSerializer.serialize(bob)
  val bob2Html_v4 = HTMLSerializer[User].serialize(bob)

  // part 4 - extension methods: bring back the simplicity and expressiveness of using bob.toHtml
  object HTMLSyntax {
    extension [T](value: T) def toHTML(using serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  }

  import HTMLSyntax.* // imports the extension methods
  val bob2Html_v5 = bob.toHTML

  /*
    Cool!
    - extend functionality to new types we want to support
    - flexibility to add type class instances in a different place than the definition of the type class
    - choose between various implementations by importing the right givens
    - super expressive via extension methods
   */

  def main(args: Array[String]): Unit = {
    println(bob2Html)
    println(bob2Html_v2)
    println(bob2Html_v3)
    println(bob2Html_v4)
    println(bob2Html_v5)
  }
}
