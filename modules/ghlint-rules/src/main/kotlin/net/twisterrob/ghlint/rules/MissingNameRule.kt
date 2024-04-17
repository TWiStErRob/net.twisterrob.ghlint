package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor
import net.twisterrob.ghlint.rule.report

public class MissingNameRule : VisitorRule, WorkflowVisitor {

	override val issues: List<Issue> = listOf(MissingWorkflowName, MissingJobName, MissingStepName)

	public override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		if (workflow.name == null) {
			reporting.report(MissingWorkflowName, workflow, Message)
		}
	}

	public override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		if (job.name == null) {
			reporting.report(MissingJobName, job, Message)
		}
	}

	public override fun visitWorkflowStep(reporting: Reporting, step: WorkflowStep) {
		super.visitWorkflowStep(reporting, step)
		if (step.name == null) {
			reporting.report(MissingStepName, step, Message)
		}
	}

	private companion object {

		private val Message: (String) -> String = {
			"${it} is missing a name, add one to improve developer experience."
		}

		val MissingWorkflowName = Issue(
			id = "MissingWorkflowName",
			title = "Workflow is missing a name.",
			description = """
				Having a workflow name is important for usability.
				The default workflow name is the file name, but it's recommended to override it for human consumption:
				
				 * The workflow name is visible at various parts of the GitHub UI, most notably in the Actions tab.
				 * It's also used in Email subjects, for example:
				   `[<org>/<repo>] Run failed: <workflow name> - <branch name> (<hash>)`.
				 * It's also useful when opening the file for viewing or editing,
				   to give some context of what's expected to happen in the workflow.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "The workflow has a name.",
					content = """
						name: "My Workflow"
						on: push
						jobs:
						  example:
						    name: "My Job"
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Example"
						        run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "The workflow is missing a name.",
					content = """
						on: push
						jobs:
						  example:
						    name: "My Job"
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Example"
						        run: echo "Example"
					""".trimIndent(),
				),
			),
		)

		val MissingJobName = Issue(
			id = "MissingJobName",
			title = "Job is missing a name.",
			description = """
				Having a job name is important for usability.
				
				The default job name is the id of the job, but it's recommended to override it for human consumption:
				
				 * The job name is visible at various parts of the GitHub UI,
				   most notably in the Checks UI on commits, bottom of pull requests and merge queues.
				 * It's also prominently visible when looking into a workflow run:
				   in the Summary dependency graph, in the Jobs tree on the left and as a title for logs.
				 * It's also used in Email contents, listing each job as failed or succeeded.
				 * It's also useful when opening the file for viewing or editing,
				   to give some context of what's expected to happen in the job.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "The example job has a name.",
					content = """
						name: "Example"
						on: push
						jobs:
						  example:
						    name: "My Job"
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Example"
						        run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "The example job is missing a name.",
					content = """
						name: "Example"
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Example"
						        run: echo "Example"
					""".trimIndent(),
				),
			),
		)

		val MissingStepName = Issue(
			id = "MissingStepName",
			title = "Step is missing a name.",
			description = """
				Having a step name is important for usability.
				
				The default step name is the first line of `run:` or the action of `uses:`,
				but it's recommended to override it for human consumption:
				
				 * The step name is the header shown when a step is collapsed on the workflow run UI.
				 * Using a succinct, but descriptive name can help to understand the workflow run at a glance.
				 * It's also useful when opening the file for viewing or editing,
				   it gives the file very nice structure if each step starts with a name.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "The first step has a name.",
					content = """
						name: "Example"
						on: push
						jobs:
						  example:
						    name: "Example"
						    runs-on: ubuntu-latest
						    steps:
						      - name: "My Step"
						        run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "The first step is missing a name.",
					content = """
						name: "Example"
						on: push
						jobs:
						  example:
						    name: "Example"
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)
	}
}
