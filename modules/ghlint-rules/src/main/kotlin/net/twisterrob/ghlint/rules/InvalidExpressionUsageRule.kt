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

public class InvalidExpressionUsageRule : VisitorRule, ActionVisitor, WorkflowVisitor {

	override val issues: List<Issue> = listOf(InvalidExpression)

	override fun visitWorkflowUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		super.visitWorkflowUsesStep(reporting, step)

		if (step.uses.uses.containsGitHubExpression()) {
			reporting.report(InvalidExpression, step) {
				"${it} contains a GitHub expression in the `uses` field."
			}
		}
	}

	override fun visitActionUsesStep(reporting: Reporting, step: ActionStep.Uses) {
		super.visitActionUsesStep(reporting, step)

		if (step.uses.uses.containsGitHubExpression()) {
			reporting.report(InvalidExpression, step) {
				"${it} contains a GitHub expression in the `uses` field."
			}
		}
	}

	private companion object {
		const val uses = "actions/checkout@\${{ github.sha }}"

		val InvalidExpression = Issue(
			id = "InvalidExpressionUsage",
			title = "Expressions should not be used in uses field.",
			description = """
				> GitHub Action Expressions can be used to programmatically set environment variables in workflow files.
				> -- [About expressions](https://docs.github.com/en/actions/learn-github-actions/expressions)
				
				However, they cannot be used within the uses field. GitHub will report an error - "The workflow is not valid: A template expression is not allowed in this context"
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "GitHub Expression not used within the uses field.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
					""".trimIndent(),
					),
			),
			nonCompliant = listOf(
				Example(
					explanation = "GitHub Expression used within the uses field.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: $uses
					""".trimIndent(),
				),
			)
		)
	}
}

private fun String.containsGitHubExpression(): Boolean {
	val regex = Regex("""\$\{\{.*?}}""")
	return regex.containsMatchIn(this)
}
