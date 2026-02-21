package net.twisterrob.ghlint.cli

import io.kotest.matchers.should
import io.kotest.matchers.string.beEmpty
import net.twisterrob.ghlint.test.captureSystemStreams
import net.twisterrob.ghlint.test.readResourceText
import net.twisterrob.ghlint.test.shouldEqualIgnoringEOL
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
		result.stdout shouldEqualIgnoringEOL MainKtTest::class.java.readResourceText("help.txt")
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `--help prints expected output`() {
		val result = captureSystemStreams {
			main("--no-exit", "--help")
		}

		result.stderr should beEmpty()
		result.stdout shouldEqualIgnoringEOL MainKtTest::class.java.readResourceText("help.txt")
	}
}
