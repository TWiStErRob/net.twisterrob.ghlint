package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.ruleset.RuleSet

public class Analyzer {

	public fun analyzeWorkflows(workflows: List<Workflow>, vararg ruleSets: RuleSet): List<Finding> {

		val findings = workflows.flatMap { workflow ->
			ruleSets
				.flatMap { it.createRules() }
				.flatMap { it.check(workflow) }
		}
		return findings
	}
}
