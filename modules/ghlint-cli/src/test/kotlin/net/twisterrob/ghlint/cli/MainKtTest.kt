package net.twisterrob.ghlint.cli

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.beEmpty
import net.twisterrob.ghlint.test.captureSystemStreams
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources

/**
 * @see main
 */
class MainKtTest {

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `no args prints help`() {
		val result = captureSystemStreams {
			main("--no-exit")
		}

		result.stderr should beEmpty()
		val helpTxt = MainKtTest::class.java.getResourceAsStream("help.txt")
			?: error("Cannot find help.txt resource")
		val expectedHelp = helpTxt.reader().use { it.readText() }
		result.stdout.replace(System.lineSeparator(), "\n") shouldBe expectedHelp
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `--help prints expected output`() {
		val result = captureSystemStreams {
			main("--no-exit", "--help")
		}

		result.stderr should beEmpty()
		val helpTxt = MainKtTest::class.java.getResourceAsStream("help.txt")
			?: error("Cannot find help.txt resource")
		val expectedHelp = helpTxt.reader().use { it.readText() }
		result.stdout.replace(System.lineSeparator(), "\n") shouldBe expectedHelp
	}
}
