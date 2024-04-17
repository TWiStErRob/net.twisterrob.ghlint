package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.WorkflowVisitor
import net.twisterrob.ghlint.rule.report

public class EnvironmentFileOverwriteRule : VisitorRule, WorkflowVisitor {

	override val issues: List<Issue> = listOf(EnvironmentFileOverwritten)

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		GITHUB_ENVIRONMENT_FILE_OVERWRITE_REGEX.findAll(step.run).forEach { match ->
			val environmentFile = match.groups["environmentFile"]?.value ?: error("Invalid regex match")
			reporting.report(EnvironmentFileOverwritten, step) {
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
