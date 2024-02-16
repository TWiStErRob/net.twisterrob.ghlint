package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class MissingJobTimeoutRule : VisitorRule {

	override val issues: List<Issue> = listOf(MissingJobTimeout)

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		super.visitNormalJob(reporting, job)
		if (job.timeoutMinutes == null) {
			reporting.report(MissingJobTimeout, job) { "${it} is missing `timeout-minutes`." }
		}
	}

	internal companion object {

		val MissingJobTimeout = Issue(
			id = "MissingJobTimeout",
			title = "Job is missing a timeout.",
			description = """
				Timeouts are important to prevent stuck jobs from blocking the workflow.
				
				The [default value](https://docs.github.com/en/actions/learn-github-actions/workflow-syntax-for-github-actions#jobsjob_idtimeout-minutes)
				is 360 minutes, which means 6 hours.
				This is usually too long for most jobs, and should be set explicitly to a lower value.
				
				Fun fact: 360 minutes is actually a hard limit on the job length, and not a friendly default.
				
				This should save resources:
				 * You'll get an error faster in case something is stuck.
				 * You'll get less [billed minutes](https://docs.github.com/en/billing/managing-billing-for-github-actions/about-billing-for-github-actions#minute-multipliers) because of used minutes.
				   Especially important on macOS (10x) runners.
				 * Other jobs will be able to run due to [concurrency limits](https://docs.github.com/en/actions/learn-github-actions/usage-limits-billing-and-administration#usage-limits).
				 * You'll contribute to a greener planet due to less data-center usage.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Timeout is declared, job will be cancelled early.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    timeout-minutes: 1
						    steps:
						      - run: while true; do echo "Infinite"; done
					""".trimIndent()
				),
				Example(
					explanation = """
						An additional recommendation for better developer experience:
						Set a timeout on the longest-running steps inside a job.
						
						When running integration tests which are known to complete in 30 minutes,
						but are sometimes hanging or get stuck, it's useful to set the job timeout to 35 minutes,
						and the step timeout to 30 minutes.
						This will ensure that there's enough time for setup and teardown,
						including uploading artifact reports for a partial test execution after a cancelled test run.
					""".trimIndent(),
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    timeout-minutes: 35
						    steps:
						      - uses: actions/checkout@v4
						
						      - run: ./scripts/integration-tests.sh
						        timeout-minutes: 30
						
						      - uses: actions/upload-artifact@v4
						        if: success() || failure()
						        with:
						          path: modules/**/test-reports
					""".trimIndent()
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Timeout is missing, this job will run for 6 hours.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: while true; do echo "Infinite"; done
					""".trimIndent()
				),
			),
		)
	}
}
