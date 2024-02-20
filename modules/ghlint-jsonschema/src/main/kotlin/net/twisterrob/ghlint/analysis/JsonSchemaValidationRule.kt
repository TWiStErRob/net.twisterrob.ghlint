package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule

internal class JsonSchemaValidationRule : Rule {

	override val issues: List<Issue> = listOf(JsonSchemaValidation)

	override fun check(file: File): List<Finding> =
		if (file.location.path == "test.yml") {
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
	}
}
