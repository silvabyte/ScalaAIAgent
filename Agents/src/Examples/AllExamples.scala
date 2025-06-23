package agents.examples

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.*
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Main runner that executes all example applications.
 * 
 * Usage: ./mill Agents.runMain agents.examples.AllExamples
 * 
 * Or run individual examples:
 * - ./mill Agents.runMain agents.examples.BasicExample
 * - ./mill Agents.runMain agents.examples.AdvancedExample
 * - ./mill Agents.runMain agents.examples.ConversationExample
 * - ./mill Agents.runMain agents.examples.ErrorHandlingExample
 * - ./mill Agents.runMain agents.examples.ModelComparisonExample
 * - ./mill Agents.runMain agents.examples.StructuredResponseExample
 * - ./mill Agents.runMain agents.examples.EntityExtractionExample
 */
object AllExamples extends App:
  
  println("Running all ScalaAIAgent examples...")
  println("=" * 60)
  
  import scala.util.{Try, Success, Failure}
  
  Try {
    // Run all examples in sequence
    BasicExample.main(Array.empty)
    Thread.sleep(1000) // Brief pause between examples
    
    AdvancedExample.main(Array.empty)
    Thread.sleep(1000)
    
    ConversationExample.main(Array.empty)
    Thread.sleep(1000)
    
    ErrorHandlingExample.main(Array.empty)
    Thread.sleep(1000)
    
    ModelComparisonExample.main(Array.empty)
    Thread.sleep(1000)
    
    StructuredResponseExample.main(Array.empty)
    Thread.sleep(1000)
    
    EntityExtractionExample.main(Array.empty)
  } match {
    case Success(_) =>
      println("\n" + "=" * 60)
      println("All examples completed successfully!")
    case Failure(ex) =>
      println(s"Example execution failed: ${ex.getMessage}")
      ex.printStackTrace()
  }