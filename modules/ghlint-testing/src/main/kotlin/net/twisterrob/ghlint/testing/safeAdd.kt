package net.twisterrob.ghlint.testing

import io.kotest.assertions.print.Print
import io.kotest.assertions.print.Printers

/** Serializes writes to Kotest's mutable printer registry during parallel test initialization. */
internal inline fun <reified T : Any> Printers.safeAdd(print: Print<T>) {
	synchronized(this) {
		add(T::class, print)
	}
}
