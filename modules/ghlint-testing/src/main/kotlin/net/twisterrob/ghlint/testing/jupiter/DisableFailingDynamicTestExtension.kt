package net.twisterrob.ghlint.testing.jupiter

import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Assertions.fail
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
		val method = generateSequence(extensionContext) { it.parent.getOrNull() }
			.map { it.element.getOrNull() }
			.first { it != null }
			?: error("No method context found for ${extensionContext.displayName}")
		val disableds = method.getAnnotationsByType(DisableFailingDynamicTest::class.java)
		val disabled = disableds.singleOrNull { it.displayName == extensionContext.displayName }

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
			e shouldHaveMessage Regex(disabled.acceptableFailure)
		}
	}
}
