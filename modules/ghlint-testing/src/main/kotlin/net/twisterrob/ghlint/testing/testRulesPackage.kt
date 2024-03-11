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
		.overrideClassLoaders(this)
		.ignoreParentClassLoaders()
		.acceptPackages(rulesPackage.name) // Includes subpackages.
		.scan()
		.use { scanResult ->
			scanResult.getClassesImplementing(Rule::class.java)
				.filterNot { it.isAbstract }
				.map { it.loadClass<Rule>() }
		}

@Suppress("UNCHECKED_CAST", "EXTENSION_SHADOWED_BY_MEMBER")
private inline fun <reified T> ClassInfo.loadClass(): Class<T> =
	(this.loadClass() ?: error("Class not found: ${this.name}")) as Class<T>
