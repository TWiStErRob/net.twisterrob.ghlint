package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Finding
import net.twisterrob.ghlint.model.Rule
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.from
import net.twisterrob.ghlint.yaml.Yaml

public class Analyzer {

	public fun analyzeWorkflows(files: List<File>, rules: List<Rule>): List<Finding> {
		val workflows = files.map { file ->
			Workflow.from(file, Yaml.load(file.readText()))
		}
		val findings = workflows.flatMap { workflow ->
			rules.flatMap { rule ->
				rule.check(workflow)
			}
		}
		return findings
	}
}
