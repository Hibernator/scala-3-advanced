package com.rockthejvm.part5ts

object OpaqueTypes {

  object SocialNetwork {
    // some data structures - domain
    opaque type Name = String

    object Name {
      def apply(str: String): Name = str
    }

    extension (name: Name) def length: Int = name.length // using String API

    // inside the object, Name and String can be used interchangeably
    def addFriend(person1: Name, person2: Name): Boolean =
      person1.length == person2.length // entire String API can be used
  }

  // outside SocialNetwork, Name and String are NOT related
  // opaque modifier disconnects the type alias from its actual implementation
  import SocialNetwork.*
//  val name: Name = "Daniel" // will not compile

  /*
    Why is it useful:
    - so that my API is not polluted with the actual type API, or I want to prevent access to it
    - we don't need or want to expose access to the entire String API for the Name type
    - instead, we can define API of our own choosing via companion objects and extension methods
   */

  object Graphics {
    opaque type Color = Int // in hex
    opaque type ColorFilter <: Color = Int

    val Red: Color = 0xff000000
    val Green: Color = 0x00ff0000
    val Blue: Color = 0x0000ff00
    val halfTransparency: ColorFilter = 0x88 // 50% transparency
  }

  import Graphics.*
  case class OverlayFilter(c: Color)
  val fadeLayer = OverlayFilter(halfTransparency) // ColorFilter <: Color

  // How can we create instances of opaque types and access their APIs?
  // 1 - through companion objects
  val aName: Name = Name("Daniel")
  // 2 - through extension methods
  val nameLength = aName.length // works because there is an extension method for this

  def main(args: Array[String]): Unit = {}
}
