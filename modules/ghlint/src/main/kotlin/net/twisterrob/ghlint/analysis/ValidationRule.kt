package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.Action
import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileType
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.SnakeAction
import net.twisterrob.ghlint.model.SnakeErrorContent
import net.twisterrob.ghlint.model.SnakeSyntaxErrorContent
import net.twisterrob.ghlint.model.SnakeUnknownContent
import net.twisterrob.ghlint.model.SnakeWorkflow
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.inferType
import net.twisterrob.ghlint.results.Finding
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.visitor.ActionVisitor
import net.twisterrob.ghlint.rule.visitor.InvalidContentVisitor
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor
import net.twisterrob.ghlint.yaml.YamlValidation
import net.twisterrob.ghlint.yaml.YamlValidationProblem
import net.twisterrob.ghlint.yaml.YamlValidationType
import net.twisterrob.ghlint.yaml.resolve
import net.twisterrob.ghlint.yaml.toLocation
import org.snakeyaml.engine.v2.nodes.Node

internal class ValidationRule : VisitorRule, ActionVisitor, WorkflowVisitor, InvalidContentVisitor {

	override val issues: List<Issue> = listOf(YamlSyntaxError, YamlLoadError, JsonSchemaValidation)

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		workflow as SnakeWorkflow
		YamlValidation.validate(workflow.node, YamlValidationType.WORKFLOW)
			.toFindings(workflow.node, workflow.parent)
			.forEach(reporting::report)
	}

	override fun visitAction(reporting: Reporting, action: Action) {
		action as SnakeAction
		YamlValidation.validate(action.node, YamlValidationType.ACTION)
			.toFindings(action.node, action.parent)
			.forEach(reporting::report)
	}

	override fun visitInvalidContent(reporting: Reporting, content: InvalidContent) {
		@Suppress("detekt.ElseCaseInsteadOfExhaustiveWhen") // REPORT False positive: InvalidContent is not sealed.
		when (content) {
			is SnakeErrorContent -> {
				val file = content.parent
				val type = when (file.location.inferType()) {
					FileType.WORKFLOW -> YamlValidationType.WORKFLOW
					FileType.ACTION -> YamlValidationType.ACTION
					FileType.UNKNOWN -> null
				}
				if (type != null) {
					YamlValidation.validate(content.node, type).toFindings(content.node, file)
						.forEach(reporting::report)
				}
				reporting.report(content.toFinding(YamlLoadError, "loaded"))
			}

			is SnakeSyntaxErrorContent -> {
				reporting.report(content.toFinding(YamlSyntaxError, "parsed"))
			}

			is SnakeUnknownContent -> {
				reporting.report(content.toFinding(YamlLoadError, "loaded"))
			}

			else -> {
				error("Unknown content type: ${content}")
			}
		}
	}

	private fun List<YamlValidationProblem>.toFindings(root: Node, file: File): List<Finding> =
		this
			.filter { it.error != "False schema always fails" }
			.map { error -> error.toFinding(root, file) }

	private fun YamlValidationProblem.toFinding(root: Node, file: File): Finding =
		Finding(
			rule = this@ValidationRule,
			issue = JsonSchemaValidation,
			location = root.resolve(instanceLocation).toLocation(file),
			message = "$error ($instanceLocation)"
		)

	private fun InvalidContent.toFinding(issue: Issue, verb: String): Finding =
		Finding(
			rule = this@ValidationRule,
			issue = issue,
			location = this.location,
			message = @Suppress("detekt.StringShouldBeRawString")
			// Cannot be trimIndent'd, because we don't control the error message.
			"File ${parent.location.path} could not be ${verb}:\n```\n${this.error}\n```"
		)

	companion object {

		private val YamlSyntaxError = Issue(
			id = "YamlSyntaxError",
			title = "YAML syntax error.",
			description = """
				A syntactically correct YAML file is required to ensure the YAML file can be loaded.
				
				Fix the problems in the workflow file to make it a valid YAML file.
				
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

		private val YamlLoadError = Issue(
			id = "YamlLoadError",
			title = "YAML loading error.",
			description = """
				A semantically correct YAML file is required to ensure the GH-Lint object model is valid.
				
				Fix the problems in the workflow file to make it a valid workflow or action YAML file.
				
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
					explanation = "Jobs is required for a workflow, otherwise the work cannot be declared.",
					content = """
						on: push
					""".trimIndent(),
				),
			),
		)

		private val JsonSchemaValidation = Issue(
			id = "JsonSchemaValidation",
			title = "JSON-Schema based validation problem.",
			description = """
				JSON-Schema validation is required to ensure the GH-Lint object model is valid.
				
				Fix the problems in the workflow file
				to make it validate against the JSON schema for the corresponding file type.
				
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
