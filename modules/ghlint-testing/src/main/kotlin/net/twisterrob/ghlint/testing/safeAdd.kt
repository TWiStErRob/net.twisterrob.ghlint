package net.twisterrob.ghlint.testing

import io.kotest.assertions.print.Printed
import io.kotest.assertions.print.Printers

/** Serializes writes to Kotest's mutable printer registry during parallel test initialization. */
internal inline fun <reified T : Any> Printers.safeAdd(crossinline render: (T) -> String) {
	synchronized(this) {
		add(T::class) { Printed(render(it)) }
	}
}
