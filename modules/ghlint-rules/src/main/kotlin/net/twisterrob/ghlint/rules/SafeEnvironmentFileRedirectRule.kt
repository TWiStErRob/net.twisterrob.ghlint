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

public class SafeEnvironmentFileRedirectRule : VisitorRule, WorkflowVisitor, ActionVisitor {

	override val issues: List<Issue> = listOf(SafeEnvironmentFileRedirect)

	override fun visitWorkflowRunStep(reporting: Reporting, step: WorkflowStep.Run) {
		super.visitWorkflowRunStep(reporting, step)
		validate(reporting, step, step)
	}

	override fun visitActionRunStep(reporting: Reporting, step: ActionStep.Run) {
		super.visitActionRunStep(reporting, step)
		validate(reporting, step, step)
	}

	private fun validate(reporting: Reporting, step: Step.Run, target: Component) {
		GITHUB_ENVIRONMENT_FILE_REGEX.findAll(step.run).forEach { match ->
			val environmentFile = match.groups.getRequired("environmentFile")
			val prefix = match.groups.getRequired("prefix")
			val suffix = match.groups.getRequired("suffix")
			if (prefix != "\"\${" && suffix != "}\"") {
				reporting.report(SafeEnvironmentFileRedirect, target) {
					"""${it} should be formatted as `>> "${'$'}{${environmentFile}}"`."""
				}
			}
		}
	}

	@Suppress("detekt.ClassOrdering") // Keep logic above Issue declaration for easy scrolling.
	private companion object {

		private val environmentFileName = Regex(
			"(?<environmentFile>GITHUB_ENV|GITHUB_OUTPUT|GITHUB_PATH|GITHUB_STEP_SUMMARY)"
		)
		private val GITHUB_ENVIRONMENT_FILE_REGEX = Regex(
			""">>?(\s*(\\\n)?)*(?<prefix>"?\$\{?)${environmentFileName}(?<suffix>}?"?)"""
		)

		private fun MatchGroupCollection.getRequired(name: String): String =
			(this[name] ?: error("Invalid regex, group <${name}> was not matched.")).value

		val SafeEnvironmentFileRedirect = Issue(
			id = "SafeEnvironmentFileRedirect",
			title = "`GITHUB_OUTPUT` must be quoted.",
			description = """
				Environment files can be written in many different ways from shell scripts.
				
				To be consistent with ShellCheck recommendations
				([SC2086](https://www.shellcheck.net/wiki/SC2086) and [SC2250](https://www.shellcheck.net/wiki/SC2250)),
				it is recommended to quote and use curly braces around environment file path variables, for example:
				```shell
				>> "${'$'}{GITHUB_OUTPUT}"
				```
				
				While other styles also work, this style is the most robust and safe.
				If this style is copied elsewhere it can only benefit the target script.
				In short, the benefits are:
				
				 * Quotes around file paths help with spaces and special characters.
				 * Curly braces help with explicit variable references and disambiguation.
				
				References:
				
				 * [Environment files documentation](https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#environment-files)
				 * [Prefer putting braces around variable references even when not strictly required.](https://www.shellcheck.net/wiki/SC2250)
				 * [Double quote to prevent globbing and word splitting.](https://www.shellcheck.net/wiki/SC2086)
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Fully quoted and safe access to `GITHUB_OUTPUT` environment variable.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "result=Example" >> "${'$'}{GITHUB_OUTPUT}"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Missing quotes and curly braces around `GITHUB_OUTPUT`.",
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
		)
	}
}
