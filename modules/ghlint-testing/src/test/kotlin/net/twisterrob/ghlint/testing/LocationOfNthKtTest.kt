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

		val result = text.locationOfNth(LOCATION, "text", 1)

		result shouldBe aLocation(
			Location(
				LOCATION,
				Position(LineNumber(1), ColumnNumber(1)),
				Position(LineNumber(1), ColumnNumber(5)),
			)
		)
	}

	@Test fun `finds the first occurrence`() {
		val text = """
			text text text text
		""".trimIndent()

		val result = text.locationOfNth(LOCATION, "text", 1)

		result shouldBe aLocation(
			Location(
				LOCATION,
				Position(LineNumber(1), ColumnNumber(1)),
				Position(LineNumber(1), ColumnNumber(5)),
			)
		)
	}

	@Test fun `finds the an occurrence`() {
		val text = """
			text text text text
		""".trimIndent()

		val result = text.locationOfNth(LOCATION, "text", 2)

		result shouldBe aLocation(
			Location(
				LOCATION,
				Position(LineNumber(1), ColumnNumber(6)),
				Position(LineNumber(1), ColumnNumber(10)),
			)
		)
	}

	@Test fun `finds the last occurrence`() {
		val text = """
			text text text text
		""".trimIndent()

		val result = text.locationOfNth(LOCATION, "text", 4)

		result shouldBe aLocation(
			Location(
				LOCATION,
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

		val result = text.locationOfNth(LOCATION, "text", 1)

		result shouldBe aLocation(
			Location(
				LOCATION,
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

		val result = text.locationOfNth(LOCATION, "text", 3)

		result shouldBe aLocation(
			Location(
				LOCATION,
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

		val result = text.locationOfNth(LOCATION, "text", 4)

		result shouldBe aLocation(
			Location(
				LOCATION,
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

		val result = text.locationOfNth(LOCATION, "text", 4)

		result shouldBe aLocation(
			Location(
				LOCATION,
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

		val result = text.locationOfNth(LOCATION, "line3", 1)

		result shouldBe aLocation(
			Location(
				LOCATION,
				Position(LineNumber(3), ColumnNumber(1)),
				Position(LineNumber(3), ColumnNumber(6)),
			)
		)
	}

	@Test fun `errors on 0th occurrence`() {
		val text = """
			text
		""".trimIndent()

		val result = shouldThrow<IllegalArgumentException> {
			text.locationOfNth(LOCATION, "text", 0)
		}

		result.message shouldNotContain "text"
		result.message shouldContain "0"
	}

	@Test fun `errors on negative occurrence`() {
		val text = """
			text
		""".trimIndent()

		val result = shouldThrow<IllegalArgumentException> {
			text.locationOfNth(LOCATION, "text", -5)
		}

		result.message shouldNotContain "text"
		result.message shouldContain "-5"
	}

	@Test fun `errors on too many occurrences`() {
		val text = """
			text text
			text text
		""".trimIndent()

		val result = shouldThrow<IllegalArgumentException> {
			text.locationOfNth(LOCATION, "text", 5)
		}

		result.message shouldContain "text"
		result.message shouldContain "5"
	}

	companion object {
		private val LOCATION = FileLocation("test.yml")
	}
}
