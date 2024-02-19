package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class ActionInputsRule : VisitorRule {

	override val issues: List<Issue> = listOf(InvalidActionInput)

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		super.visitUsesStep(reporting, step)
		val action = step.uses.action
		val (required, _) = action.inputs.values.partition { it.required }
		val missing = required.map { it.id } - step.with.orEmpty().keys
		if (missing.isNotEmpty()) {
			reporting.report(InvalidActionInput, step) { "${it} is missing inputs: ${missing}." }
		}
	}

	public companion object {

		private val InvalidActionInput = Issue(
			id = "InvalidActionInput",
			title = "TODO Object is verb subject.",
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
						      - run: echo "Example"
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
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)
	}
}
