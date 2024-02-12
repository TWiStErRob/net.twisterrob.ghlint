package net.twisterrob.ghlint.ruleset

import net.twisterrob.ghlint.rule.Rule
import kotlin.reflect.KClass

public class ReflectiveRuleSet(
	public override val id: String,
	public override val name: String,
	public vararg val rules: KClass<out Rule>,
) : RuleSet {

	override fun createRules(): List<Rule> =
		rules.map { it.java.getDeclaredConstructor().newInstance() }
}
