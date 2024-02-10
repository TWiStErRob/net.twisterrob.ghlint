package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet
import net.twisterrob.ghlint.yaml.Yaml
import org.intellij.lang.annotations.Language

public inline fun <reified T : Rule> check(
	@Language("yaml") yml: String,
	fileName: String = "test.yml",
): List<Finding> {
	val ruleSet = ReflectiveRuleSet("test-ruleset", T::class)
	require(yml.isNotEmpty()) { "At least one workflow.yml file must be provided." }
	return Yaml.analyze(listOf(File(FileLocation(fileName), yml)), listOf(ruleSet))
}
