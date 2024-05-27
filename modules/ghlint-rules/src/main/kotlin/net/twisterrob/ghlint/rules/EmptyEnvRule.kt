package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.Component
import net.twisterrob.ghlint.model.Env
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.ActionVisitor
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class EmptyEnvRule : VisitorRule, WorkflowVisitor, ActionVisitor {

	override val issues: List<Issue> = listOf(EmptyWorkflowEnv, EmptyJobEnv, EmptyStepEnv)

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		@Suppress("detekt.NamedArguments")
		checkEmptyEnv(workflow, workflow.env, reporting, EmptyWorkflowEnv)
	}

	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		@Suppress("detekt.NamedArguments")
		checkEmptyEnv(job, job.env, reporting, EmptyJobEnv)
	}

	override fun visitWorkflowStep(reporting: Reporting, step: WorkflowStep) {
		super.visitWorkflowStep(reporting, step)
		@Suppress("detekt.NamedArguments")
		checkEmptyEnv(step, step.env, reporting, EmptyStepEnv)
	}

	override fun visitActionStep(reporting: Reporting, step: ActionStep) {
		super.visitActionStep(reporting, step)
		@Suppress("detekt.NamedArguments")
		checkEmptyEnv(step, step.env, reporting, EmptyStepEnv)
	}

	@Suppress("detekt.CanBeNonNullable") // All usages pass in nullable, to reduce duplicated logic.
	private fun checkEmptyEnv(component: Component, env: Env?, reporting: Reporting, issue: Issue) {
		if (env is Env.Explicit && env.map.isEmpty()) {
			reporting.report(issue, component) { "${it} should not have empty env." }
		}
	}

	private companion object {

		private val EMPTY_ENV_DESCRIPTION = """
			Dead code can lead to confusion.
			
			Empty environment variable listing is not necessary, that is the default.
			GitHub Actions treats empty env and missing env the same way,
			because the differently scoped env listings are additive.
			
			Remove the `env` section, if it is empty.
		""".trimIndent()

		private val NO_ENV_EXAMPLE = Example(
			explanation = "No env defined.",
			content = """
				on: push
				jobs:
				  example:
				    runs-on: ubuntu-latest
				    steps:
				      - run: echo "Example"
			""".trimIndent(),
		)

		val EmptyWorkflowEnv = Issue(
			id = "EmptyWorkflowEnv",
			title = "Workflow has empty env.",
			description = EMPTY_ENV_DESCRIPTION,
			compliant = listOf(
				NO_ENV_EXAMPLE,
			),
			nonCompliant = listOf(
				Example(
					explanation = "Empty env on workflow.",
					content = """
						on: push
						env: {}
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)

		val EmptyJobEnv = Issue(
			id = "EmptyJobEnv",
			title = "Job has empty env.",
			description = EMPTY_ENV_DESCRIPTION,
			compliant = listOf(
				NO_ENV_EXAMPLE,
			),
			nonCompliant = listOf(
				Example(
					explanation = "Empty env on job.",
					content = """
						on: push
						jobs:
						  example:
						    env: {}
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)

		val EmptyStepEnv = Issue(
			id = "EmptyStepEnv",
			title = "Step has empty env.",
			description = EMPTY_ENV_DESCRIPTION,
			compliant = listOf(
				NO_ENV_EXAMPLE,
			),
			nonCompliant = listOf(
				Example(
					explanation = "Empty env on step.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        env: {}
					""".trimIndent(),
				),
				Example(
					explanation = "Empty env on composite step.",
					path = "action.yml",
					content = """
						name: ""
						description: ""
						runs:
						  using: composite
						  steps:
						    - shell: bash
						      run: echo "Example"
						      env: {}
					""".trimIndent(),
				),
			),
		)
	}
}
