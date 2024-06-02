package net.twisterrob.ghlint.test

import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Captures the output of [System.out] during the execution of [block].
 *
 * In parallel test execution, lock the [System.setOut] usages, e.g. Jupiter:
 * ```
 * @ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
 * ```
 *
 * @sample net.twisterrob.ghlint.test.CaptureSystemOutKtTest.exampleUsage
 */
public inline fun captureSystemOut(block: () -> Unit): String {
	val out = System.out
	val buffer = ByteArrayOutputStream()
	System.setOut(PrintStream(buffer, true))
	try {
		block()
	} finally {
		System.setOut(out)
	}
	return buffer.toString(Charsets.UTF_8)
}
