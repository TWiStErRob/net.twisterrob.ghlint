package net.twisterrob.ghlint.ruleset

import net.twisterrob.ghlint.rule.Rule
import kotlin.reflect.KClass

/**
 * A [RuleSet] that uses reflection to instantiate rules via their default constructor.
 *
 * Example usage:
 * ```kotlin
 * public class MyRuleSet : RuleSet by ReflectiveRuleSet(
 *     id = "my-id",
 *     name = "My Name",
 *     MyRule1::class,
 *     MyRule2::class,
 *     …
 * )
 * ```
 *
 * Note: this requires minification to keep default constructors, see `META-INF/com.android.tools/r8/ghlint-api.pro`.
 */
public class ReflectiveRuleSet(
	public override val id: String,
	public override val name: String,
	public vararg val rules: KClass<out Rule>,
) : RuleSet {

	override fun createRules(): List<Rule> =
		rules.map { it.java.getDeclaredConstructor().newInstance() }
}
