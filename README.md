# ScalaAIAgent

A Scala library for building AI agents with pluggable LLM providers. Easily switch between OpenAI, Anthropic, and other language model APIs without changing your core agent logic.

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

// Basic setup with factory methods
val openaiKey = "your-openai-api-key"

AgentFactory.createOpenAIAgent(
  name = "My Assistant",
  instructions = "You are a helpful assistant",
  apiKey = openaiKey
) match
  case Success(agent) =>
    val response = Await.result(agent.generateText("What is 2+2?"), 10.seconds)
    println(s"Response: ${response.content}")
  case Failure(ex) =>
    println(s"Error: ${ex.getMessage}")
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

### 4. Structured Response Generation

```scala
import agents.*
import ujson.{Obj, Arr}

// Define a schema for a person object
val personSchema = JsonSchema(
  schema = Obj(
    "type" -> "object",
    "properties" -> Obj(
      "name" -> Obj("type" -> "string"),
      "age" -> Obj("type" -> "number"),
      "skills" -> Obj(
        "type" -> "array",
        "items" -> Obj("type" -> "string")
      )
    ),
    "required" -> Arr("name", "age")
  ),
  description = Some("A person with name, age, and skills")
)

val agent = Agent(
  name = "Data Extractor",
  instructions = "Extract structured data from user input",
  provider = new OpenAIProvider("your-api-key"),
  model = "gpt-4o"
)

// Generate structured object
val response = Await.result(
  agent.generateObject("Create a profile for a software engineer named Alex", personSchema),
  10.seconds
)

println(response.`object`)
// Output: {"name": "Alex", "age": 28, "skills": ["Scala", "Java", "Python"]}
```

### 5. Streaming Structured Responses

```scala
import scala.concurrent.ExecutionContext.Implicits.global

val streamResponse = agent.streamObject(
  "Generate a detailed character profile for a fantasy novel",
  characterSchema
)

streamResponse.foreach { iterator =>
  iterator.foreach { chunk =>
    if (chunk.isComplete) {
      println(s"Final object: ${chunk.partialObject}")
    } else {
      println(s"Partial: ${chunk.partialObject}")
    }
  }
}
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
- `generateObject(userChatMessage: String, schema: JsonSchema): Future[ObjectResponse]` - Generate structured object with conversation history
- `generateObjectWithoutHistory(userChatMessage: String, schema: JsonSchema): Future[ObjectResponse]` - Generate structured object without history
- `streamObject(userChatMessage: String, schema: JsonSchema): Future[Iterator[StreamingObjectResponse]]` - Stream structured object generation
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
  def generateObject(request: ObjectRequest): Future[ObjectResponse]
  def streamObject(request: ObjectRequest): Future[Iterator[StreamingObjectResponse]]
  def validateModel(model: String): Boolean
```

#### `AgentFactory`

Factory methods for common agent configurations.

```scala
object AgentFactory:
  def createOpenAIAgent(name: String, instructions: String, apiKey: String, model: String = "gpt-4o-mini"): Try[Agent]
  def createAnthropicAgent(name: String, instructions: String, apiKey: String, model: String = "claude-3-5-haiku-20241022"): Try[Agent]
  def createAgent(name: String, instructions: String, provider: LLMProvider, model: String): Try[Agent]
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

Run all examples: `./mill Agents.runMain agents.Example`

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
import scala.util.{Try, Success, Failure}

Try(Await.result(agent.generateText("Hello"), 10.seconds)) match
  case Success(response) =>
    println(response.content)
  case Failure(ex: LLMError) =>
    println(s"LLM Error: ${ex.message}")
    ex.code.foreach(code => println(s"Error code: $code"))
  case Failure(ex) =>
    println(s"Unexpected error: ${ex.getMessage}")
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

### Structured Data Extraction

```scala
import ujson.{Obj, Arr}

val extractionSchema = JsonSchema(
  schema = Obj(
    "type" -> "object",
    "properties" -> Obj(
      "entities" -> Obj(
        "type" -> "array",
        "items" -> Obj(
          "type" -> "object",
          "properties" -> Obj(
            "name" -> Obj("type" -> "string"),
            "type" -> Obj("type" -> "string"),
            "confidence" -> Obj("type" -> "number")
          )
        )
      ),
      "summary" -> Obj("type" -> "string")
    )
  ),
  description = Some("Extract entities and provide a summary")
)

val agent = Agent(
  name = "Entity Extractor",
  instructions = "Extract entities from text and provide analysis",
  provider = new OpenAIProvider("your-key"),
  model = "gpt-4o"
)

val text = "Apple Inc. was founded by Steve Jobs in 1976. The company is headquartered in Cupertino, California."

val result = Await.result(
  agent.generateObject(s"Extract entities from: $text", extractionSchema),
  10.seconds
)

println(s"Extracted data: ${result.`object`}")
```

### Schema Validation with Different Providers

```scala
val taskSchema = JsonSchema(
  schema = Obj(
    "type" -> "object",
    "properties" -> Obj(
      "tasks" -> Obj(
        "type" -> "array",
        "items" -> Obj(
          "type" -> "object",
          "properties" -> Obj(
            "title" -> Obj("type" -> "string"),
            "priority" -> Obj("type" -> "string", "enum" -> Arr("high", "medium", "low")),
            "deadline" -> Obj("type" -> "string", "format" -> "date"),
            "completed" -> Obj("type" -> "boolean")
          ),
          "required" -> Arr("title", "priority")
        )
      )
    )
  )
)

// Test with both providers
val openaiAgent = Agent("OpenAI Planner", "Create task lists", new OpenAIProvider(key1), "gpt-4o")
val anthropicAgent = Agent("Claude Planner", "Create task lists", new AnthropicProvider(key2), "claude-3-5-sonnet-20241022")

val prompt = "Create a task list for planning a birthday party"

val openaiTasks = Await.result(openaiAgent.generateObject(prompt, taskSchema), 10.seconds)
val anthropicTasks = Await.result(anthropicAgent.generateObject(prompt, taskSchema), 10.seconds)

println(s"OpenAI tasks: ${openaiTasks.`object`}")
println(s"Anthropic tasks: ${anthropicTasks.`object`}")
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
  │   ├── OpenAIProvider (OpenAI API implementation with JSON Schema)
  │   └── AnthropicProvider (Anthropic API implementation with tools)
  ├── ChatMessage history (automatic conversation tracking)
  └── Structured Response Support
      ├── JsonSchema (schema definitions)
      ├── ObjectRequest/ObjectResponse (structured generation)
      └── StreamingObjectResponse (streaming structured data)
```

## Contributing

1. Add new provider implementations by extending `BaseLLMProvider`
2. Implement the required abstract methods:
   - `buildHeaders` - HTTP headers for API authentication
   - `buildRequestBody` - Convert ChatRequest to provider-specific format
   - `parseResponse` - Parse text response from provider
   - `buildObjectRequestBody` - Convert ObjectRequest to provider-specific format
   - `parseObjectResponse` - Parse structured object response from provider
3. Add comprehensive tests for new providers
4. Update documentation and examples
5. Ensure both text and structured response generation work correctly

## License

This project is licensed under the MIT License.
