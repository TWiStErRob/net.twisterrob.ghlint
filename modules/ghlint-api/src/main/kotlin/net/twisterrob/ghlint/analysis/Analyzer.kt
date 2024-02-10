package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.ruleset.RuleSet

public class Analyzer {

	public fun analyzeWorkflows(workflows: List<Workflow>, vararg ruleSets: RuleSet): List<Finding> {

		val findings = workflows.flatMap { workflow ->
			ruleSets
				.flatMap { it.createRules() }
				.flatMap {
					try {
						it.check(workflow)
					} catch (e: Exception) {
						listOf(Finding(it, RuleErrored, workflow.location, e.stackTraceToString()))
					}
				}
		}
		return findings
	}

	internal companion object {

		private val RuleErrored = Issue("RuleErrored", "JSON-Schema based validation problem.")
	}
}
