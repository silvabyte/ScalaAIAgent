package agents

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.*
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import agents.providers.{AnthropicProvider, OpenAIProvider, LLMProvider}

object Example extends App:

  // Example 1: Basic setup with factory methods
  def basicExample(): Unit =
    println("=== Basic Example ===")

    // Get API keys from environment variables
    val openaiKey = sys.env.get("OPENAI_API_KEY")
    val anthropicKey = sys.env.get("ANTHROPIC_API_KEY")

    openaiKey match
      case Some(key) =>
        AgentFactory.createOpenAIAgent("OpenAI Assistant", "You are a helpful assistant", key) match
          case Success(agent) =>
            val response = Await.result(agent.generateText("What is 2+2?"), 10.seconds)
            println(s"OpenAI Response: ${response.content}")
          case Failure(ex) =>
            println(s"Failed to create OpenAI agent: ${ex.getMessage}")
      case None =>
        println("OPENAI_API_KEY not found")

    anthropicKey match
      case Some(key) =>
        AgentFactory.createAnthropicAgent("Anthropic Assistant", "You are a helpful assistant", key) match
          case Success(agent) =>
            val response = Await.result(agent.generateText("Explain quantum computing in one sentence"), 10.seconds)
            println(s"Anthropic Response: ${response.content}")
          case Failure(ex) =>
            println(s"Failed to create Anthropic agent: ${ex.getMessage}")
      case None =>
        println("ANTHROPIC_API_KEY not found")

  // Example 2: Advanced configuration
  def advancedExample(): Unit =
    println("\n=== Advanced Example ===")

    sys.env.get("OPENAI_API_KEY") match
      case Some(key) =>
        val provider = new OpenAIProvider(key)

        val agent = Agent(
          name = "Creative Writer",
          instructions = "You are a creative writer who writes short, engaging stories",
          provider = provider,
          model = "gpt-4o-mini",
          temperature = Some(0.8), // Higher creativity
          maxTokens = Some(200)
        )

        val response = Await.result(
          agent.generateText("Write a short story about a robot discovering emotions"),
          15.seconds
        )

        println(s"Creative Story:\n${response.content}")

        // Show usage statistics
        response.usage.foreach { usage =>
          println(
            s"\nUsage: ${usage.promptTokens} prompt + ${usage.completionTokens} completion = ${usage.totalTokens} total tokens"
          )
        }

      case None =>
        println("OPENAI_API_KEY not found for advanced example")

  // Example 3: Conversation with history
  def conversationExample(): Unit =
    println("\n=== Conversation Example ===")

    sys.env.get("ANTHROPIC_API_KEY") match
      case Some(key) =>
        val agent = Agent(
          name = "Conversational AI",
          instructions = "You are a friendly conversational AI that remembers context",
          provider = new AnthropicProvider(key),
          model = "claude-3-5-haiku-20241022"
        )

        // First message
        val response1 = Await.result(
          agent.generateText("Hi, my name is Alice and I love programming"),
          10.seconds
        )
        println(s"Agent: ${response1.content}")

        // Second message - agent should remember the name
        val response2 = Await.result(
          agent.generateText("What programming language would you recommend for me?"),
          10.seconds
        )
        println(s"Agent: ${response2.content}")

        // Show conversation history
        println(s"\nConversation history has ${agent.getConversationHistory.size} messages")

      case None =>
        println("ANTHROPIC_API_KEY not found for conversation example")

  // Example 4: Error handling
  def errorHandlingExample(): Unit =
    println("\n=== Error Handling Example ===")

    // Try with invalid API key
    val invalidAgent = Agent(
      name = "Invalid Agent",
      instructions = "Test agent",
      provider = new OpenAIProvider("invalid-key"),
      model = "gpt-4o-mini"
    )

    try
      val response = Await.result(invalidAgent.generateText("Hello"), 10.seconds)
      println(s"Unexpected success: ${response.content}")
    catch
      case ex: LLMError =>
        println(s"Expected error: ${ex.message}")
      case ex =>
        println(s"Unexpected error: ${ex.getMessage}")

  // Example 5: Model comparison
  def modelComparisonExample(): Unit =
    println("\n=== Model Comparison Example ===")

    val prompt = "Explain artificial intelligence in exactly 20 words"

    for
      openaiKey <- sys.env.get("OPENAI_API_KEY")
      anthropicKey <- sys.env.get("ANTHROPIC_API_KEY")
    do
      // OpenAI GPT-4o-mini
      val openaiAgent = Agent(
        name = "OpenAI Agent",
        instructions = "You are precise and concise",
        provider = new OpenAIProvider(openaiKey),
        model = "gpt-4o-mini",
        temperature = Some(0.3)
      )

      // Anthropic Claude
      val anthropicAgent = Agent(
        name = "Anthropic Agent",
        instructions = "You are precise and concise",
        provider = new AnthropicProvider(anthropicKey),
        model = "claude-3-5-haiku-20241022",
        temperature = Some(0.3)
      )

      // Get responses
      val openaiResponse = Await.result(openaiAgent.generateText(prompt), 10.seconds)
      val anthropicResponse = Await.result(anthropicAgent.generateText(prompt), 10.seconds)

      println(s"OpenAI:    ${openaiResponse.content}")
      println(s"Anthropic: ${anthropicResponse.content}")

      // Compare word counts
      val openaiWords = openaiResponse.content.split("\\s+").length
      val anthropicWords = anthropicResponse.content.split("\\s+").length
      println(s"Word counts - OpenAI: $openaiWords, Anthropic: $anthropicWords")

  // Run examples
  try
    basicExample()
    advancedExample()
    conversationExample()
    errorHandlingExample()
    modelComparisonExample()
  catch
    case ex =>
      println(s"Example failed: ${ex.getMessage}")
      ex.printStackTrace()
