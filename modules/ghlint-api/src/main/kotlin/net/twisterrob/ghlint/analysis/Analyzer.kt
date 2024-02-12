package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.ruleset.RuleSet

public class Analyzer {

	public fun analyzeWorkflows(workflows: List<Workflow>, ruleSets: List<RuleSet>): List<Finding> {
		val findings = workflows.flatMap { workflow ->
			ruleSets
				.flatMap { it.createRules() }
				.flatMap { rule ->
					try {
						rule.check(workflow)
					} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Throwable) {
						// detekt.TooGenericExceptionCaught: Can't know what's wrong, so we can't handle it more specifically.
						val errorFinding = Finding(
							rule = rule,
							issue = RuleErrored,
							location = workflow.location,
							message = ex.stackTraceToString()
						)
						listOf(errorFinding)
					}
				}
		}
		return findings
	}

	internal companion object {

		private val RuleErrored = Issue("RuleErrored", "JSON-Schema based validation problem.")
	}
}
