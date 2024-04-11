package net.twisterrob.ghlint.docs.issues

import net.twisterrob.ghlint.analysis.JsonSchemaRuleSet
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.name
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet

internal class BuiltInRuleSet : RuleSet {

	override val id: String = "builtins"
	override val name: String = "Built-ins"

	override fun createRules(): List<Rule> {
		val safeRule = load<Rule>("net.twisterrob.ghlint.analysis.SafeRule")
		val validRule = load<Rule>("net.twisterrob.ghlint.analysis.JsonSchemaValidationRule")
		@Suppress("SpreadOperator")
		return listOf(
			validRule.getDeclaredConstructor().newInstance(),
			safeRule.getDeclaredConstructor(Rule::class.java).newInstance(ProblematicRule()),
			*JsonSchemaRuleSet().createRules().toTypedArray()
		)
	}
}

private class ProblematicRule : Rule {

	override val issues: List<Issue> = emptyList()
	override fun check(file: File): List<Finding> =
		if (file.location.name == "Invalid") {
			error("Demonstrative failure.")
		} else {
			emptyList()
		}
}

@Suppress("UNCHECKED_CAST", "detekt.CastNullableToNonNullableType")
private fun <T> load(className: String): Class<T> =
	Class.forName(className) as Class<T>
