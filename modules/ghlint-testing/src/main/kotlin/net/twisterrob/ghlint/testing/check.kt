@file:Suppress(
	// Cannot extract private constant, because of inline,
	// and don't want to pollute namespace with public constant.
	"detekt.StringLiteralDuplication",
)

package net.twisterrob.ghlint.testing

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeIn
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Rule

/**
 * Enables debug logging for testing functions.
 *
 * @see check
 * @see checkUnsafe
 */
public var isDebugEnabled: Boolean = System.getProperty("ghlint.debug", "false").toBooleanStrict()

public inline fun <reified T : Rule> check(file: RawFile): List<Finding> {
	val rule = createRule<T>()
	return rule.check(file)
}

public inline fun <reified T : Rule> checkUnsafe(file: RawFile): List<Finding> {
	val rule = createRule<T>()
	return rule.checkUnsafe(file)
}

/**
 * Checks the given [file] through the [Rule] and returns the [Finding]s.
 * Additional validation is performed to ensure correct syntax and internal consistency.
 *
 * Debug logging can be enabled via [isDebugEnabled] top level property in the same package.
 *
 * WARNING: This method is not recommended to be used directly,
 * use [check]`<Rule>()` or [checkUnsafe]`<Rule>()` wherever possible.
 *
 * @see check
 * @see checkUnsafe
 */
public fun Rule.check(file: RawFile): List<Finding> = check(file, validate = true)

/**
 * Checks the given [file] through the [Rule] and returns the [Finding]s.
 * It's unsafe because it performs no validation to ensure correct syntax and internal consistency.
 *
 * Debug logging can be enabled via [isDebugEnabled] top level property in the same package.
 *
 * WARNING: This method is not recommended to be used directly,
 * use [check]`<Rule>()` or [checkUnsafe]`<Rule>()` wherever possible.
 *
 * @see check
 * @see checkUnsafe
 */
public fun Rule.checkUnsafe(file: RawFile): List<Finding> = check(file, validate = false)

private fun Rule.check(file: RawFile, validate: Boolean): List<Finding> {
	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	if (isDebugEnabled) println("${this} > ${file.location.path}:\n${file.content}")
	val loadedFile = if (validate) load(file) else loadUnsafe(file)
	val findings = this.check(loadedFile)
	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	if (isDebugEnabled) findings.forEach { println(it.testString()) }
	assertFindingsProducibleByRule(findings, this)
	return findings
}

public fun assertFindingsProducibleByRule(findings: List<Finding>, rule: Rule) {
	findings.forEach { finding ->
		withClue("Rule declares issue to be produced by it.") {
			withClue(finding.testString()) {
				finding.issue shouldBeIn rule.issues
			}
		}
	}
}
