package net.twisterrob.ghlint.testing

import io.kotest.assertions.print.Printed
import io.kotest.assertions.print.Printers
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.rule.Issue

/** Registers custom printers through one JVM class initializer so parallel tests cannot race the writes. */
internal object MatcherPrinters {
	init {
		Printers.add(Finding::class) { Printed(it.testString()) }
		Printers.add(Issue::class) { Printed(it.testString()) }
		Printers.add(Location::class) { Printed(it.testString()) }
	}

	internal fun ensureInitialized(): Unit = Unit
}
