package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Component
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class DoubleCurlyIfRule : VisitorRule {

	override val issues: List<Issue> = listOf(DoubleCurlyIf)

	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		val condition = job.`if` ?: return
		validate(reporting, job, condition)
	}

	override fun visitStep(reporting: Reporting, step: Step) {
		super.visitStep(reporting, step)
		val condition = step.`if` ?: return
		validate(reporting, step, condition)
	}

	private fun validate(reporting: Reporting, target: Component, condition: String) {
		if (hasDoubleCurlyInside(condition)) {
			reporting.report(DoubleCurlyIf, target) { "${it} has nested or invalid double-curly-braces." }
		} else if (!isWrappedInDoubleCurly(condition)) {
			reporting.report(DoubleCurlyIf, target) { "${it} does not have double-curly-braces." }
		}
	}

	@Suppress("detekt.ClassOrdering") // Keep logic above Issue declaration for easy scrolling.
	private companion object {

		private fun isWrappedInDoubleCurly(condition: String): Boolean =
			condition.startsWith("\${{") && condition.endsWith("}}")

		private fun hasDoubleCurlyInside(condition: String): Boolean {
			val startCurly = condition.indexOf(string = "\${{")
			val endCurly = condition.indexOf("}}")
			return startCurly != -1 && startCurly != 0 || endCurly != -1 && endCurly != condition.length - 2
		}

		val DoubleCurlyIf = Issue(
			id = "DoubleCurlyIf",
			title = "if: is not wrapped in double-curly-braces.",
			description = """
				Omitting, or over-using the double-curly-braces (`${'$'}{{ }}`) can lead to unexpected behavior.
				
				While the [GitHub Actions Expressions documentation](https://docs.github.com/en/actions/learn-github-actions/expressions)
				has a note for `${'$'}{{ }}` being optional for `if:`s.
				The `if:` being an exception also has an exception,
				as [documented on conditionals](https://docs.github.com/en/actions/using-jobs/using-conditions-to-control-job-execution).
				
				The optionality listed above probably causes more problems than keystrokes it saves,
				and therefore it's strongly recommended to always wrap the full `if:` condition in `${'$'}{{ }}`.
				
				If you find the examples confusing (I definitely did), the more reason to always use it.
				Simple rule, consistent outcomes.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "`if:`s starting with a `!` must always be wrapped in double-curly-braces.",
					content = """
						on: push
						jobs:
						  example:
						    if: ${'$'}{{ ! startsWith(github.ref, 'refs/tags/') }}
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
				Example(
					explanation = "To avoid confusion, `if:` is fully wrapped in double-curly-braces.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        if: ${'$'}{{ steps.calculation.outputs.result == 'world' }}
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = """
						When `if:` starts with `!`, it's going to break the condition.
						A string value starting with `!` is reserved syntax in YAML.
					""".trimIndent(),
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        if: ! startsWith(github.ref, 'refs/tags/')
					""".trimIndent(),
				),
				Example(
					explanation = """
						Simple comparison is non-commutative due to YAML syntax.
						The first step in this example will work correctly,
						but as soon as the condition order is swapped around the YAML doesn't even parse:
						```log
						while parsing a block mapping
						 in reader, line 3, column 5:
						        if: 'bbb' == github.context.variable
						        ^
						expected <block end>, but found '<scalar>'
						 in reader, line 3, column 15:
						        if: 'bbb' == github.context.variable
						                  ^
						```
						This is very confusing as commutativity is one of the basics of boolean math in programming languages.
						Especially for a simple thing like equality.
						In case both of these are wrapped in double-curly-braces, the order doesn't matter, and it "just works".
					""".trimIndent(),
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        if: github.context.variable == 'example'
						#     - run: echo "Example"
						#       if: 'example' == github.context.variable
					""".trimIndent(),
				),
				Example(
					explanation = """
						The `if:` condition is wrapped in double-curly-braces, but only partially.
						Looking at the expression, it might be interpreted (by humans) as a valid string comparison,
						because the GitHub Actions Context variable is wrapped in an Expression as expected.
						
						However, this condition will **always** evaluate to `true`:
						The way to interpret this expression is as follows:
						
						 * Evaluate `steps.calculation.outputs.result` -> `'hello'`
						 * Substitute `'hello'` -> `if: hello == 'world'`
						 * Evaluate `"hello == 'world'"` -> `true`
						
						This last step might be surprising, but after substituting the expressions,
						GitHub Actions just leaves us with a YAML String.
						That string is then passed to `if`, but it's a non-empty string, which is truthy.
						
						To confirm this, you can run a workflow with step debugging turned on, and you'll see this:
						```log
						Evaluating: (success() && format('{0} == ''world''', steps.calculation.outputs.result))
						```
					""".trimIndent(),
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "result=hello" >> "${'$'}{GITHUB_OUTPUT}"
						        id: calculation
						      - run: echo "Example"
						        if: ${'$'}{{ steps.calculation.outputs.result }} == 'world'
					""".trimIndent(),
				),
				Example(
					explanation = """
						The `if:` condition is wrapped in double-curly-braces, but only partially.
						Looking at the expression, it might be interpreted (by humans) as a valid boolean expression,
						because the GitHub Actions Context variable accesses are wrapped in an Expression as expected.
						
						However, this condition will **always** evaluate to `true`.
						
						The way to interpret this expression is as follows:
						
						 * Evaluate first `${{ }}` -> for example `true`
						 * Evaluate second `${{ }}` -> for example `false`
						 * Substitute expressions -> `if: 'true && false'`
						 * Evaluate `'true && false'` -> `true`
						
						This last step might be surprising, but after substituting the expressions,
						GitHub Actions just leaves us with a YAML String.
						That string is then passed to `if`, but it's a non-empty string, which is truthy.
						
						To confirm this, you can run a workflow with step debugging turned on, and you'll see this:
						```log
						Evaluating: (success() && format('{0} && {1}', ..., ...))
						```
					""".trimIndent(),
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
						        if: ${'$'}{{ github.event.pull_request.additions > 10 }} && ${'$'}{{ github.event.pull_request.draft }}
					""".trimIndent(),
				),
			),
		)
	}
}
