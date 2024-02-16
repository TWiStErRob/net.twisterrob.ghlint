package net.twisterrob.ghlint.testing

import io.kotest.matchers.shouldNot
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * Validates a [RuleSet], it's [Rule]s and their [Issue]s.
 *
 * Usage:
 * ```
 * @TestFactory
 * fun test(): List<DynamicNode> =
 *     validate(DefaultRuleSet::class)
 * ```
 */
public fun test(ruleSet: KClass<out RuleSet>): List<DynamicNode> =
	listOf(
		dynamicTest("RuleSet ${ruleSet.simplerName} is instantiatable") {
			ruleSet.java.getDeclaredConstructor().newInstance()
		},
		dynamicTest("RuleSet ${ruleSet.simplerName} can create rules") {
			val instance = ruleSet.java.getDeclaredConstructor().newInstance()
			instance.createRules()
		},
		dynamicContainer(
			"RuleSet ${ruleSet.simplerName} rules",
			Stream
				.generate { ruleSet.java.getDeclaredConstructor().newInstance() }
				.limit(1)
				.flatMap { it.createRules().stream() }
				.flatMap { rule ->
					test(rule).stream()
				}
		)
	)

/**
 * Validates a [Rule] and its [Issue]s.
 *
 * Usage:
 * ```
 * @TestFactory
 * fun metadata(): List<DynamicNode> =
 *     validate(MyRule())
 * ```
 */
public fun test(rule: KClass<out Rule>): DynamicNode =
	dynamicContainer(
		"Rule ${rule.simplerName} is valid",
		Stream.concat(
			Stream.of(dynamicTest("Rule ${rule.simplerName} is instantiatable") {
				rule.java.getDeclaredConstructor().newInstance()
			}),
			Stream
				.generate { rule.java.getDeclaredConstructor().newInstance() }
				.limit(1)
				.flatMap { instance ->
					test(instance).stream()
				}
		)
	)

private fun test(instance: Rule): List<DynamicNode> = listOf(
	dynamicTest("Rule ${instance::class.simplerName} issues are not empty") {
		instance.issues shouldNot io.kotest.matchers.collections.beEmpty()
	},
	dynamicContainer(
		"Rule ${instance::class.simplerName} issues",
		instance.issues.map { issue ->
			dynamicContainer(
				"Issue ${issue.id} is valid",
				listOf(
					dynamicTest("Issue ${issue.id} title is valid") {
						validateIssueTitle(issue)
					},
					dynamicTest("Issue ${issue.id} description is valid") {
						validateIssueDescription(issue)
					},
					// STOPSHIP split into multiple tests
					dynamicTest("Issue ${issue.id} compliant examples are valid") {
						instance.validateCompliantExamples(issue)
					},
					// STOPSHIP split into multiple tests
					dynamicTest("Issue ${issue.id} non-compliant examples are valid") {
						instance.validateNonCompliantExamples(issue)
					},
				)
			)
		}
	)
)

private val KClass<*>.simplerName: String
	get() = this.simpleName ?: this.qualifiedName ?: this.java.name
