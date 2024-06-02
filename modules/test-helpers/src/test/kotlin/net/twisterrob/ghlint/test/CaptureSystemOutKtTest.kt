package net.twisterrob.ghlint.test

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources

class CaptureSystemOutKtTest {

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	fun exampleUsage() {
		val out = captureSystemOut {
			print("Hello")
		}
		out shouldBe "Hello"
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	fun `captures single line`() {
		val out = captureSystemOut {
			println("Hello")
		}
		out shouldBe "Hello${System.lineSeparator()}"
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	fun `captures many lines`() {
		val out = captureSystemOut {
			println("Hello")
			println("GH-Lint")
			println("Testing")
		}
		out shouldBe """
			Hello
			GH-Lint
			Testing
			
		""".trimIndent().replace("\n", System.lineSeparator())
	}
}
