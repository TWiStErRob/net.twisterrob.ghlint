package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class ComponentCountRule : VisitorRule {

	override val issues: List<Issue> = listOf(TooManyJobs, TooManySteps)

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		val jobCount = workflow.jobs.size
		if (jobCount > MAX_JOB_COUNT) {
			reporting.report(TooManyJobs, workflow) {
				"${it} has ${jobCount} jobs, maximum recommended is ${MAX_JOB_COUNT}."
			}
		}
	}

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		super.visitNormalJob(reporting, job)
		val stepCount = job.steps.size
		if (stepCount > MAX_STEP_COUNT) {
			reporting.report(TooManySteps, job) {
				"${it} has ${stepCount} steps, maximum recommended is ${MAX_STEP_COUNT}."
			}
		}
	}

	private companion object {

		private const val MAX_JOB_COUNT = 10
		private const val MAX_STEP_COUNT = 20

		val TooManyJobs = Issue(
			id = "TooManyJobs",
			title = "Workflow has too many jobs.",
			description = """
				Workflow complexity is too high.
				
				Consider the following refactors to reduce complexity:
				 * Introduce a matrix to reduce duplication.
				 * Split the workflow into multiple nested workflows with `workflow_call`.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Simple workflow with only a single job.",
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
					explanation = "Complex workflow with 5 reusable workflow calls and 5 normal jobs.",
					content = """
						on: push
						jobs:
						  example1:
						    uses: other/workflow.yml
						  example2:
						    uses: other/workflow.yml
						  example3:
						    uses: other/workflow.yml
						  example4:
						    uses: other/workflow.yml
						  example5:
						    uses: other/workflow.yml
						  example6:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						  example7:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						  example8:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						  example9:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						  example10:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						  example11:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)

		val TooManySteps = Issue(
			id = "TooManySteps",
			title = "Job has too many steps.",
			description = """
				Job complexity is too high.
				
				Consider the following refactors to reduce complexity:
				 * Remove unused steps.
				 * Split up job into meaningful smaller jobs with `needs` dependencies.
				 * Extract tightly coupled steps into a composite action.
				 * Extract common steps into a reusable composite action.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Simple job with only a few steps.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
						      - run: echo "Example"
						      - run: echo "Example"
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Complex job with 20 steps.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example 1"
						      - run: echo "Example 2"
						      - run: echo "Example 3"
						      - run: echo "Example 4"
						      - run: echo "Example 5"
						      - run: echo "Example 6"
						      - run: echo "Example 7"
						      - run: echo "Example 8"
						      - run: echo "Example 9"
						      - run: echo "Example 10"
						      - run: echo "Example 11"
						      - run: echo "Example 12"
						      - run: echo "Example 13"
						      - run: echo "Example 14"
						      - run: echo "Example 15"
						      - run: echo "Example 16"
						      - run: echo "Example 17"
						      - run: echo "Example 18"
						      - run: echo "Example 19"
						      - run: echo "Example 20"
						      - run: echo "Example 21"
					""".trimIndent(),
				),
			),
		)
	}
}
