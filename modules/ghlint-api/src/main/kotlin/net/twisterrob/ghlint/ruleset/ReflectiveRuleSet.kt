package net.twisterrob.ghlint.ruleset

import net.twisterrob.ghlint.rule.Rule
import kotlin.reflect.KClass

public class ReflectiveRuleSet(
	public val name: String,
	public vararg val rules: KClass<out Rule>,
) : RuleSet {

	override fun createRules(): List<Rule> =
		rules.map { it.java.getDeclaredConstructor().newInstance() }
}
