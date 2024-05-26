package com.rockthejvm.part4context

object OrganizingCAs {

  val aList = List(2, 3, 1, 4)
  val anOrderedList = aList.sorted

  // compiler fetches givens/extension methods from the following places
  // 1 - local scope, same scope as where the method is being called

  // this has a preference over the ordering defined in the standard library
  given reverseOrdering: Ordering[Int] with {
    override def compare(x: Int, y: Int): Int = y - x
  }

  //  2 - imported scope: all the explicitly imported values from other places/packages
  case class Person(name: String, age: Int)
  val persons = List(
    Person("Steve", 30),
    Person("Amy", 22),
    Person("John", 67)
  )

  object PersonGivens {
    given ageOrdering: Ordering[Person] with {
      override def compare(x: Person, y: Person): Int = y.age - x.age
    }

    extension (p: Person) def greet(): String = s"Heya, I'm ${p.name}, I'm so glad to meet you"

  }

  // Several ways to import givens
  // a - import explicitly
//  import PersonGivens.ageOrdering

  // b - import a given for a particular type, compiler will search for it in the given location
  // useful when I don't know the name of the given instance
//  import PersonGivens.{given Ordering[Person]}

  // c - import all givens
//  import PersonGivens.given

  // warning: import PersonGivens.* does NOT also import given instances! Givens have to be marked explicitly for import. But it imports extension methods
//  import PersonGivens.*

  // 3 - companion objects of all types involved in method signature
  /*
    - Ordering companion object
    - List companion object
    - Person companion object
   */
  // def sorted[B >: A](using ord: Ordering[B]): List[B]

  object Person {
    given byNameOrdering: Ordering[Person] with {
      override def compare(x: Person, y: Person): Int = x.name.compareTo(y.name)
    }

    extension (p: Person) def greet(): String = s"Hello, I'm ${p.name}"
  }

  val sortedPersons = persons.sorted

  /*
    Good practices for organizing givens:
    1. When there is a "default" given that works in most cases (or is the only one that makes sense), add it in the companion object of the type.
    2. When there are several possible givens, but ONE is dominant (used most), add it in the companion object and the rest in other places.
    3. When there are MANY possible givens and NO ONE is dominant, add them in separate objects and import explicitly as needed.
   */

  // Same principles apply to extension methods as well.

  /*
    Exercises.
    Create given instances for Ordering[Purchase]
    - ordering by total price, descending = 50% of codebase
    - ordering by unit count, descending = 25% of code base
    - ordering by unit price, ascending = 25% of code base
   */

  case class Purchase(nUnits: Int, unitPrice: Double)

  object Purchase {
    given orderingByTotalPriceDesc: Ordering[Purchase] with {
      override def compare(x: Purchase, y: Purchase): Int = -(x.nUnits * x.unitPrice).compareTo(y.nUnits * y.unitPrice)
    }
  }

  object UnitCountOrdering {
    given unitCountOrdering: Ordering[Purchase] = Ordering.fromLessThan((x, y) => y.nUnits > x.nUnits)
  }

  object UnitPriceOrdering {
    given orderingByUnitPriceAsc: Ordering[Purchase] = Ordering.fromLessThan((x, y) => x.unitPrice < y.unitPrice)
  }

  def main(args: Array[String]): Unit = {
    println(anOrderedList)
    println(sortedPersons)

    import PersonGivens.* // includes extension methods, but not givens
    println(Person("Daniel", 99).greet())
  }
}
