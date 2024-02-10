package net.twisterrob.ghlint.ruleset

import net.twisterrob.ghlint.rule.Rule

public interface RuleSet {

	public fun createRules(): List<Rule>
}
