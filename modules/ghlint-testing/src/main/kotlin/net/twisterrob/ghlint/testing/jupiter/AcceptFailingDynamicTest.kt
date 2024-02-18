package net.twisterrob.ghlint.testing.jupiter

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Aborts a dynamic test method that fails.
 *
 * This is useful if an external framework creates dynamic tests on your behalf,
 * but you want to disable some of them that fail.
 *
 * If the test does not fail, the annotation can be removed.
 *
 * Example usage:
 * ```
 * @AcceptFailingDynamicTest(
 * 	displayName = "Some test name deep inside a dynamic container",
 * 	reason = "To demonstrate how the failing test can be accepted.",
 * 	acceptableFailure = "^\\Q"
 * 			+ "Example\n"
 * 			+ "failure"
 * 			+ "\\E$"
 * )
 * @Suppress("detekt.StringShouldBeRawString") // Cannot trimIndent on annotation parameters.
 * @TestFactory fun tests() = listOf(
 *   dynamicTest("...") { ... },
 *   dynamicContainer("", listOf(
 *     dynamicTest("...") { ... },
 *     dynamicTest("Some test name deep inside a dynamic container") { fail("Example\nfailure") },
 *     dynamicTest("...") { ... },
 *   ))
 * )
 * ```
 */
@Repeatable
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@ExtendWith(AcceptFailingDynamicTestExtension::class)
public annotation class AcceptFailingDynamicTest(
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
	 *  * use `\Q...\E` to match complex bits of the message without having to worry about escaping.
	 */
	@Language("RegExp")
	val acceptableFailure: String,
)
