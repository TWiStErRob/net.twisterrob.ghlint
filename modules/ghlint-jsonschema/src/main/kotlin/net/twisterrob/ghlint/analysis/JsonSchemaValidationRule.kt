package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule

internal class JsonSchemaValidationRule : Rule {

	override val issues: List<Issue> = listOf(JsonSchemaValidation)

	override fun check(workflow: Workflow): List<Finding> =
		if (workflow.parent.location.path == "test.yml") {
			emptyList()
		} else {
			error("Should never be called.")
		}

	companion object {

		val JsonSchemaValidation = Issue(
			id = "JsonSchemaValidation",
			title = "JSON-Schema based validation problem.",
			description = """
				JSON-Schema validation is required to ensure the GHLint object model is valid.
				
				Fix the problems in the workflow file to make it valid.
				GitHub would also very likely reject the file with an error message similar to:
				```
				Invalid workflow file: .github/workflows/test.yml#L5
				The workflow is not valid. .github/workflows/test.yml (Line: 5, Col: 17): Error message
				```
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Minimal valid workflow.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Missing `on:` and requires at least one job in `jobs:`.",
					content = """
						on: push
						jobs: {}
					""".trimIndent(),
				),
			),
		)

		val SyntaxError = Issue(
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
