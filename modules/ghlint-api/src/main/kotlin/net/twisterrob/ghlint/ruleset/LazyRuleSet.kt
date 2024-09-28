package net.twisterrob.ghlint.ruleset

import net.twisterrob.ghlint.rule.Rule

/**
 * A [RuleSet] that uses factory functions to instantiate rules.
 * This can be easily used to instantiate rules via their default constructor.
 *
 * Example usage:
 * ```kotlin
 * public class MyRuleSet : RuleSet by LazyRuleSet(
 *     id = "my-id",
 *     name = "My Name",
 *     ::MyRule1,
 *     ::MyRule2,
 *     …
 * )
 * ```
 */
public class LazyRuleSet(
	public override val id: String,
	public override val name: String,
	public vararg val rules: () -> Rule,
) : RuleSet {

	override fun createRules(): List<Rule> =
		rules.map { it.invoke() }
}
