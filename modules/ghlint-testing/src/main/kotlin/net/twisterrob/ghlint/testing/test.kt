package net.twisterrob.ghlint.testing

import io.kotest.matchers.collections.atLeastSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldHave
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldNotStartWith
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.stream.Stream
import kotlin.reflect.KClass
import io.kotest.matchers.string.beEmpty as beEmptyString

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
 *     validate(MyRule::class)
 * ```
 */
public fun test(rule: KClass<out Rule>): DynamicNode =
	dynamicContainer(
		"Rule ${rule.simplerName}",
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
				"Issue ${issue.id}",
				listOf(
					dynamicTest("Issue ${issue.id} title") {
						validateIssueTitle(issue)
					},
					dynamicTest("Issue ${issue.id} description") {
						validateIssueDescription(issue)
					},
					dynamicContainer(
						"Issue ${issue.id} compliant examples",
						testCompliantExamples(instance, issue)
					),
					dynamicContainer(
						"Issue ${issue.id} non-compliant examples",
						testNonCompliantExamples(instance, issue)
					),
				)
			)
		}
	)
)

private fun testCompliantExamples(instance: Rule, issue: Issue): List<DynamicNode> {
	val basics = listOf(
		dynamicTest("Issue ${issue.id} compliant examples are not empty") {
			issue.compliant shouldHave atLeastSize(1)
		}
	)
	val examples = issue.compliant.mapIndexed { index, example ->
		val name = "Issue ${issue.id} compliant example #${index + 1}"
		dynamicContainer(
			name,
			listOf(
				dynamicTest("${name} syntax") {
					validate(example.content) should beEmpty()
				},
				dynamicTest("${name} has no findings") {
					instance.check(example.content) shouldNot haveOnlyFindings(issue.id)
				},
				dynamicTest("${name} explanation") {
					example.explanation shouldNot io.kotest.matchers.string.beEmpty()
					example.explanation shouldNotStartWith "TODO"
				}
			)
		)
	}
	return basics + examples
}

private fun testNonCompliantExamples(instance: Rule, issue: Issue): List<DynamicNode> {
	val basics = listOf(
		dynamicTest("Issue ${issue.id} non-compliant examples are not empty") {
			issue.nonCompliant shouldHave atLeastSize(1)
		}
	)
	val examples = issue.nonCompliant.mapIndexed { index, example ->
		val name = "Issue ${issue.id} non-compliant example #${index + 1}"
		dynamicContainer(
			name,
			listOf(
				dynamicTest("${name} has findings") {
					val findings = validate(example.content) + instance.check(example.content)
					findings should haveOnlyFindings(issue.id)
				},
				dynamicTest("${name} explanation") {
					example.explanation shouldNot beEmptyString()
					example.explanation shouldNotStartWith "TODO"
				}
			)
		)
	}
	return basics + examples
}

private val KClass<*>.simplerName: String
	get() = this.simpleName ?: this.qualifiedName ?: this.java.name
