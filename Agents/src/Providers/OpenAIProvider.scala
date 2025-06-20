package agents.providers

import upickle.default.*
import scala.util.{Try, Success, Failure}
import agents.{ChatRequest, ChatResponse, Role, Usage, LLMError}

class OpenAIProvider(protected val apiKey: String) extends BaseLLMProvider:

  override val name: String = "OpenAI"
  override protected val baseUrl: String = "https://api.openai.com/v1"

  override val supportedModels: List[String] = List(
    "gpt-4o",
    "gpt-4o-mini",
    "gpt-4-turbo",
    "gpt-4",
    "gpt-3.5-turbo"
  )

  override protected def buildHeaders(apiKey: String): Map[String, String] =
    Map(
      "Authorization" -> s"Bearer $apiKey",
      "Content-Type" -> "application/json"
    )

  override protected def buildRequestBody(request: ChatRequest): ujson.Value =
    val messages = ujson.Arr(
      request.messages.map(msg =>
        ujson.Obj(
          "role" -> (msg.role match
            case Role.System    => "system"
            case Role.User      => "user"
            case Role.Assistant => "assistant"
          ),
          "content" -> msg.content
        )
      )*
    )

    val baseObj = ujson.Obj(
      "model" -> request.model,
      "messages" -> messages,
      "stream" -> request.stream
    )

    request.temperature.foreach(temp => baseObj("temperature") = temp)
    request.maxTokens.foreach(tokens => baseObj("max_tokens") = tokens)

    baseObj

  override protected def parseResponse(responseBody: String): Try[ChatResponse] =
    Try {
      val json = ujson.read(responseBody)

      if json.obj.contains("error") then
        val error = json("error")
        throw LLMError(
          message = error("message").str,
          code = error.obj.get("code").map(_.str)
        )

      val choice = json("choices")(0)
      val message = choice("message")
      val content = message("content").str
      val finishReason = choice.obj.get("finish_reason").map(_.str)

      val usage = json.obj.get("usage").map { u =>
        Usage(
          promptTokens = u("prompt_tokens").num.toInt,
          completionTokens = u("completion_tokens").num.toInt,
          totalTokens = u("total_tokens").num.toInt
        )
      }

      ChatResponse(
        content = content,
        usage = usage,
        model = json("model").str,
        finishReason = finishReason
      )
    }.recoverWith {
      case ex: ujson.ParsingFailedException =>
        Failure(LLMError(s"Failed to parse OpenAI response: ${ex.getMessage}"))
      case ex: NoSuchElementException =>
        Failure(LLMError(s"Missing required field in OpenAI response: ${ex.getMessage}"))
    }
