package net.twisterrob.ghlint.yaml

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import net.twisterrob.ghlint.testing.aLocation
import org.junit.jupiter.api.Test

class ErrorInvalidContentTest {

	@Test fun `empty file`() {
		val rawFile = RawFile(
			FileLocation("unknown"),
			""
		)
		val error = Throwable("fake error")

		val subject = ErrorInvalidContent.create(rawFile, error)

		subject.raw shouldBe rawFile.content
		subject.error shouldBe error
		subject.parent.content shouldBe subject
		subject.parent.location shouldBe FileLocation("unknown")
		subject.location should aLocation(
			Location(
				file = FileLocation("unknown"),
				// TODO these look strange, does it work in Sarif?
				start = Position(LineNumber(1), ColumnNumber(1)),
				end = Position(LineNumber(1), ColumnNumber(0)),
			)
		)
	}

	@Test fun `single line file`() {
		val rawFile = RawFile(
			FileLocation("unknown"),
			"""
				something that's not valid yaml
			""".trimIndent()
		)
		val error = Throwable("fake error")

		val subject = ErrorInvalidContent.create(rawFile, error)

		subject.raw shouldBe rawFile.content
		subject.error shouldBe error
		subject.parent.content shouldBe subject
		subject.parent.location shouldBe FileLocation("unknown")
		subject.location should aLocation(
			Location(
				file = FileLocation("unknown"),
				start = Position(LineNumber(1), ColumnNumber(1)),
				end = Position(LineNumber(1), ColumnNumber(31)),
			)
		)
	}

	@Test fun `multiline invalid file`() {
		val rawFile = RawFile(
			FileLocation("unknown"),
			"""
				something that's
				not valid yaml
				somewhat long
			""".trimIndent()
		)
		val error = Throwable("fake error")

		val subject = ErrorInvalidContent.create(rawFile, error)

		subject.raw shouldBe rawFile.content
		subject.error shouldBe error
		subject.parent.content shouldBe subject
		subject.parent.location shouldBe FileLocation("unknown")
		subject.location should aLocation(
			Location(
				file = FileLocation("unknown"),
				start = Position(LineNumber(1), ColumnNumber(1)),
				end = Position(LineNumber(3), ColumnNumber(13)),
			)
		)
	}
}
