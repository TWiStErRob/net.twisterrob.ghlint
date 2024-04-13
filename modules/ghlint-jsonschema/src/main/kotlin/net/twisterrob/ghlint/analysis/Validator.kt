package net.twisterrob.ghlint.analysis

import dev.harrel.jsonschema.Validator
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.yaml.SnakeYaml
import net.twisterrob.ghlint.yaml.YamlValidation
import net.twisterrob.ghlint.yaml.resolve
import net.twisterrob.ghlint.yaml.toLocation
import org.snakeyaml.engine.v2.nodes.Node

public class Validator {

	public fun validateWorkflows(files: List<File>): List<Finding> {
		val rule = JsonSchemaValidationRule()
		return files.flatMap { file ->
			try {
				validateWorkflow(rule, file)
			} catch (@Suppress("detekt.TooGenericExceptionCaught") e: Throwable) {
				// TooGenericExceptionCaught: don't know what can go wrong,
				// but all the files should be validated, independently.
				listOf(findingForError(rule, file, e))
			}
		}
	}

	private fun findingForError(rule: JsonSchemaValidationRule, file: File, e: Throwable) =
		Finding(
			rule = rule,
			issue = SyntaxError,
			location = Location(
				file.location,
				Position(LineNumber(1), ColumnNumber(1)),
				Position(
					LineNumber(file.content.lineSequence().count()),
					ColumnNumber(file.content.lineSequence().last().count())
				),
			),
			message = e.message ?: "Unknown error in ${file.location.path}"
		)

	private fun validateWorkflow(rule: JsonSchemaValidationRule, file: File): List<Finding> {
		val root: Node = SnakeYaml.load(file.content)
		val result: Validator.Result = YamlValidation.validate(file.content)
		return result.errors
			.filter { it.error != "False schema always fails" }
			.map { error ->
				Finding(
					rule = rule,
					issue = JsonSchemaValidationRule.JsonSchemaValidation,
					location = root.resolve(error.instanceLocation).toLocation(file),
					message = "${error.error} (${error.instanceLocation})"
				)
			}
	}

	public companion object {

		internal val SyntaxError = Issue(
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
