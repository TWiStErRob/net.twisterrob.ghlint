package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Component
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class EmptyEnvRule : VisitorRule {

	override val issues: List<Issue> = listOf(EmptyWorkflowEnv, EmptyJobEnv, EmptyStepEnv)

	@Suppress("detekt.NamedArguments")
	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		checkEmptyEnv(workflow, workflow.env, reporting, EmptyWorkflowEnv)
	}

	@Suppress("detekt.NamedArguments")
	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		checkEmptyEnv(job, job.env, reporting, EmptyJobEnv)
	}

	@Suppress("detekt.NamedArguments")
	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		checkEmptyEnv(step, step.env, reporting, EmptyStepEnv)
	}

	@Suppress("detekt.CanBeNonNullable") // All usages pass in nullable, to reduce duplicated logic.
	private fun checkEmptyEnv(component: Component, env: Map<String, String>?, reporting: Reporting, issue: Issue) {
		if (env != null && env.isEmpty()) {
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
			),
		)
	}
}
