import utest.*
import agents.*
import agents.providers.*
import scala.util.{Success, Failure}

object ProviderSpec extends TestSuite:

  val tests = Tests {

    test("OpenAI provider configuration") {
      val provider = new OpenAIProvider("test-api-key")

      assert(provider.name == "OpenAI")
      assert(provider.supportedModels.contains("gpt-4o"))
      assert(provider.supportedModels.contains("gpt-4o-mini"))
      assert(provider.supportedModels.contains("gpt-3.5-turbo"))
      assert(provider.validateModel("gpt-4o"))
      assert(!provider.validateModel("unsupported-model"))
    }

    test("Anthropic provider configuration") {
      val provider = new AnthropicProvider("test-api-key")

      assert(provider.name == "Anthropic")
      assert(provider.supportedModels.contains("claude-3-5-sonnet-20241022"))
      assert(provider.supportedModels.contains("claude-3-5-haiku-20241022"))
      assert(provider.validateModel("claude-3-5-haiku-20241022"))
      assert(!provider.validateModel("unsupported-model"))
    }

  }
