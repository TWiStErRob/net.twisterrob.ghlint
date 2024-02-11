package net.twisterrob.ghlint.testing

import io.kotest.matchers.shouldNot
import net.twisterrob.ghlint.ruleset.RuleSet
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream
import kotlin.reflect.KClass

public fun validate(ruleSet: KClass<out RuleSet>): List<DynamicNode> =
	listOf(
		DynamicTest.dynamicTest("RuleSet ${ruleSet.simpleName} is instantiatable") {
			ruleSet.java.getDeclaredConstructor().newInstance()
		},
		DynamicTest.dynamicTest("RuleSet ${ruleSet.simpleName} can create rules") {
			val instance = ruleSet.java.getDeclaredConstructor().newInstance()
			instance.createRules()
		},
		DynamicContainer.dynamicContainer(
			"RuleSet ${ruleSet.simpleName} rules",
			Stream
				.generate { ruleSet.java.getDeclaredConstructor().newInstance() }
				.limit(1)
				.flatMap { it.createRules().stream() }
				.map { rule ->
					DynamicContainer.dynamicContainer(
						"Rule ${rule::class.simpleName} is valid",
						listOf(
							DynamicTest.dynamicTest("Rule ${rule::class.simpleName} issues are not empty") {
								rule.issues shouldNot io.kotest.matchers.collections.beEmpty()
							},
							DynamicContainer.dynamicContainer(
								"Rule ${rule::class.simpleName} issues",
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
