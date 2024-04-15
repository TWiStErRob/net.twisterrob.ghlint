package net.twisterrob.ghlint.testing

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldHave
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.yaml.SnakeYaml
import org.intellij.lang.annotations.Language

public var isDebugEnabled: Boolean = System.getProperty("ghlint.debug", "false").toBooleanStrict()

public inline fun <reified T : Rule> check(
	@Language("yaml") yml: String,
	fileName: String = "test.yml",
): List<Finding> {
	val rule = createRule<T>()
	return rule.check(yml, fileName)
}

public fun Rule.check(
	@Language("yaml") yml: String,
	fileName: String = "test.yml",
): List<Finding> {
	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	if (isDebugEnabled) println("${this} > ${fileName}:\n${yml}")
	val validation = validate(yml, fileName)
	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	if (isDebugEnabled) validation.forEach { println(it.testString()) }
	validation shouldHave noFindings()
	val findings = this.check(SnakeYaml.load(RawFile(FileLocation(fileName), yml)))
	@Suppress("detekt.ForbiddenMethodCall") // TODO logging.
	if (isDebugEnabled) findings.forEach { println(it.testString()) }
	assertFindingsProducibleByRule(findings, this)
	return findings
}

public fun assertFindingsProducibleByRule(findings: List<Finding>, rule: Rule) {
	findings.forEach { finding ->
		withClue(finding.testString()) {
			finding.issue shouldBeIn rule.issues
		}
	}
}
