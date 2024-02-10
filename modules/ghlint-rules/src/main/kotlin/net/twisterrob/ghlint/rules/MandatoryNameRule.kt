package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

@Suppress("detekt.StringLiteralDuplication") // Inside lambda, only visually identical.
public class MandatoryNameRule : VisitorRule {

	public override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		if (workflow.name == null) {
			reporting.report(MandatoryWorkflowName, workflow) { "${it} must have a name." }
		}
	}

	public override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		if (job.name == null) {
			reporting.report(MandatoryJobName, job) { "${it} must have a name." }
		}
	}

	public override fun visitStep(reporting: Reporting, step: Step) {
		super.visitStep(reporting, step)
		if (step.name == null) {
			reporting.report(MandatoryStepName, step) { "${it} must have a name." }
		}
	}

	internal companion object {

		val MandatoryWorkflowName = Issue(
			id = "MandatoryWorkflowName",
			description = "Workflow must have a name.",
			reasoning = """
				Having a workflow name is important for usability.
				The workflow name is visible at various parts of the GitHub UI, most notable in the Actions tab.
				It's also used in Email subjects, for example:
				"[<org>/<repo>] Run failed: <workflow name> - <branch name> (<hash>)".
				It's also useful when opening the file for viewing or editing,
				to give some context of what's expected to happen in the workflow.
			""".trimIndent(),
			compliant = listOf(
				Example(
					"""
						name: "My Workflow"
						on: push
						jobs:
						  test:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent()
				)
			),
			nonCompliant = listOf(
				Example(
					"""
						on: push
						jobs:
						  test:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent()
				)
			)
		)

		val MandatoryJobName = Issue(
			id = "MandatoryJobName",
			description = "Job must have a name.",
			compliant = listOf(
				Example(
					"""
						on: push
						jobs:
						  test:
						    name: "My Job"
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent()
				)
			),
			nonCompliant = listOf(
				Example(
					"""
						on: push
						jobs:
						  test:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent()
				)
			)
		)

		val MandatoryStepName = Issue(
			id = "MandatoryStepName",
			description = "Step must have a name.",
			compliant = listOf(
				Example(
					"""
						on: push
						jobs:
						  test:
						    runs-on: ubuntu-latest
						    steps:
						      - name: "My Step"
						        run: echo "Example"
					""".trimIndent()
				)
			),
			nonCompliant = listOf(
				Example(
					"""
						on: push
						jobs:
						  test:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent()
				)
			)
		)
	}
}
