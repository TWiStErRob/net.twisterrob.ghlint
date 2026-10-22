package net.twisterrob.ghlint.testing

import io.kotest.assertions.print.Print
import io.kotest.assertions.print.Printers
import kotlin.reflect.KClass

/** Serializes writes to Kotest's mutable printer registry during parallel test initialization. */
internal fun <T : Any> Printers.safeAdd(kclass: KClass<out T>, print: Print<T>) {
	synchronized(this) {
		add(kclass, print)
	}
}
