package net.twisterrob.ghlint.testing.jupiter

import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.extension.DynamicTestInvocationContext
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import java.lang.reflect.AnnotatedElement
import kotlin.jvm.optionals.getOrNull

internal class DisableFailingDynamicTestExtension : InvocationInterceptor {

	override fun interceptDynamicTest(
		@Suppress("ForbiddenVoid") // REPORT false positive: overridden method cannot be Unit.
		invocation: InvocationInterceptor.Invocation<Void>,
		invocationContext: DynamicTestInvocationContext,
		extensionContext: ExtensionContext
	) {
		val method = extensionContext.firstElement
			?: error("No method context found for ${extensionContext.displayName}")
		val disabled = method.findDisableAnnotationFor(extensionContext.displayName)

		if (disabled != null) {
			invocation.invokeDisabled(disabled, method)
		} else {
			super.interceptDynamicTest(invocation, invocationContext, extensionContext)
		}
	}

	private fun InvocationInterceptor.Invocation<*>.invokeDisabled(
		disabled: DisableFailingDynamicTest,
		method: AnnotatedElement
	) {
		try {
			this.proceed()
			fail(
				"""
					"${disabled.displayName}" should have failed.
					You can remove @DisableDynamicTest annotation from ${method}.
				""".trimIndent()
			)
		} catch (e: AssertionError) {
			if (e.message.orEmpty().matches(Regex(disabled.acceptableFailure))) {
				Assumptions.abort("Test has been disabled: ${disabled.reason}.")
			} else {
				throw AssertionError(
					"""
						"${disabled.displayName}" failed with unexpected message.
						Expected: ${disabled.acceptableFailure}
						Actual: ${e.message}
					""".trimIndent()
				).apply { initCause(e) }
			}
		}
	}
}

/**
 * Dynamic tests are nested inside each other and a test method.
 * This extension attempts to find the first element that is a test method.
 */
private val ExtensionContext.firstElement: AnnotatedElement?
	get() = generateSequence(this) { it.parent.getOrNull() }
		.map { it.element.getOrNull() }
		.firstOrNull { it != null }

private fun AnnotatedElement.findDisableAnnotationFor(displayName: String): DisableFailingDynamicTest? {
	val disabled: Array<DisableFailingDynamicTest> = this.getAnnotationsByType(DisableFailingDynamicTest::class.java)
	val matching = disabled.filter { it.displayName == displayName }
	return when (matching.size) {
		0 -> null
		1 -> matching.single()
		else -> error("Multiple @DisableFailingDynamicTest annotations for ${displayName} on ${this}")
	}
}
