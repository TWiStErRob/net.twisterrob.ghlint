package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.Workflow
import net.twisterrob.ghlint.model.defaultShell
import net.twisterrob.ghlint.model.effectiveShell
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class DuplicateShellRule : VisitorRule {

	override val issues: List<Issue> = listOf(DuplicateShellOnSteps)

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		val steps = workflow.jobs.values.flatMap { (it as? Job.NormalJob)?.steps.orEmpty() }
		val explicitShells = steps.countExplicitShells()
		if (explicitShells.size == 1) {
			val (shell, count) = explicitShells.entries.single()
			if (workflow.defaultShell != null && shell != workflow.defaultShell) {
				reporting.report(DuplicateShellOnSteps, workflow) {
					"All (${count}) steps in ${it} override shell as `${shell}`, " +
							"change the default shell on the workflow from `${workflow.defaultShell}` to `${shell}`, " +
							"and remove shells from steps."
				}
			}
		}
	}

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		super.visitNormalJob(reporting, job)
		val explicitShells = job.steps.countExplicitShells()
		if (explicitShells.size == 1) {
			if (job.effectiveShell == null) {
				val (shell, count) = explicitShells.entries.single()
				if (count >= MAX_SHELLS_ON_STEPS) {
					reporting.report(DuplicateShellOnSteps, job) {
						"${it} has all (${count}) steps defining ${shell} shell, set default shell on job."
					}
				}
			} else if (job.defaultShell != null) {
				val (shell, count) = explicitShells.entries.single()
				if (shell != job.defaultShell) {
					reporting.report(DuplicateShellOnSteps, job) {
						"All (${count}) steps in ${it} override shell as `${shell}`, " +
								"change the default shell on the job from `${job.defaultShell}` to `${shell}`, " +
								"and remove shells from steps."
					}
				}
			}
		}
	}

	private companion object {

		private const val MAX_SHELLS_ON_STEPS = 2

		val DuplicateShellOnSteps = Issue(
			id = "DuplicateShellOnSteps",
			title = "Multiple steps have the shell defined.",
			description = """
				Multiple steps having a shell defined causes noise.
				Step shells should be uniform for each job, since each step is executed on the same runner.
				
				Relevant documentation:
				
				 * [`jobs.<job_id>.steps[*].shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsshell)
				 * [`jobs.<job_id>.defaults.run.shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_iddefaultsrun)
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Only one step has shell.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        shell: bash
					""".trimIndent(),
				),
				Example(
					explanation = "Default shell is defined for the job.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    defaults:
						      run:
						        shell: bash
						    steps:
						      - run: echo "Example"
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Multiple steps have shell.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        shell: bash
						      - run: echo "Example"
						        shell: bash
					""".trimIndent(),
				),
			),
		)
	}
}

private fun List<Step>.countExplicitShells(): Map<String, Int> =
	this
		.filterIsInstance<Step.Run>()
		.filter { it.shell != null }
		.groupingBy { it.shell ?: error("Just filtered it!") }
		.eachCount()
