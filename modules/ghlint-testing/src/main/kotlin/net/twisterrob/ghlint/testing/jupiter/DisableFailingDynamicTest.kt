package net.twisterrob.ghlint.testing.jupiter

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Disables a dynamic test method that fails.
 *
 * This is useful if an external framework creates dynamic tests on your behalf,
 * but you want to disable some of them that fail.
 *
 * If the test does not fail, the annotation can be removed.
 */
@Repeatable
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@ExtendWith(DisableFailingDynamicTestExtension::class)
public annotation class DisableFailingDynamicTest(
	/**
	 * The display name of the dynamic test, does not include the full path, just the lowest [DynamicNode]'s name.
	 */
	val displayName: String,

	/**
	 * The reason why the test is disabled. For humans only.
	 */
	val reason: String,

	/**
	 * Regular expression to match the message of the failed test.
	 *
	 * The pattern is used to match the entire message, so apply the following tips:
	 *  * it should be anchored with `^` and `$` because of the entire message match.
	 *  * use `.*(...)$` to create an "ends with" match.
	 *  * use `^(...).*` to create a "starts with" match.
	 *  * use `(?idmsux-idmsux:...)` to enable flags for the pattern.
	 *    specifically: prepend `(?s)` to enable DOTALL mode to match multiline messages.
	 */
	@Language("RegExp")
	val acceptableFailure: String,
)