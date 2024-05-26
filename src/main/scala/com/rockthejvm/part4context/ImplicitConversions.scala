package com.rockthejvm.part4context

// special import
import scala.language.implicitConversions

object ImplicitConversions {

  // Implicit conversions are usually needed with boxed types (types that wrap one value) to enable autoboxing

  case class Person(name: String) {
    def greet(): String = s"Hi, I'm $name, how are you?"
  }

  val daniel = Person("Daniel")
  val danielSaysHi = daniel.greet()

  // what if I use String and Person interchangeably in business logic?

  // special conversion instance, enables compiler to autobox String into Person
  given string2Person: Conversion[String, Person] with {
    override def apply(x: String): Person = Person(x)
  }

  val danielSaysHi_v2 = "Daniel".greet() // Person("Daniel").greet(), automatically by the compiler

  def processPerson(person: Person): String =
    if person.name.startsWith("J") then "OK" else "NOT OK"

  val isJaneOk = processPerson("Jane") // compiler rewrites to processPerson(Person("Jane"))

  // 2 things are required: the import and given converter

  /*
    Reasons for implicit conversions
    - auto-box types
    - use multiple types for the same code interchangeably (I might have 2 types that mean the same thing)
   */

  // Implicit conversion is not just simple autoboxing because the converter can have some logic

  def main(args: Array[String]): Unit = {}
}
