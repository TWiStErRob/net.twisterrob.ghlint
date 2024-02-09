package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.from
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.yaml.Yaml

public class Analyzer {

	public fun analyzeWorkflows(files: List<File>, rules: List<Rule>): List<Finding> {
//		val validation = files
//			.map { file -> Yaml.validate(file.readText()) }
//			.map { TODO() }
		val workflows = files
			.map { file -> Workflow.from(file, Yaml.load(file.readText())) }
		val findings = workflows.flatMap { workflow ->
			rules.flatMap { rule ->
				rule.check(workflow)
			}
		}
		return findings
	}
}
