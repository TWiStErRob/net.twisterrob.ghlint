package net.twisterrob.ghlint.ruleset

import net.twisterrob.ghlint.rule.Rule

public interface RuleSet {

	public val id: String

	public val name: String

	public fun createRules(): List<Rule>
}
