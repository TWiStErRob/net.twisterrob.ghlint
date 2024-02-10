package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.from
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.ruleset.RuleSet
import net.twisterrob.ghlint.yaml.Yaml

internal class Analyzer {

	internal fun analyzeWorkflows(files: List<File>, vararg ruleSets: RuleSet): List<Finding> {
		val workflows = files
			.map { file -> Workflow.from(file, Yaml.load(file.readText())) }
		val findings = workflows.flatMap { workflow ->
			ruleSets
				.flatMap { it.createRules() }
				.flatMap { it.check(workflow) }
		}
		return findings
	}
}
