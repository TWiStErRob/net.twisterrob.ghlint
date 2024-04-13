package net.twisterrob.ghlint.docs.issues

import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet

internal class DocumentationRuleSet : RuleSet {

	override val id: String = "builtins"
	override val name: String = "Built-ins"

	override fun createRules(): List<Rule> {
		val safeRule = load<Rule>("net.twisterrob.ghlint.analysis.SafeRule")
		val validRule = load<Rule>("net.twisterrob.ghlint.analysis.JsonSchemaValidationRule")
		return listOf(
			validRule.getDeclaredConstructor().newInstance(),
			safeRule.getDeclaredConstructor(Rule::class.java).newInstance(ProblematicRule()),
			Validator()
		)
	}
}

private class Validator : Rule {

	override val issues: List<Issue>
		get() {
			val validator = load<Issue>("net.twisterrob.ghlint.analysis.Validator")

			@Suppress("detekt.CastNullableToNonNullableType")
			val syntaxError = validator.getDeclaredField("SyntaxError")
				.apply { isAccessible = true }
				.get(null) as Issue
			return listOf(syntaxError)
		}

	override fun check(workflow: Workflow): List<Finding> = emptyList()
}

private class ProblematicRule : Rule {

	override val issues: List<Issue> = emptyList()
	override fun check(workflow: Workflow): List<Finding> =
		if (workflow.name == "Invalid") {
			error("Demonstrative failure.")
		} else {
			emptyList()
		}
}

@Suppress("UNCHECKED_CAST", "detekt.CastNullableToNonNullableType")
private fun <T> load(className: String): Class<T> =
	Class.forName(className) as Class<T>
