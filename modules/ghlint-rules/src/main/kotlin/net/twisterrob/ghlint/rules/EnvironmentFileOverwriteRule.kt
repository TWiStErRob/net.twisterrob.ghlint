package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.Component
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.ActionVisitor
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class EnvironmentFileOverwriteRule : VisitorRule, WorkflowVisitor, ActionVisitor {

	override val issues: List<Issue> = listOf(EnvironmentFileOverwritten)

	override fun visitWorkflowRunStep(reporting: Reporting, step: WorkflowStep.Run) {
		super.visitWorkflowRunStep(reporting, step)
		validate(reporting, step, step)
	}

	override fun visitActionRunStep(reporting: Reporting, step: ActionStep.Run) {
		super.visitActionRunStep(reporting, step)
		validate(reporting, step, step)
	}

	private fun validate(reporting: Reporting, step: Step.Run, target: Component) {
		GITHUB_ENVIRONMENT_FILE_OVERWRITE_REGEX.findAll(step.run).forEach { match ->
			val environmentFile = match.groups["environmentFile"]?.value ?: error("Invalid regex match")
			reporting.report(EnvironmentFileOverwritten, target) {
				"${it} overwrites environment file `${environmentFile}`."
			}
		}
	}

	private companion object {

		private val environmentFileName = Regex(
			"(?<environmentFile>GITHUB_ENV|GITHUB_OUTPUT|GITHUB_PATH|GITHUB_STEP_SUMMARY)"
		)
		private val GITHUB_ENVIRONMENT_FILE_OVERWRITE_REGEX = Regex(
			"""(?<!>)>(\s*(\\\n)?)*(?<quote>"?)\$\{?${environmentFileName}\}?(\k<quote>)"""
		)

		val EnvironmentFileOverwritten = Issue(
			id = "EnvironmentFileOverwritten",
			title = "Environment files should be appended.",
			description = """
				Most environment files are used to pass data between steps, and should be appended to, not overwritten.
				
				It is a common mistake to overwrite environment files, which can lead to data loss or unexpected behavior.
				
				Use `>>` instead of `>`, to ensure the file is appended, not overwritten.
				
				References:
				
				 * [Environment files documentation](https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#environment-files)
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Appending to an environment file is usually the intended behavior.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "result=Example" >> ${'$'}GITHUB_OUTPUT
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Overwriting an environment file is probably unintended.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "result=Example" > ${'$'}GITHUB_OUTPUT
					""".trimIndent(),
				),
			),
		)
	}
}
