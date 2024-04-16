package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import net.twisterrob.ghlint.testing.aLocation
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class FileTest {

	@Test fun `empty file`() {
		val subject = file("")

		subject.wholeFile shouldBe aLocation(
			Location(
				file = subject.location,
				start = Position(LineNumber(1), ColumnNumber(1)),
				end = Position(LineNumber(1), ColumnNumber(1)),
			)
		)
	}

	@Test fun `single line file`() {
		val subject = file(
			"""
				something that's not valid yaml
			""".trimIndent()
		)

		subject.wholeFile shouldBe aLocation(
			Location(
				file = subject.location,
				start = Position(LineNumber(1), ColumnNumber(1)),
				end = Position(LineNumber(1), ColumnNumber(32)),
			)
		)
	}

	@Test fun `multiline invalid file`() {
		val subject = file(
			"""
				something that's
				not valid yaml
				somewhat long
			""".trimIndent()
		)

		subject.wholeFile shouldBe aLocation(
			Location(
				file = subject.location,
				start = Position(LineNumber(1), ColumnNumber(1)),
				end = Position(LineNumber(3), ColumnNumber(14)),
			)
		)
	}

	private fun file(rawContents: String): File {
		val file: File = mock()
		val rawFile: RawFile = mock()
		whenever(file.location).thenReturn(FileLocation("test.yml"))
		whenever(file.origin).thenReturn(rawFile)
		whenever(rawFile.content).thenReturn(rawContents)
		return file
	}
}
