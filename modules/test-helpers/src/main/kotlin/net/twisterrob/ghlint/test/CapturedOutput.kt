@file:OptIn(ExperimentalContracts::class)

package net.twisterrob.ghlint.test

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

public class CapturedOutput<T>(
	public val result: T,
	public val stdout: String,
	public val stderr: String,
)

/**
 * Captures the output of [System.out]/[System.err] during the execution of [block].
 *
 * In parallel test execution, lock the [System.setOut]/[System.setErr] usages, e.g. Jupiter:
 * ```
 * @ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
 * @ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
 * ```
 *
 * @sample net.twisterrob.ghlint.test.CapturedOutputTest.Output.exampleUsage
 */
public inline fun <R> captureSystemStreams(block: () -> R): CapturedOutput<R> {
	contract {
		callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
	}
	val result = captureSystemOut { captureSystemErr { block() } }
	return CapturedOutput(
		result = result.result.result,
		stdout = result.stdout,
		stderr = result.result.stderr,
	)
}

/**
 * Captures the output of [System.out] during the execution of [block].
 *
 * In parallel test execution, lock the [System.setOut] usages, e.g. Jupiter:
 * ```
 * @ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
 * ```
 *
 * @sample net.twisterrob.ghlint.test.CapturedOutputTest.Output.exampleUsage
 */
public inline fun <R> captureSystemOut(block: () -> R): CapturedOutput<R> {
	contract {
		callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
	}
	val out = System.out
	val buffer = ByteArrayOutputStream()
	System.setOut(PrintStream(buffer, true))
	val result = try {
		block()
	} finally {
		System.setOut(out)
	}
	return CapturedOutput(
		result = result,
		stdout = buffer.toString(Charsets.UTF_8),
		stderr = "",
	)
}

/**
 * Captures the output of [System.out] during the execution of [block], bug does not capture the [block]'s result.
 *
 * @see captureSystemOut
 */
public inline fun captureSystemOutOnly(block: () -> Unit): String = captureSystemOut { block() }.stdout

/**
 * Captures the output of [System.err] during the execution of [block].
 *
 * In parallel test execution, lock the [System.setErr] usages, e.g. Jupiter:
 * ```
 * @ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
 * ```
 *
 * @sample net.twisterrob.ghlint.test.CapturedOutputTest.Error.exampleUsage
 */
public inline fun <R> captureSystemErr(block: () -> R): CapturedOutput<R> {
	contract {
		callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
	}
	val err = System.err
	val buffer = ByteArrayOutputStream()
	System.setErr(PrintStream(buffer, true))
	val result = try {
		block()
	} finally {
		System.setErr(err)
	}
	return CapturedOutput(
		result = result,
		stdout = "",
		stderr = buffer.toString(Charsets.UTF_8),
	)
}

/**
 * Captures the output of [System.err] during the execution of [block], bug does not capture the [block]'s result.
 *
 * @see captureSystemErr
 */
public inline fun captureSystemErrOnly(block: () -> Unit): String = captureSystemErr { block() }.stderr
