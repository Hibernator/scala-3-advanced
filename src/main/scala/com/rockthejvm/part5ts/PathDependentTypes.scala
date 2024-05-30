package com.rockthejvm.part5ts

object PathDependentTypes {

  class Outer {
    class Inner
    object InnerObject
    type InnerType

    def process(arg: Inner) = println(arg) // this will only be applicable to this instance-dependent type
    def processGeneral(arg: Outer#Inner) = println(arg) // this takes a parent of all possible inner types
  }

  val outer = new Outer
  val inner = new outer.Inner // outer.Inner is a separate TYPE = path-dependent type, it depends on instance of Outer

  val outerA = new Outer
  val outerB = new Outer
//  val inner2: outerA.Inner = new outerB.Inner // illegal, type mismatch because they belong to different instances of Outer

  // these two are of different type
  val innerA = new outerA.Inner
  val innerB = new outerB.Inner

//  outerA.process(innerB) // illegal, because of type mismatch
  outer.process(inner) // inner type is of the right path-dependent type

  // all the path/instance-dependent types are children of a bigger type
  // parent-type: Outer#Inner - depends on class, not instance
  outerA.processGeneral(innerA) // this satisfies even the instance-dependent type
  outerA.processGeneral(innerB) // also works. The path-dependent type is different but outerB.Inner <: Outer#Inner

  /*
    Why are path-dependent types useful
    - type checking/type inference - Akka Streams: Flow[Int, Int, NotUsed]#Repr
    - type-level programming
   */

  // methods with path-dependent types: return a different COMPILE-TIME type depending on the argument
  // no need for generics

  trait Record {
    type Key
    def defaultValue: Key
  }

  class StringRecord extends Record {
    override type Key = String

    override def defaultValue: String = ""
  }

  class IntRecord extends Record {
    override type Key = Int

    override def defaultValue: Int = 0
  }

  // user-facing API for above

  // different type will be returned depending on the actual instance of record, without any generics
  // provides type-safety
  def getDefaultIdentifier(record: Record): record.Key = record.defaultValue

  // compiler can figure out the correct type of the return value depending on the argument being passed to it
  val aString: String = getDefaultIdentifier(new StringRecord)
  val anInt: Int = getDefaultIdentifier(new IntRecord)

  // function with dependent types (needed for higher-order functions)
  // eta-expansion, Record#Key is the most specific type that can be inferred
  val getIdentifierFunc: Record => Record#Key = getDefaultIdentifier

  def main(args: Array[String]): Unit = {}
}
