package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.model.effectiveShell
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor
import net.twisterrob.ghlint.rules.shell.ShellCheck
import java.nio.file.Path

public class ShellCheckRule : VisitorRule, WorkflowVisitor {
	private val sc = ShellCheck(Path.of("."), Path.of("."))

	override val issues: List<Issue> = listOf(ShellCheck)

	override fun visitWorkflowRunStep(reporting: Reporting, step: WorkflowStep.Run) {
		super.visitWorkflowRunStep(reporting, step)
		val script = step.run
		val shell = step.effectiveShell
		val result = sc.check(script, shell)
		result.forEach { sc ->
			reporting.report(ShellCheck, step) { "[${sc.level}] ${sc.url}: ${sc.message}" }
		}
	}

	private companion object {

		val ShellCheck = Issue(
			id = "ShellCheck",
			title = "Run shellcheck on each run step.",
			description = """
				TODO Persuasive sentence about why this is a problem.
				TODO More details about the problem.
				TODO More details about the fix.
				TODO List benefits.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "TODO Describe what to focus on succinctly.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "${'$'}{FOO}"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "TODO Describe what to focus on succinctly.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo ${'$'}FOO
					""".trimIndent(),
				),
			),
		)
	}
}
