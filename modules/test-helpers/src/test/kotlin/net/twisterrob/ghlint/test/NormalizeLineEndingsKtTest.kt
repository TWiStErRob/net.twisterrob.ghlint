package net.twisterrob.ghlint.test

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * @see normalizeLineEndings
 */
class NormalizeLineEndingsKtTest {

	@Nested
	inner class `no line endings` {

		@Test fun `empty string is unchanged`() {
			"".normalizeLineEndings() shouldBe ""
		}

		@Test fun `single line without ending is unchanged`() {
			"no newlines here".normalizeLineEndings() shouldBe "no newlines here"
		}
	}

	@Nested
	inner class `LF input` {

		@Test fun `LF is kept as LF`() {
			"line1\nline2".normalizeLineEndings() shouldBe "line1\nline2"
		}

		@Test fun `LF at end is kept`() {
			"line1\n".normalizeLineEndings() shouldBe "line1\n"
		}

		@Test fun `multiple LF are kept`() {
			"a\n\nb".normalizeLineEndings() shouldBe "a\n\nb"
		}
	}

	@Nested
	inner class `CRLF input` {

		@Test fun `CRLF is normalized to LF`() {
			"line1\r\nline2".normalizeLineEndings() shouldBe "line1\nline2"
		}

		@Test fun `CRLF at end is normalized`() {
			"line1\r\n".normalizeLineEndings() shouldBe "line1\n"
		}

		@Test fun `multiple CRLF are normalized`() {
			"a\r\n\r\nb".normalizeLineEndings() shouldBe "a\n\nb"
		}
	}

	@Nested
	inner class `CR input` {

		@Test fun `CR is normalized to LF`() {
			"line1\rline2".normalizeLineEndings() shouldBe "line1\nline2"
		}

		@Test fun `CR at end is normalized`() {
			"line1\r".normalizeLineEndings() shouldBe "line1\n"
		}
	}

	@Nested
	inner class `mixed input` {

		@Test fun `mixed CRLF and LF are normalized`() {
			"a\r\nb\nc".normalizeLineEndings() shouldBe "a\nb\nc"
		}

		@Test fun `mixed CRLF and CF are normalized`() {
			"a\r\nb\rc".normalizeLineEndings() shouldBe "a\nb\nc"
		}

		@Test fun `mixed CR and LF are normalized`() {
			"a\rb\nc".normalizeLineEndings() shouldBe "a\nb\nc"
		}

		@Test fun `all three styles are normalized`() {
			"a\r\nb\rc\nd".normalizeLineEndings() shouldBe "a\nb\nc\nd"
		}
	}
}
