package com.rockthejvm.part4context

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object ContextFunctions {

  val aList = List(2, 1, 3, 4)
  val sortedList = aList.sorted // sorted method has a "using" clause

  // defs can have using clauses
  def methodWithoutContextArguments(nonContextArg: Int)(nonContextArg2: String): String = ???
  def methodWithContextArguments(nonContextArg: Int)(using nonContextArg2: String): String = ???

  // eta-expansion
  val functionWithoutContextArguments: Int => String => String =
    methodWithoutContextArguments // this is a function value

  // the same thing doesn't work for a method that has context/implicit arguments
//  val func2 = methodWithContextArguments // doesn't work

  // context function
  // second argument is implicit and marked with additional ?
  val functionWithContextArguments: Int => String ?=> String = methodWithContextArguments

  val someResult = functionWithContextArguments(2)(using "scala")

  /*
    Reasons for context functions
    - ability to convert methods with using clauses to function values
    - we can create higher-order functions with function values taking given instances as arguments
    - requiring given instances at CALL SITE, not at DEFINITION
   */

  // we would need to have an execution context here, in the scope of the function
  // this is pretty annoying because I should only need it where it's called, not where it's being defined

  // execution context required, doesn't work without one in scope
//  val incrementAsync: Int => Future[Int] = x => Future(x + 1)

  // this works because execution context is a given argument
  val incrementAsync: ExecutionContext ?=> Int => Future[Int] = x => Future(x + 1)

  def main(args: Array[String]): Unit = {}

}
