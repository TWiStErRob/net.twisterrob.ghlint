package net.twisterrob.ghlint.testing

import io.kotest.assertions.print.Print
import io.kotest.assertions.print.Printed
import io.kotest.assertions.print.Printers
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import net.twisterrob.ghlint.results.Location

@Suppress("unused") // Initialize static framework when these assertions are used.
private val init = run {
	Printers.add(Location::class, object : Print<Location> {
		override fun print(a: Location, level: Int): Printed = Printed(a.testString())

		@Suppress("OVERRIDE_DEPRECATION")
		override fun print(a: Location): Printed = error("Unused")
	})
}

public fun aLocation(expected: Location): Matcher<Location> =
	object : Matcher<Location> {
		override fun test(value: Location): MatcherResult = MatcherResult(
			value.file.path == expected.file.path &&
					value.start.line == expected.start.line &&
					value.start.column == expected.start.column &&
					value.end.line == expected.end.line &&
					value.end.column == expected.end.column,
			{ "Location ${value.testString()} should be ${expected.testString()}" },
			{ "Location ${value.testString()} should not be ${expected.testString()}" },
		)
	}

private fun Location.testString(): String =
	"${file.path}@${start.line.number}:${start.column.number}-${end.line.number}:${end.column.number}"
