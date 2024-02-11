package net.twisterrob.ghlint.testing

import io.kotest.matchers.shouldNot
import net.twisterrob.ghlint.ruleset.RuleSet
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * Usage:
 * ```
 * @TestFactory
 * fun test(): List<DynamicNode> =
 *     validate(DefaultRuleSet::class)
 * ```
 */
public fun validate(ruleSet: KClass<out RuleSet>): List<DynamicNode> =
	listOf(
		DynamicTest.dynamicTest("RuleSet ${ruleSet.simplerName} is instantiatable") {
			ruleSet.java.getDeclaredConstructor().newInstance()
		},
		DynamicTest.dynamicTest("RuleSet ${ruleSet.simplerName} can create rules") {
			val instance = ruleSet.java.getDeclaredConstructor().newInstance()
			instance.createRules()
		},
		DynamicContainer.dynamicContainer(
			"RuleSet ${ruleSet.simplerName} rules",
			Stream
				.generate { ruleSet.java.getDeclaredConstructor().newInstance() }
				.limit(1)
				.flatMap { it.createRules().stream() }
				.map { rule ->
					DynamicContainer.dynamicContainer(
						"Rule ${rule::class.simplerName} is valid",
						listOf(
							DynamicTest.dynamicTest("Rule ${rule::class.simplerName} issues are not empty") {
								rule.issues shouldNot io.kotest.matchers.collections.beEmpty()
							},
							DynamicContainer.dynamicContainer(
								"Rule ${rule::class.simplerName} issues",
								rule.issues.map { issue ->
									DynamicContainer.dynamicContainer(
										"Issue ${issue.id} is valid",
										listOf(
											DynamicTest.dynamicTest("Issue ${issue.id} description is valid") {
												validateIssueDescription(issue)
											},
											DynamicTest.dynamicTest("Issue ${issue.id} reasoning is valid") {
												validateIssueReasoning(issue)
											},
											DynamicTest.dynamicTest("Issue ${issue.id} compliant examples are valid") {
												rule.validateCompliantExamples(issue)
											},
											DynamicTest.dynamicTest("Issue ${issue.id} non-compliant examples are valid") {
												rule.validateNonCompliantExamples(issue)
											},
										)
									)
								}
							)
						)
					)
				}
		)
	)

private val KClass<*>.simplerName: String
	get() = this.simpleName ?: this.qualifiedName ?: this.java.name
