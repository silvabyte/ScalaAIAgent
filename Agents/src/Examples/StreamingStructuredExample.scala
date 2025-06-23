package agents.examples

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.*
import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import agents.*
import agents.providers.OpenAIProvider
import ujson.{Obj, Arr}

object StreamingStructuredExample extends App:
  println("=== Streaming Structured Response Example ===")

  val taskSchema = JsonSchema(
    schema = Obj(
      "type" -> "object",
      "properties" -> Obj(
        "project_name" -> Obj("type" -> "string"),
        "tasks" -> Obj(
          "type" -> "array",
          "items" -> Obj(
            "type" -> "object",
            "properties" -> Obj(
              "title" -> Obj("type" -> "string"),
              "description" -> Obj("type" -> "string"),
              "priority" -> Obj("type" -> "string", "enum" -> Arr("high", "medium", "low")),
              "estimated_hours" -> Obj("type" -> "number")
            ),
            "required" -> Arr("title", "priority")
          )
        )
      ),
      "required" -> Arr("project_name", "tasks")
    ),
    description = Some("A project with multiple tasks")
  )

  sys.env.get("OPENAI_API_KEY") match
    case Some(key) =>
      val agent = Agent(
        name = "Project Planner",
        instructions = "Create detailed project plans with tasks",
        provider = new OpenAIProvider(key),
        model = "gpt-4o"
      )

      Try {
        val streamResponse = agent.streamObject(
          "Create a project plan for building a mobile app",
          taskSchema
        )

        streamResponse.foreach { iterator =>
          var chunkCount = 0
          iterator.foreach { chunk =>
            chunkCount += 1
            if (chunk.isComplete) {
              println(s"Final project plan (after $chunkCount chunks): ${chunk.partialObject}")
            } else {
              println(s"Chunk $chunkCount: ${chunk.partialObject}")
            }
          }
        }
      } match {
        case Success(_) =>
          // Successfully completed streaming
          // noop
          ()
        case Failure(ex: LLMError) =>
          println(s"LLM Error: ${ex.message}")
        case Failure(ex) =>
          println(s"Error: ${ex.getMessage}")
      }

    case None =>
      println("OPENAI_API_KEY not found for streaming example")
