package agents

import upickle.default.*

sealed trait Role derives ReadWriter

object Role:
  case object System extends Role
  case object User extends Role
  case object Assistant extends Role

case class ChatMessage(
    role: Role,
    content: String
) derives ReadWriter

case class ChatRequest(
    messages: List[ChatMessage],
    model: String,
    temperature: Option[Double] = None,
    maxTokens: Option[Int] = None,
    stream: Boolean = false
) derives ReadWriter

case class Usage(
    promptTokens: Int,
    completionTokens: Int,
    totalTokens: Int
) derives ReadWriter

case class ChatResponse(
    content: String,
    usage: Option[Usage] = None,
    model: String,
    finishReason: Option[String] = None
) derives ReadWriter

case class LLMError(
    message: String,
    code: Option[String] = None,
    statusCode: Option[Int] = None
) extends Exception(message)
