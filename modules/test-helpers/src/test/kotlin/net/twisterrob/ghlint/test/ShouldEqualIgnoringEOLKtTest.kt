package net.twisterrob.ghlint.test

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * @see shouldEqualIgnoringEOL
 */
class ShouldEqualIgnoringEOLKtTest {

	@Nested
	inner class Success {

		@Test fun `empty strings are equal`() {
			"" shouldEqualIgnoringEOL ""
		}

		@Test fun `strings without line endings are equal`() {
			"no newlines" shouldEqualIgnoringEOL "no newlines"
		}

		@Test fun `same line endings equal`() {
			"line1\nline2" shouldEqualIgnoringEOL "line1\nline2"
			"line1\rline2" shouldEqualIgnoringEOL "line1\rline2"
			"line1\r\nline2" shouldEqualIgnoringEOL "line1\r\nline2"
		}

		@Test fun `LF and CRLF are equal`() {
			"line1\r\nline2" shouldEqualIgnoringEOL "line1\nline2"
			"line1\nline2" shouldEqualIgnoringEOL "line1\r\nline2"
		}

		@Test fun `LF and CR are equal`() {
			"line1\rline2" shouldEqualIgnoringEOL "line1\nline2"
			"line1\nline2" shouldEqualIgnoringEOL "line1\rline2"
		}

		@Test fun `CR and CRLF are equal`() {
			"line1\r\nline2" shouldEqualIgnoringEOL "line1\rline2"
			"line1\rline2" shouldEqualIgnoringEOL "line1\r\nline2"
		}
	}

	@Nested
	inner class Failure {

		@Test fun `different content fails`() {
			val failure = shouldThrow<AssertionError> {
				"hello" shouldEqualIgnoringEOL "world"
			}

			failure shouldHaveMessage "expected:<world> but was:<hello>"
		}

		@Test fun `null receiver fails`() {
			val failure = shouldThrow<AssertionError> {
				null shouldEqualIgnoringEOL "expected"
			}

			failure shouldHaveMessage "Expected \"expected\" but actual was null"
		}

		@Test fun `extra leading line ending fails`() {
			val failure = shouldThrow<AssertionError> {
				"\nline1\nline2" shouldEqualIgnoringEOL "line1\nline2"
			}

			failure shouldHaveMessage
					"(contents match, but line-breaks differ; output has been escaped to show line-breaks)\n" +
					"expected:<line1\\nline2> but was:<\\nline1\\nline2>"
		}

		@Test fun `extra trailing line ending fails`() {
			val failure = shouldThrow<AssertionError> {
				"line1\nline2\n" shouldEqualIgnoringEOL "line1\nline2"
			}

			failure shouldHaveMessage
					"(contents match, but line-breaks differ; output has been escaped to show line-breaks)\n" +
					"expected:<line1\\nline2> but was:<line1\\nline2\\n>"
		}
	}
}
