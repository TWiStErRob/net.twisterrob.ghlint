package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.ruleset.RuleSet

public class Analyzer {

	public fun analyzeWorkflows(workflows: List<Workflow>, ruleSets: List<RuleSet>): List<Finding> {
		val findings = workflows.flatMap { workflow ->
			ruleSets
				.asSequence()
				.flatMap { it.createRules() }
				.map(::SafeRule)
				.flatMap { rule -> rule.check(workflow) }
		}
		return findings
	}
}
