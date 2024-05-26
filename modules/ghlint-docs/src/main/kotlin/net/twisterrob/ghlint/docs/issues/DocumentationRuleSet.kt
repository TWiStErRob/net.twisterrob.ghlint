package net.twisterrob.ghlint.docs.issues

import net.twisterrob.ghlint.BuiltInRuleSet
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet

internal class DocumentationRuleSet(
	private val builtIn: BuiltInRuleSet = BuiltInRuleSet(),
) : RuleSet by builtIn {

	override fun createRules(): List<Rule> =
		@Suppress("detekt.SpreadOperator")
		listOf(
			*builtIn.createRules().toTypedArray(),
			load<Rule>("net.twisterrob.ghlint.analysis.SafeRule")
				.getDeclaredConstructor(Rule::class.java)
				.newInstance(ProblematicRule()),
		)
}

private class ProblematicRule : Rule {

	override val issues: List<Issue> = emptyList()
	override fun check(file: File): List<Finding> =
		if ((file.content as Workflow).name == "Invalid") {
			// Non-compliant example in SafeRule.
			error("Demonstrative failure.")
		} else {
			emptyList()
		}
}

@Suppress("UNCHECKED_CAST", "detekt.CastNullableToNonNullableType", "SameParameterValue")
private fun <T> load(className: String): Class<T> =
	Class.forName(className) as Class<T>
