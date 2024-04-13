package net.twisterrob.ghlint.testing

import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("detekt.ClassNaming")
class Matchers_LocationKtTest {

	@Nested
	inner class `beLocation Test` {

		@Nested
		inner class `working as intended` {

			@Test fun `matches self`() {
				val location = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)
				location shouldBe aLocation(location)
			}

			@Test fun `full mismatch is symmetric`() {
				val location1 = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)
				val location2 = Location(
					file = FileLocation("test2.file"),
					start = Position(LineNumber(5), ColumnNumber(6)),
					end = Position(LineNumber(7), ColumnNumber(8)),
				)
				location1 shouldNotBe aLocation(location2)
				location2 shouldNotBe aLocation(location1)
			}
		}

		@Nested
		inner class `fails correctly` {

			@Test fun `mismatch on file name`() {
				val location1 = Location(
					file = FileLocation("test1.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)
				val location2 = Location(
					file = FileLocation("test2.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)

				shouldThrowMessage("Location test1.file@1:2-3:4 should be test2.file@1:2-3:4") {
					location1 shouldBe aLocation(location2)
				}
			}

			@Test fun `mismatch on start line`() {
				val location1 = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)
				val location2 = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(5), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)

				shouldThrowMessage("Location test.file@1:2-3:4 should be test.file@5:2-3:4") {
					location1 shouldBe aLocation(location2)
				}
			}

			@Test fun `mismatch on start column`() {
				val location1 = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)
				val location2 = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(1), ColumnNumber(5)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)

				shouldThrowMessage("Location test.file@1:2-3:4 should be test.file@1:5-3:4") {
					location1 shouldBe aLocation(location2)
				}
			}

			@Test fun `mismatch on end line`() {
				val location1 = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)
				val location2 = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(5), ColumnNumber(4)),
				)

				shouldThrowMessage("Location test.file@1:2-3:4 should be test.file@1:2-5:4") {
					location1 shouldBe aLocation(location2)
				}
			}

			@Test fun `mismatch on end column`() {
				val location1 = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(4)),
				)
				val location2 = Location(
					file = FileLocation("test.file"),
					start = Position(LineNumber(1), ColumnNumber(2)),
					end = Position(LineNumber(3), ColumnNumber(5)),
				)

				shouldThrowMessage("Location test.file@1:2-3:4 should be test.file@1:2-3:5") {
					location1 shouldBe aLocation(location2)
				}
			}
		}
	}
}
