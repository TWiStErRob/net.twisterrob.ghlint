package net.twisterrob.ghlint.testing

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet
import kotlin.reflect.KClass

/**
 * Validates a [RuleSet] has rules only from a specific package.
 *
 * Opt in, because packages structures might be special in some projects.
 *
 * Usage:
 * ```
 * @Test
 * fun `includes all rules in the package`() {
 *     testRulesPackage(DefaultRuleSet::class)
 * }
 * ```
 */
public fun testRulesPackage(ruleSet: KClass<out RuleSet>, rulesPackage: Package = ruleSet.java.`package`) {
	val instance = ruleSet.java.getDeclaredConstructor().newInstance()
	val actualRules = instance.createRules().map { it::class.java }
	val expectedRules = ruleSet.java.classLoader.getRulesFrom(rulesPackage)
	actualRules shouldContainExactlyInAnyOrder expectedRules
}

private fun ClassLoader.getRulesFrom(rulesPackage: Package): List<Class<Rule>> =
	ClassGraph()
		.enableClassInfo()
		.enableClassLoaders(this)
		.ignoreParentClassLoaders()
		.acceptPackages(rulesPackage.name) // Includes subpackages.
		.scan()
		.use { scanResult ->
			scanResult.getAllClassesImplementing(Rule::class.java)
				.filterNot { it.isAbstract }
				.map { it.loadClass<Rule>(this) }
		}

@Suppress("UNCHECKED_CAST")
private inline fun <reified T> ClassInfo.loadClass(loader: ClassLoader): Class<T> =
	Class.forName(this.name, false, loader) as Class<T>
