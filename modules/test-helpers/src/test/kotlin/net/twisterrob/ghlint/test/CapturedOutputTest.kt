package net.twisterrob.ghlint.test

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.beEmpty
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources

@Suppress(
	"detekt.ForbiddenMethodCall", // Testing console output.
	"ReplaceJavaStaticMethodWithKotlinAnalog", // Be explicit for readability.
)
// Capturing system streams is not thread-safe.
// Even though we could declare resource locks for each test, it's not good enough.
// Each test will be affected by both stdout and stderr, so they cannot be executed in parallel in any way.
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
class CapturedOutputTest {

	/**
	 * @see captureSystemStreams
	 */
	@Nested
	inner class Streams {
		@Test
		fun exampleUsage() {
			val captured = captureSystemStreams {
				System.out.print("Hello")
				System.err.print("World")
				42
			}
			captured.stdout shouldBe "Hello"
			captured.stderr shouldBe "World"
			captured.result shouldBe 42
		}

		@Test
		fun `captures interleaved multiline output`() {
			val captured = captureSystemStreams {
				System.out.println("Hello")
				System.err.println("World")
				System.out.println("GH-Lint")
				System.err.println("Testing")
				42
			}
			captured.result shouldBe 42
			captured.stdout shouldBe """
				Hello
				GH-Lint
				
			""".trimIndent().replace("\n", System.lineSeparator())
			captured.stderr shouldBe """
				World
				Testing
				
			""".trimIndent().replace("\n", System.lineSeparator())
		}
	}

	/**
	 * @see captureSystemOut
	 * @see captureSystemOutOnly
	 */
	@Nested
	inner class Output {
		@Test
		fun exampleUsage() {
			val captured = captureSystemOut {
				print("Hello")
				42
			}
			captured.stdout shouldBe "Hello"
			captured.stderr should beEmpty()
			captured.result shouldBe 42
		}

		@Test
		fun `captures single line Unit`() {
			val captured = captureSystemOutOnly {
				System.out.println("Hello")
			}
			captured shouldBe "Hello${System.lineSeparator()}"
		}

		@Test
		fun `captures many lines Unit`() {
			val captured = captureSystemOutOnly {
				System.out.println("Hello")
				System.out.println("GH-Lint")
				System.out.println("Testing")
			}
			captured shouldBe """
				Hello
				GH-Lint
				Testing
				
			""".trimIndent().replace("\n", System.lineSeparator())
		}

		@Test
		fun `captures single line R`() {
			val captured = captureSystemOut {
				System.out.println("Hello")
				"42"
			}
			captured.result shouldBe "42"
			captured.stderr should beEmpty()
			captured.stdout shouldBe "Hello${System.lineSeparator()}"
		}

		@Test
		fun `captures many lines R`() {
			val captured = captureSystemOut {
				System.out.println("Hello")
				System.out.println("GH-Lint")
				System.out.println("Testing")
				"42"
			}
			captured.result shouldBe "42"
			captured.stderr should beEmpty()
			captured.stdout shouldBe """
				Hello
				GH-Lint
				Testing
				
			""".trimIndent().replace("\n", System.lineSeparator())
		}

		@Test
		fun `ignores error stream`() {
			val captured = captureSystemOut {
				System.err.println("Hello")
				42
			}
			captured.result shouldBe 42
			captured.stderr should beEmpty()
			captured.stdout should beEmpty()
		}
	}

	/**
	 * @see captureSystemErr
	 * @see captureSystemErrOnly
	 */
	@Nested
	inner class Error {
		@Test
		fun exampleUsage() {
			val captured = captureSystemErr {
				System.err.print("Hello")
				42
			}
			captured.stdout should beEmpty()
			captured.stderr shouldBe "Hello"
			captured.result shouldBe 42
		}

		@Test
		fun `captures single line Unit`() {
			val captured = captureSystemErrOnly {
				System.err.println("Hello")
			}
			captured shouldBe "Hello${System.lineSeparator()}"
		}

		@Test
		fun `captures many lines Unit`() {
			val captured = captureSystemErrOnly {
				System.err.println("Hello")
				System.err.println("GH-Lint")
				System.err.println("Testing")
			}
			captured shouldBe """
				Hello
				GH-Lint
				Testing
				
			""".trimIndent().replace("\n", System.lineSeparator())
		}

		@Test
		fun `captures single line R`() {
			val captured = captureSystemErr {
				System.err.println("Hello")
				"42"
			}
			captured.result shouldBe "42"
			captured.stdout should beEmpty()
			captured.stderr shouldBe "Hello${System.lineSeparator()}"
		}

		@Test
		fun `captures many lines R`() {
			val captured = captureSystemErr {
				System.err.println("Hello")
				System.err.println("GH-Lint")
				System.err.println("Testing")
				"42"
			}
			captured.result shouldBe "42"
			captured.stdout should beEmpty()
			captured.stderr shouldBe """
				Hello
				GH-Lint
				Testing
				
			""".trimIndent().replace("\n", System.lineSeparator())
		}

		@Test
		fun `ignores output stream`() {
			val captured = captureSystemErr {
				System.out.println("Hello")
				42
			}
			captured.result shouldBe 42
			captured.stdout should beEmpty()
			captured.stderr should beEmpty()
		}
	}
}
