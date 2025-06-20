# ScalaAIAgent

A Scala library for building AI agents with pluggable LLM providers. Easily switch between OpenAI, Anthropic, and other language model APIs without changing your core agent logic.

## Features

- **Provider Abstraction**: Switch between LLM providers (OpenAI, Anthropic) without code changes
- **Async/Non-blocking**: Built with Scala Futures for efficient concurrent operations
- **Conversation Memory**: Conversation history tracking
- **Type Safety**: Leverages Scala's type system for reliable API interactions
- **JSON Serialization**: Built-in JSON support using uPickle
- **Error Handling**: Comprehensive error handling for API failures
- **Flexible Configuration**: Customizable temperature, max tokens, and other parameters

## Supported Providers

- **OpenAI**: GPT-4o, GPT-4o-mini, GPT-4-turbo, GPT-4, GPT-3.5-turbo
- **Anthropic**: Claude 3.5 Sonnet, Claude 3.5 Haiku, Claude 3 Opus, Claude 3 Sonnet, Claude 3 Haiku

## Quick Start

### 1. Setup Dependencies

//TODO: add this once an initial release version is cut

### 2. Basic Usage

```scala
import agents.*
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.*
import scala.util.{Success, Failure}

// Quick setup with factory methods
val openaiKey = "your-openai-api-key"

AgentFactory.quickOpenAI(openaiKey, "You are a helpful assistant") match
  case Success(agent) =>
    val response = Await.result(agent.generateText("What is 2+2?"), 10.seconds)
    println(s"Response: ${response.content}")
  case Failure(ex) =>
    println(s"Error: ${ex.getChatMessage}")
```

### 3. Advanced Configuration

```scala
import agents.*

// Custom configuration
val provider = new OpenAIProvider("your-api-key")

val agent = Agent(
  name = "Creative Writer",
  instructions = "You are a creative writer who writes engaging stories",
  provider = provider,
  model = "gpt-4o-mini",
  temperature = Some(0.8), // Higher creativity
  maxTokens = Some(200)
)

val response = Await.result(
  agent.generateText("Write a short story about a robot"),
  15.seconds
)

println(response.content)
```

## API Reference

### Core Classes

#### `Agent`

The main orchestrator for AI interactions.

```scala
case class Agent(config: AgentConfig)
```

**Methods:**

- `generateText(userChatMessage: String): Future[ChatResponse]` - Generate response with conversation history
- `generateTextWithoutHistory(userChatMessage: String): Future[ChatResponse]` - Generate response without history
- `getConversationHistory: List[ChatMessage]` - Get current conversation
- `clearHistory(): Unit` - Clear conversation history
- `withSystemChatMessage(systemChatMessage: String): Agent` - Create new agent with different instructions

#### `LLMProvider`

Abstract interface for language model providers.

```scala
trait LLMProvider:
  def name: String
  def supportedModels: List[String]
  def chat(request: ChatRequest): Future[ChatResponse]
  def validateModel(model: String): Boolean
```

#### `AgentFactory`

Factory methods for common agent configurations.

```scala
object AgentFactory:
  def createOpenAIAgent(name: String, instructions: String, apiKey: String, model: String = "gpt-4o-mini"): Try[Agent]
  def createAnthropicAgent(name: String, instructions: String, apiKey: String, model: String = "claude-3-5-haiku-20241022"): Try[Agent]
  def quickOpenAI(apiKey: String, instructions: String): Try[Agent]
  def quickAnthropic(apiKey: String, instructions: String): Try[Agent]
```

### Provider Implementations

#### `OpenAIProvider`

```scala
class OpenAIProvider(apiKey: String) extends BaseLLMProvider

// Supported models:
// - gpt-4o
// - gpt-4o-mini
// - gpt-4-turbo
// - gpt-4
// - gpt-3.5-turbo
```

#### `AnthropicProvider`

```scala
class AnthropicProvider(apiKey: String) extends BaseLLMProvider

// Supported models:
// - claude-3-5-sonnet-20241022
// - claude-3-5-haiku-20241022
// - claude-3-opus-20240229
// - claude-3-sonnet-20240229
// - claude-3-haiku-20240307
```

## Examples

### Conversation with Memory

```scala
val agent = Agent(
  name = "Conversational AI",
  instructions = "You are a friendly AI that remembers context",
  provider = new AnthropicProvider("your-key"),
  model = "claude-3-5-haiku-20241022"
)

// First message
val response1 = Await.result(
  agent.generateText("Hi, my name is Alice"),
  10.seconds
)

// Second message - agent remembers the name
val response2 = Await.result(
  agent.generateText("What's my name?"),
  10.seconds
)
```

### Error Handling

```scala
try
  val response = Await.result(agent.generateText("Hello"), 10.seconds)
  println(response.content)
catch
  case ex: LLMError =>
    println(s"LLM Error: ${ex.message}")
    ex.code.foreach(code => println(s"Error code: $code"))
  case ex =>
    println(s"Unexpected error: ${ex.getChatMessage}")
```

### Model Comparison

```scala
val prompt = "Explain AI in 20 words"

val openaiAgent = Agent("OpenAI", "Be concise", new OpenAIProvider(key1), "gpt-4o-mini")
val anthropicAgent = Agent("Claude", "Be concise", new AnthropicProvider(key2), "claude-3-5-haiku-20241022")

val openaiResponse = Await.result(openaiAgent.generateText(prompt), 10.seconds)
val anthropicResponse = Await.result(anthropicAgent.generateText(prompt), 10.seconds)

println(s"OpenAI: ${openaiResponse.content}")
println(s"Anthropic: ${anthropicResponse.content}")
```

## Building and Testing

```bash
# Compile the library
./mill Agents.compile

# Run tests
./mill Agents.test.test

# Run examples (requires API keys in environment)
export OPENAI_API_KEY="your-key"
export ANTHROPIC_API_KEY="your-key"
./mill Agents.run
```

## Environment Variables

Set these environment variables for the examples:

- `OPENAI_API_KEY` - Your OpenAI API key
- `ANTHROPIC_API_KEY` - Your Anthropic API key

## Architecture

The library uses a provider pattern to abstract different LLM APIs:

```
Agent
  ├── AgentConfig (name, instructions, provider, model, etc.)
  ├── LLMProvider (interface)
  │   ├── OpenAIProvider (OpenAI API implementation)
  │   └── AnthropicProvider (Anthropic API implementation)
  └── ChatMessage history (automatic conversation tracking)
```

Key design principles:

- **Provider abstraction** - Switch LLM backends without code changes
- **Async by default** - All operations return Futures
- **Type safety** - Strong typing for requests/responses
- **Error handling** - Comprehensive error types and handling
- **Memory management** - Automatic conversation history tracking

## Contributing

1. Add new provider implementations by extending `BaseLLMProvider`
2. Implement the required abstract methods: `buildHeaders`, `buildRequestBody`, `parseResponse`
3. Add comprehensive tests for new providers
4. Update documentation and examples

## License

This project is licensed under the MIT License.
