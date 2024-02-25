package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.defaultShell
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.toTarget

public class RedundantShellRule : VisitorRule {

	override val issues: List<Issue> = listOf(RedundantDefaultShell, RedundantShell)

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		super.visitNormalJob(reporting, job)
		val workflow = job.parent
		val myShell = job.defaultShell
		val globalShell = workflow.defaultShell
		if (globalShell != null && myShell == globalShell) {
			reporting.report(RedundantDefaultShell, job) {
				"Both ${it} and ${workflow.toTarget()} has `${myShell}` shell as default, one of them can be removed."
			}
		}
	}

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		val myShell = step.shell
		val (globalLocation, globalShell) = when {
			step.parent.defaultShell != null -> step.parent to step.parent.defaultShell
			step.parent.parent.defaultShell != null -> step.parent.parent to step.parent.parent.defaultShell
			else -> null to null
		}
		if (globalLocation != null && myShell != null && globalShell == myShell) {
			reporting.report(RedundantShell, step) {
				"Both ${globalLocation.toTarget()} and ${it} has `${myShell}` shell, the step's shell can be removed."
			}
		}
	}

	private companion object {

		val RedundantDefaultShell = Issue(
			id = "RedundantDefaultShell",
			title = "Same default shell is defined both on job and workflow.",
			description = """
				Duplication can lead to confusion.
				
				The default `shell:` can be specified on 2 levels, and the lowest wins:
				
				 * [`defaults.run.shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#defaultsrun)
				 * [`jobs.<job_id>.defaults.run.shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_iddefaultsrun)
				
				This means that when the workflow has the shell defined, the job's definition is not necessary.
				
				It is, however, recommended that the shell is defined on a per-job basis,
				because each job can have different runners.
				
				This results in
				
				 * explicit definitions:
				   each job has their shells defined.
				 * better locality:
				   each job has their shell defined closer to usage.
				 * clear separation:
				   `runs-on` an `defaults.run.shell` are on the same level.
				 * better copy-paste-ability:
				   when a job is copied or moved, it can't break because of changed or missing shell.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Default shell is defined on workflow.",
					content = """
						on: push
						defaults:
						  run:
						    shell: bash
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
				Example(
					explanation = "Default shell is defined on job.",
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
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Same default shell is defined on both workflow and job.",
					content = """
						on: push
						defaults:
						  run:
						    shell: bash
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    defaults:
						      run:
						        shell: bash
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)

		val RedundantShell = Issue(
			id = "RedundantShell",
			title = "Same shell is defined both on step and globally.",
			description = """
				Duplication can lead to confusion.
				
				The `shell:` can be specified on 3 levels, and the lowest wins:
				
				 * [`defaults.run.shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#defaultsrun)
				 * [`jobs.<job_id>.defaults.run.shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_iddefaultsrun)
				 * [`jobs.<job_id>.steps[*].shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsshell)
				
				This means that when the workflow or job has the shell defined, the steps' definitions are not necessary.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Default shell is defined on job.",
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
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Default shell is defined on job, but steps also repeat it.",
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
						        shell: bash
					""".trimIndent(),
				),
				Example(
					explanation = "Default shell is defined on workflow, but steps also repeat it.",
					content = """
						on: push
						defaults:
						  run:
						    shell: bash
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        shell: bash
					""".trimIndent(),
				),
			),
		)
	}
}
