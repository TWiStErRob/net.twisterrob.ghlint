package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.analysis.Analyzer
import net.twisterrob.ghlint.model.SnakeWorkflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import org.intellij.lang.annotations.Language

public inline fun <reified T : Rule> check(
	@Language("yaml") yml: String,
	fileName: String = "test.yml",
): List<Finding> {
	val ruleSet = ReflectiveRuleSet("test-ruleset", T::class)
	require(yml.isNotEmpty()) { "At least one workflow.yml file must be provided." }
	val workflow = SnakeWorkflow.from(yml, fileName)
	return Analyzer().analyzeWorkflows(listOf(workflow), ruleSet)
}
