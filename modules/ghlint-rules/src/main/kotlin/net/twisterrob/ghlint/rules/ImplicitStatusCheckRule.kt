package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Component
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.WorkflowVisitor
import net.twisterrob.ghlint.rule.report

public class ImplicitStatusCheckRule : VisitorRule, WorkflowVisitor {

	override val issues: List<Issue> = listOf(NeverUseAlways, NegativeStatusCheck)

	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		job.`if`?.let { validate(reporting, job, it) }
	}

	override fun visitStep(reporting: Reporting, step: Step) {
		super.visitStep(reporting, step)
		step.`if`?.let { validate(reporting, step, it) }
	}

	private fun validate(reporting: Reporting, target: Component, condition: String) {
		if (condition.contains("always()")) {
			reporting.report(NeverUseAlways, target) { "${it} uses the always() condition." }
		}
		if (condition.contains(NEGATIVE_CONDITION)) {
			reporting.report(NegativeStatusCheck, target) { "${it} uses a negative condition." }
		}
	}

	private companion object {

		private val NEGATIVE_CONDITION = Regex("""!\s*(always|success|failure|cancelled)\(\)""")

		val NeverUseAlways = Issue(
			id = "NeverUseAlways",
			title = "Using `always()` is discouraged.",
			description = """
				always() does not mean what you might think it means.
				
				```kotlin
				always() == success() || failure() || cancelled()
				```
				
				Most of the time, we just want to use `if: success() || failure()`,
				for example to upload test reports, when tests failed.
				
				Implying `cancelled()` via `always()` is risky when the step affects something external.
				If someone manually cancels a workflow run, they explicitly expressed they don't want its effects to happen,
				but `always()` will still execute the steps.
				
				References:
				
				 * [Documentation](https://docs.github.com/en/actions/learn-github-actions/expressions#status-check-functions)
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Step is explicitly defining to run on failure in addition to the default `success()`.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Something that could fail."
						
						      - uses: actions/upload-artifact@v0
						        if: ${'$'}{{ success() || failure() }}
					""".trimIndent(),
				),
				Example(
					explanation = "Step is well-specified when to execute with `if:`.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Something that could fail."
						
						      - uses: actions/upload-artifact@v0
						        if: ${'$'}{{ success() || failure() || cancelled() }}
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "`always()` used in an `if:` condition.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Something that could fail."
						
						      - uses: actions/upload-artifact@v0
						        if: ${'$'}{{ always() }}
					""".trimIndent(),
				),
			),
		)

		val NegativeStatusCheck = Issue(
			id = "NegativeStatusCheck",
			title = "Use positive conditions.",
			description = """
				Using a negated status check function is confusing.
				Being explicit helps in understanding the intent of the condition.
				
				If someone has read the documentation, they might understand what `!failure()` actually means,
				but being explicit comes at almost no cost and helps everyone immediately understand the intent:
				```yaml
				if: ${'$'}{{ success() || cancelled() }}
				```
				
				_Aside: In the unlikely event that GitHub introduces a new status check function,
				half of the negative usages will get invalid.
				You never know if your condition will be in the right half._
				
				References:
				
				 * [Documentation](https://docs.github.com/en/actions/learn-github-actions/expressions#status-check-functions)
				
				---
				
				Note the documentation recommends:
				> If you want to run a job or step regardless of its success or failure,
				> use the recommended alternative: `if: ${'$'}{{ !cancelled() }}`
				> -- [Documentation](https://docs.github.com/en/actions/learn-github-actions/expressions#status-check-functions)
				
				but I strongly believe if you want to "run a job or step regardless of its success or failure",
				use `if: ${'$'}{{ success() || failure() }}`, it's much clearer, isn't it?
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "`if:` condition is explicitly stating the statuses to run on.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Something that could fail."
						
						      - uses: actions/upload-artifact@v0
						        if: ${'$'}{{ success() || failure() }}
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "`if:` condition uses `!` to negate a status check function.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Something that could fail."
						
						      - uses: actions/upload-artifact@v0
						        if: ${'$'}{{ !cancelled() }}
					""".trimIndent(),
				),
			),
		)
	}
}
