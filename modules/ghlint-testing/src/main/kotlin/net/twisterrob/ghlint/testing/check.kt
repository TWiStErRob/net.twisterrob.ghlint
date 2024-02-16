package net.twisterrob.ghlint.testing

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeIn
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.yaml.Yaml
import org.intellij.lang.annotations.Language

public var debug: Boolean = System.getProperty("ghlint.debug", "false").toBooleanStrict()

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
	if (debug) println("${this} > ${fileName}:\n${yml}")
	require(yml.isNotEmpty()) { "A non-empty workflow.yml file must be provided." }
	val findings = this.check(Yaml.loadWorkflow(File(FileLocation(fileName), yml)))
	if (debug) findings.forEach { println(it.testString()) }
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
