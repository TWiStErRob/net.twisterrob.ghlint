package net.twisterrob.ghlint.analysis

import dev.harrel.jsonschema.Validator
import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.SnakeAction
import net.twisterrob.ghlint.model.SnakeWorkflow
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Rule
import net.twisterrob.ghlint.yaml.YamlValidation
import net.twisterrob.ghlint.yaml.resolve
import net.twisterrob.ghlint.yaml.toLocation
import org.snakeyaml.engine.v2.nodes.Node

internal class JsonSchemaValidationRule : Rule {

	override val issues: List<Issue> = listOf(JsonSchemaValidation)

	override fun check(file: File): List<Finding> =
		when (val content = file.content) {
			is Workflow -> {
				content as SnakeWorkflow
				YamlValidation.validate(content.node).toFindings(content.node, file)
			}

			is Action -> {
				content as SnakeAction
				YamlValidation.validate(content.node).toFindings(content.node, file)
			}

			is InvalidContent -> {
				listOf(
					Finding(
						rule = this@JsonSchemaValidationRule,
						issue = JsonSchemaValidation,
						location = content.location,
						message = "File could not be parsed: ${content.error}\n${content.raw}"
					)
				)
			}
		}

	private fun Validator.Result.toFindings(root: Node, file: File): List<Finding> = this
		.errors
		.filter { it.error != "False schema always fails" }
		.map { error ->
			Finding(
				rule = this@JsonSchemaValidationRule,
				issue = JsonSchemaValidation,
				location = root.resolve(error.instanceLocation).toLocation(file),
				message = "${error.error} (${error.instanceLocation})"
			)
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
					explanation = "Requires at least one job in `jobs:`.",
					content = """
						on: push
						jobs: {}
					""".trimIndent(),
				),
				Example(
					explanation = "Missing `on:` trigger list.",
					content = """
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)
	}
}
