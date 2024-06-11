package net.twisterrob.ghlint.testing

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import org.junit.jupiter.api.Test

class LocationOfNthKtTest {

	@Test fun `finds the full input`() {
		val text = """
			text
		""".trimIndent()

		text.locationOfNth("text", 1) shouldBe aLocation(
			Location(
				FileLocation("test.yml"),
				Position(LineNumber(1), ColumnNumber(1)),
				Position(LineNumber(1), ColumnNumber(5)),
			)
		)
	}

	@Test fun `finds the first occurrence`() {
		val text = """
			text text text text
		""".trimIndent()

		text.locationOfNth("text", 1) shouldBe aLocation(
			Location(
				FileLocation("test.yml"),
				Position(LineNumber(1), ColumnNumber(1)),
				Position(LineNumber(1), ColumnNumber(5)),
			)
		)
	}

	@Test fun `finds the an occurrence`() {
		val text = """
			text text text text
		""".trimIndent()

		text.locationOfNth("text", 2) shouldBe aLocation(
			Location(
				FileLocation("test.yml"),
				Position(LineNumber(1), ColumnNumber(6)),
				Position(LineNumber(1), ColumnNumber(10)),
			)
		)
	}

	@Test fun `finds the last occurrence`() {
		val text = """
			text text text text
		""".trimIndent()

		text.locationOfNth("text", 4) shouldBe aLocation(
			Location(
				FileLocation("test.yml"),
				Position(LineNumber(1), ColumnNumber(16)),
				Position(LineNumber(1), ColumnNumber(20)),
			)
		)
	}

	@Test fun `finds the first full line`() {
		val text = """
			text
			text
			text
			text
		""".trimIndent()

		text.locationOfNth("text", 1) shouldBe aLocation(
			Location(
				FileLocation("test.yml"),
				Position(LineNumber(1), ColumnNumber(1)),
				Position(LineNumber(1), ColumnNumber(5)),
			)
		)
	}

	@Test fun `finds a full line`() {
		val text = """
			text
			text
			text
			text
		""".trimIndent()

		text.locationOfNth("text", 3) shouldBe aLocation(
			Location(
				FileLocation("test.yml"),
				Position(LineNumber(3), ColumnNumber(1)),
				Position(LineNumber(3), ColumnNumber(5)),
			)
		)
	}

	@Test fun `finds the last full line`() {
		val text = """
			text
			text
			text
			text
		""".trimIndent()

		text.locationOfNth("text", 4) shouldBe aLocation(
			Location(
				FileLocation("test.yml"),
				Position(LineNumber(4), ColumnNumber(1)),
				Position(LineNumber(4), ColumnNumber(5)),
			)
		)
	}

	@Test fun `finds the 4th occurrence on a separate line`() {
		val text = """
			text text
			text text
		""".trimIndent()

		text.locationOfNth("text", 4) shouldBe aLocation(
			Location(
				FileLocation("test.yml"),
				Position(LineNumber(2), ColumnNumber(6)),
				Position(LineNumber(2), ColumnNumber(10)),
			)
		)
	}

	@Test fun `finds the 3rd distinct line`() {
		val text = """
			line1
			line2
			line3
			line4
		""".trimIndent()

		text.locationOfNth("line3", 1) shouldBe aLocation(
			Location(
				FileLocation("test.yml"),
				Position(LineNumber(3), ColumnNumber(1)),
				Position(LineNumber(3), ColumnNumber(6)),
			)
		)
	}

	@Test fun `errors on 0th occurrence`() {
		val text = """
			text
		""".trimIndent()

		val ex = shouldThrow<IllegalArgumentException> { text.locationOfNth("text", 0) }
		ex.message shouldNotContain "text"
		ex.message shouldContain "0"
	}

	@Test fun `errors on negative occurrence`() {
		val text = """
			text
		""".trimIndent()

		val ex = shouldThrow<IllegalArgumentException> { text.locationOfNth("text", -5) }
		ex.message shouldNotContain "text"
		ex.message shouldContain "-5"
	}

	@Test fun `errors on too many occurrences`() {
		val text = """
			text text
			text text
		""".trimIndent()

		val ex = shouldThrow<IllegalArgumentException> { text.locationOfNth("text", 5) }
		ex.message shouldContain "text"
		ex.message shouldContain "5"
	}
}
