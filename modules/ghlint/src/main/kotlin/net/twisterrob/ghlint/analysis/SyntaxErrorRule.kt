package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule

internal class SyntaxErrorRule : Rule {

	override val issues: List<Issue> = listOf(SyntaxError)

	override fun check(file: File): List<Finding> =
		if (file.content is InvalidContent) {
			val content = file.content as InvalidContent
			listOf(toFinding(content, file))
		} else {
			// Only concerned about syntax errors, not content errors, other rules will take care of that.
			emptyList()
		}

	private fun toFinding(content: InvalidContent, file: File): Finding =
		Finding(
			rule = this,
			issue = SyntaxError,
			location = content.location,
			message = "File ${file.location.path} could not be parsed: ${content.error}"
		)

	companion object {

		private val SyntaxError = Issue(
			id = "SyntaxError",
			title = "YAML syntax error.",
			description = """
				Parseable YAML file is required to ensure the GHLint object model is valid.
				
				Fix the problems in the workflow file to make it valid.
				GitHub would also very likely reject the file with an error message similar to:
				```
				Invalid workflow file: .github/workflows/test.yml#L5
				The workflow is not valid. .github/workflows/test.yml (Line: 5, Col: 17): Error message
				```
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Valid yaml file.",
					content = """
						on: push
						jobs:
						  example:
						    uses: reusable/workflow.yml
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Tabs cannot be used as indentation.",
					content = """
						on: push
						jobs:
							example:
								uses: reusable/workflow.yml
					""".trimIndent(),
				),
			),
		)
	}
}
