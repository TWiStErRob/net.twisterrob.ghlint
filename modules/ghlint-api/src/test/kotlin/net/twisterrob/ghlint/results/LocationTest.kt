package net.twisterrob.ghlint.results

import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.model.FileLocation
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class LocationTest {

	@CsvSource(
		"1, 2, 3, 4, different line; different columns",
		"1, 1, 1, 1, empty file",
		"4, 1, 4, 1, empty line",
		"1, 6, 1, 6, first line; empty location",
		"4, 3, 4, 3, other line; empty location",
		"1, 2, 1, 3, first line; different columns (1 char)",
		"1, 2, 1, 6, first line; different columns (wider)",
		"8, 2, 8, 3, other line; different columns (1 char)",
		"8, 2, 8, 6, other line; different columns (wider)",
		"5, 1, 6, 1, different line; same character",
		"3, 1, 7, 1, different line (wider); same character",
		"5, 4, 6, 5, different line; increasing character",
		"3, 4, 7, 5, different line (wider); increasing character",
		"5, 5, 6, 4, different line; decreasing character",
		"3, 5, 7, 4, different line (wider); decreasing character",
	)
	@ParameterizedTest(name = "{4}: {0}:{1}-{2}:{3}")
	@Suppress("detekt.UnusedParameter", "UNUSED_PARAMETER") // testCase is used in the name of parameterized test ({4}).
	fun `valid locations`(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, testCase: String) {
		val subject = Location(
			file = FileLocation("test.file"),
			start = Position(LineNumber(startLine), ColumnNumber(startColumn)),
			end = Position(LineNumber(endLine), ColumnNumber(endColumn)),
		)

		subject.file shouldBe FileLocation("test.file")
		subject.start.line.number shouldBe startLine
		subject.start.column.number shouldBe startColumn
		subject.end.line.number shouldBe endLine
		subject.end.column.number shouldBe endColumn
	}

	@CsvSource(
		"3, 3, 2, 3, same character",
		"6, 3, 4, 7, increasing character",
		"6, 7, 4, 3, decreasing character",
	)
	@ParameterizedTest
	@Suppress("detekt.UnusedParameter", "UNUSED_PARAMETER") // testCase is used in the name of parameterized test ({4}).
	fun `invalid locations - lines`(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, testCase: String) {
		shouldThrowMessage(
			"Start line must be before or equal to end line: ${startLine} <= ${endLine}"
		) {
			Location(
				file = FileLocation("test.file"),
				start = Position(LineNumber(startLine), ColumnNumber(startColumn)),
				end = Position(LineNumber(endLine), ColumnNumber(endColumn)),
			)
		}
	}

	@CsvSource(
		"1, 42, 1, 3, first line; swapped column",
		"5, 42, 5, 3, other line; swapped column",
		"1, 2, 1, 1, first line; swapped column at 1",
		"7, 2, 7, 1, other line; swapped column at 1",
	)
	@ParameterizedTest
	@Suppress("detekt.UnusedParameter", "UNUSED_PARAMETER") // testCase is used in the name of parameterized test ({4}).
	fun `invalid locations - columns`(
		startLine: Int,
		startColumn: Int,
		endLine: Int,
		endColumn: Int,
		testCase: String
	) {
		shouldThrowMessage(
			"Start column must be before or equal to end column on the same line (${startLine}): ${startColumn} <= ${endColumn}"
		) {
			Location(
				file = FileLocation("test.file"),
				start = Position(LineNumber(startLine), ColumnNumber(startColumn)),
				end = Position(LineNumber(endLine), ColumnNumber(endColumn)),
			)
		}
	}
}
