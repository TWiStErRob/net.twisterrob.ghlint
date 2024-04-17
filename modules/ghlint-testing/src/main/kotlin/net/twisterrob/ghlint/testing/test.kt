package net.twisterrob.ghlint.testing

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.atLeastSize
import io.kotest.matchers.shouldHave
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldNotStartWith
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.ruleset.RuleSet
import net.twisterrob.ghlint.testing.jupiter.AcceptFailingDynamicTest
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
 * fun test(): List<DynamicNode> = test(DefaultRuleSet::class)
 * ```
 *
 * @see AcceptFailingDynamicTest
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
				.map { rule ->
					dynamicContainer(
						"Rule ${rule::class.simplerName}",
						test(rule)
					)
				}
		),
	)

/**
 * Validates a [Rule] and its [Issue]s.
 *
 * Usage:
 * ```
 * @TestFactory
 * fun metadata(): List<DynamicNode> = test(MyRule::class)
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

private fun test(rule: Rule): List<DynamicNode> = listOf(
	dynamicTest("Rule ${rule::class.simplerName} issues are not empty") {
		rule.issues shouldNot io.kotest.matchers.collections.beEmpty()
	},
	dynamicContainer(
		"Rule ${rule::class.simplerName} issues",
		rule.issues.flatMap { issue ->
			testIssue(rule, issue)
		}
	)
)

public fun testIssue(rule: Rule, issue: Issue): List<DynamicNode> = listOf(
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
				testCompliantExamples(rule, issue)
			),
			dynamicContainer(
				"Issue ${issue.id} non-compliant examples",
				testNonCompliantExamples(rule, issue)
			),
		)
	)
)

private fun validateIssueTitle(issue: Issue) {
	withClue("Issue ${issue.id} title") {
		issue.title shouldNot beEmptyString()
		issue.title shouldNotStartWith "TODO"
	}
}

private fun validateIssueDescription(issue: Issue) {
	withClue("Issue ${issue.id} description") {
		issue.description shouldNot beEmptyString()
		// REPORT missing shouldNotMatch overload.
		withClue("contains TODO") {
			val todoRegex = Regex("""^TODO""", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
			issue.description shouldNotContain todoRegex
		}
	}
}

private fun testCompliantExamples(rule: Rule, issue: Issue): List<DynamicNode> {
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
					validate(
						yaml = example.content,
						fileName = "compliant/${example.path}",
					) shouldHave noFindings()
				},
				dynamicTest("${name} has no findings") {
					rule.check(
						yaml = example.content,
						fileName = "compliant/${example.path}",
						validate = false,
					) shouldHave noFindings()
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

private fun testNonCompliantExamples(rule: Rule, issue: Issue): List<DynamicNode> {
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
					val validation = validate(
						yaml = example.content,
						fileName = "non-compliant/${example.path}",
					)
					val ruleOutput = rule.check(
						yaml = example.content,
						fileName = "non-compliant/${example.path}",
						validate = false,
					)
					val findings = validation + ruleOutput
					findings shouldHave onlyFindings(issue.id)
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
