package com.rockthejvm.part5ts

object LiteralUnionIntersectionTypes {

  // 1 - Literal types
  val aNum = 3 // this is a usual Int
  val three: 3 = 3 // 3 <: Int

  def passNumber(n: Int) = println(n)
  passNumber(45)
  passNumber(three) // works because 3 <: Int

  def passStrict(n: 3) = println(n)
  passStrict(3) // works
  passStrict(three) // also works
//  passStrict(45) // illegal, because the type doesn't match

  // available for double, boolean, string
  val pi: 3.14 = 3.14
  val truth: true = true
  val favLang: "Scala" = "Scala"

  // literal types can be used as type arguments (just like any other types)
  def doSomethingWithYourLife(meaning: Option[42]) = meaning.foreach(println)

  // 2 - union types
  val truthOr42: Boolean | Int = 43
  def ambivalentMethod(arg: String | Int) = arg match // this pattern match is complete
    case _: String => "a string"
    case _: Int    => "a number"

  val aNumber = ambivalentMethod(56)
  val aString = ambivalentMethod("Scala")

  // type inference with union types - chooses lowest common ancestor of the two types instead of the String | Int
  val stringOrInt = if (43 > 0) "a string" else 45 // type automatically inferred as Any
  val stringOrInt_2: String | Int = if (43 > 0) "a string" else 45 // union type added manually and it still compiles

  // union types are useful for compile-time checking of nulls
  type Maybe[T] = T | Null
  def handleMaybe(someValue: Maybe[String]): Int =
    // after the first check, compiler knows it's not null, therefore I have access to String API (length method)
    // flow typing - compiler feature, that infer a more specific types for variables as the code goes along
    // flow typing is only available for union types where one of the types is Null
    if (someValue != null) someValue.length else 0

  type ErrorOr[T] = T | "error"
//  def handleResource(arg: ErrorOr[Int]): Unit =
//    if (arg != "error") println(arg + 1) // flow typing doesn't work here
//    else println("Error!")

  // 3 - intersection types
  class Animal
  trait Carnivore
  class Crocodile extends Animal with Carnivore

  val carnivoreAnimal: Animal & Carnivore = new Crocodile

  trait Gadget {
    def use(): Unit
  }

  trait Camera extends Gadget {
    def takePicture() = println("smile!")
    override def use() = println("snap")
  }

  trait Phone extends Gadget {
    def makePhoneCall() = println("calling...")
    override def use() = println("ring")
  }

  def useSmartDevice(smartphone: Camera & Phone): Unit = {
    // now I can call methods from both traits
    smartphone.takePicture()
    smartphone.makePhoneCall()
    // which use is being called? Depends on the specific class, diamond problem. Doesn't depend on the method signature
    smartphone.use()
  }

  class SmartPhone extends Phone with Camera // diamond problem
  class CameraWithPhone extends Camera with Phone

  // intersection types and covariance
  trait HostConfig
  trait HostController {
    def get: Option[HostConfig]
  }

  trait PortConfig
  trait PortController {
    def get: Option[PortConfig]
  }

  def getConfigs(controller: HostController & PortController): Option[HostConfig & PortConfig] = controller.get
  // code compiles even despite conflicting method definitions
  // return type is simpler than Option[HostConfig] & Option[PortConfig]
  // the compilation and return type is possible because Option is covariant

  def main(args: Array[String]): Unit = {
    useSmartDevice(new SmartPhone) // snap
    useSmartDevice(new CameraWithPhone) // ring
  }
}
