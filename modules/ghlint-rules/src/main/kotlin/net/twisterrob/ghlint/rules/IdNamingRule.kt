package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.id
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

@Suppress("detekt.StringLiteralDuplication") // Inside lambda, only visually identical.
public class IdNamingRule : VisitorRule {

	override val issues: List<Issue> = listOf(WorkflowIdNaming, JobIdNaming, StepIdNaming)

	public override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		if (!isValid(workflow.id)) {
			reporting.report(WorkflowIdNaming, workflow) { "${it} should have a lower-case kebab ID." }
		}
	}

	public override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		if (!isValid(job.id)) {
			reporting.report(JobIdNaming, job) { "${it} should have a lower-case kebab ID." }
		}
	}

	public override fun visitStep(reporting: Reporting, step: Step) {
		super.visitStep(reporting, step)
		if (step.id?.let(::isValid) == false) {
			reporting.report(StepIdNaming, step) { "${it} should have a lower-case kebab ID." }
		}
	}

	private fun isValid(id: String): Boolean =
		LOWER_KEBAB.matches(id)

	private companion object {

		private val LOWER_KEBAB = Regex("^[a-z0-9-]+$")

		val WorkflowIdNaming = Issue(
			id = "WorkflowIdNaming",
			title = "Workflow should have a lower-case kebab ID.",
			description = """
				> Workflows are defined by a YAML file checked in to your repository
				> -- [About workflows](https://docs.github.com/en/actions/using-workflows/about-workflows#about-workflows)
				
				This means their names could be pretty much anything that's a valid file name.
				Conventionally it's best to use lower-kebab-case naming for workflow file names.
				
				Workflow IDs (or their file names) appear
				
				 * in GitHub's Actions tab URLs, e.g.:
				   `https://github.com/TWiStErRob/net.twisterrob.ghlint/actions/workflows/ci-build.yml`
				 * in commands / shell scripts when triggering 
				   a [`workflow_dispatch` event](https://docs.github.com/en/actions/using-workflows/events-that-trigger-workflows#workflow_dispatch)
				   from [`gh` CLI](https://cli.github.com/manual/gh_workflow_run).
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Workflow file named: `ci-build.yml`.",
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
					explanation = "Workflow file named: `CI build.yml`.",
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

		val JobIdNaming = Issue(
			id = "JobIdNaming",
			title = "Job should have a lower-case kebab ID.",
			description = """
				> The Job id must start with a letter or _ and contain only alphanumeric characters, -, or _.
				> --- [jobs.<job_id>](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_id)
				
				Job IDs appear
				
				 * as name of Jobs when their name is not set
				 * in the [`needs` list](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idneeds) of another job to express dependencies
				 * in the [`outputs` declarations of `workflow_call`s](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#onworkflow_calloutputs)
				 * in `if` conditions [e.g. `needs.other-job.outputs.some-output`](https://docs.github.com/en/actions/learn-github-actions/contexts#needs-context)
				
				They're allowed to have `_` in the name,
				however, conventionally it's best to use lower-kebab-case naming for job names
				as it's consistent with the rest of GitHub's Workflow syntax, for example `runs-on`, `timeout-minutes`.
				
				Using kebab case also helps to distinguish their IDs from
				
				 * event names (lower_snake_case)
				 * placeholders (`<job_id>`)
				 * payload fields (`pull_request.auto_merge`)
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Jobs have conventional lower-kebab-case IDs.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    needs: [ something-else ]
						    steps:
						      - run: echo "Example"
						
						  something-else:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Jobs have non-lower-kebab-case IDs, and their IDs are inconsistent with each other.",
					content = """
						on: push
						jobs:
						  EXAMPLE:
						    runs-on: ubuntu-latest
						    needs: [ something_else ]
						    steps:
						      - run: echo "Example"
						
						  something_else:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)

		val StepIdNaming = Issue(
			id = "StepIdNaming",
			title = "Step should have a lower-case kebab ID.",
			description = """
				> A unique identifier for the step.
				> -- [`steps[*].id` documentation](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsid)
				
				The documentation doesn't define what's a valid ID, let alone a recommended one.
				Conventionally it's best to use lower-kebab-case naming for step names.
				This makes them consistent in style with the rest of the workflow syntax and workflow/job names.
				
				Step IDs appear
				
				 * GitHub Expressions, as property dereferences of the
				   [`steps` context](https://docs.github.com/en/actions/learn-github-actions/contexts#steps-context),  
				   e.g. `${'$'}{{ steps.some-step }}`
				 * in `if` conditions on other steps,  
				   e.g. `if: ${'$'}{{ steps.some-step.outputs.some-output == 'something' }}`.
				 * in `outputs` declarations of jobs,  
				   e.g. `some-output: ${'$'}{{ steps.some-step.outputs.some-output }}`.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Not having an id is the best case, use name: to express what the step is to the reader.",
					content = """
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
			nonCompliant = listOf(
				Example(
					explanation = "Step has non-lower-kebab-case ID.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - id: example step_NAME
						        run: echo "Example"
					""".trimIndent(),
				),
			),
		)
	}
}
