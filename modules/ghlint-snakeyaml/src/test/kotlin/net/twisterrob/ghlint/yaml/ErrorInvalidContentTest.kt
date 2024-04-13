package net.twisterrob.ghlint.yaml

import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import net.twisterrob.ghlint.testing.aLocation
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mockito.mock

class ErrorInvalidContentTest {

	@Test fun `empty file`() {
		val parentFile: File = mock(Answers.RETURNS_MOCKS)
		val rawContents = ""
		val error = Throwable("fake error")

		val subject = ErrorInvalidContent(parentFile, rawContents, error)

		subject.raw shouldBe rawContents
		subject.error shouldBe error
		subject.parent shouldBe parentFile
		subject.location shouldBe aLocation(
			Location(
				file = parentFile.location,
				// TODO these look strange, does it work in Sarif?
				start = Position(LineNumber(1), ColumnNumber(1)),
				end = Position(LineNumber(1), ColumnNumber(0)),
			)
		)
	}

	@Test fun `single line file`() {
		val parentFile: File = mock(Answers.RETURNS_MOCKS)
		val rawContents = """
			something that's not valid yaml
		""".trimIndent()
		val error = Throwable("fake error")

		val subject = ErrorInvalidContent(parentFile, rawContents, error)

		subject.raw shouldBe rawContents
		subject.error shouldBe error
		subject.parent shouldBe parentFile
		subject.location shouldBe aLocation(
			Location(
				file = parentFile.location,
				start = Position(LineNumber(1), ColumnNumber(1)),
				end = Position(LineNumber(1), ColumnNumber(31)),
			)
		)
	}

	@Test fun `multiline invalid file`() {
		val parentFile: File = mock(Answers.RETURNS_MOCKS)
		val rawContents = """
			something that's
			not valid yaml
			somewhat long
		""".trimIndent()
		val error = Throwable("fake error")

		val subject = ErrorInvalidContent(parentFile, rawContents, error)

		subject.raw shouldBe rawContents
		subject.error shouldBe error
		subject.parent shouldBe parentFile
		subject.location shouldBe aLocation(
			Location(
				file = parentFile.location,
				start = Position(LineNumber(1), ColumnNumber(1)),
				end = Position(LineNumber(3), ColumnNumber(13)),
			)
		)
	}
}
