package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.ReflectiveRuleSet

public inline fun <reified T : Rule> createRule(): Rule {
	val ruleSet = ReflectiveRuleSet(id = "test-ruleset", name = "Test RuleSet", T::class)
	val rule = ruleSet.createRules().single()
	return rule
}
