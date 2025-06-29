package net.twisterrob.ghlint.cli

import io.kotest.matchers.should
import io.kotest.matchers.string.contain
import net.twisterrob.ghlint.test.captureSystemStreams
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources

/**
 * Test for CLI help functionality for individual rule IDs.
 * @see CLI
 */
class CLIHelpTest {

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `help with valid rule ID shows rule documentation`() {
		val result = captureSystemStreams {
			try {
				main("--no-exit", "--help", "MissingJobTimeout")
			} catch (_: Exception) {
				// Expected for CLI tools
			}
		}

		result.stdout should contain("MissingJobTimeout")
		result.stdout should contain("Job is missing a timeout")
		result.stdout should contain("Description")
		result.stdout should contain("Compliant examples")
		result.stdout should contain("Non-compliant examples")
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `help with invalid rule ID shows error`() {
		val result = captureSystemStreams {
			try {
				main("--no-exit", "--help", "NonExistentRule")
			} catch (_: Exception) {
				// Expected for CLI tools
			}
		}

		result.stderr should contain("Unknown rule ID: NonExistentRule")
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `help without arguments shows general help`() {
		val result = captureSystemStreams {
			try {
				main("--no-exit", "--help")
			} catch (_: Exception) {
				// Expected for CLI tools
			}
		}

		result.stdout should contain("GitHub Actions Linter")
		result.stdout should contain("Usage:")
		result.stdout should contain("Options:")
	}

	@Test
	@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
	@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
	fun `help with option as next argument shows general help`() {
		val result = captureSystemStreams {
			try {
				main("--no-exit", "--help", "--verbose")
			} catch (_: Exception) {
				// Expected for CLI tools
			}
		}

		result.stdout should contain("GitHub Actions Linter")
		result.stdout should contain("Usage:")
		result.stdout should contain("Options:")
	}
}
