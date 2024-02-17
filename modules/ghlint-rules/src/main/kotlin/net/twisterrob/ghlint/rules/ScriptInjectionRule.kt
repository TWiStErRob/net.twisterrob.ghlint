package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class ScriptInjectionRule : VisitorRule {

	override val issues: List<Issue> = listOf(ShellScriptInjection, JSScriptInjection)

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		if (step.run.contains("\${{")) {
			reporting.report(ShellScriptInjection, step) { "${it} shell script contains GitHub Expressions." }
		}
	}

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		super.visitUsesStep(reporting, step)
		if (step.uses.startsWith("actions/github-script@")
			// Assuming script is required: https://github.com/actions/github-script/blob/v7.0.1/action.yml#L8-L10
			&& step.with.orEmpty().getValue("script").contains("\${{")
		) {
			reporting.report(JSScriptInjection, step) { "${it} JavaScript contains GitHub Expressions." }
		}
	}

	private companion object {

		val ShellScriptInjection = Issue(
			id = "ShellScriptInjection",
			title = "Shell script vulnerable to script injection.",
			description = """
				Using ${'$'}{{ }} in shell scripts is vulnerable to script injection.
				Script injection is when a user can control part of the script, and can inject arbitrary code.
				In most cases this is a security vulnerability, but at the very least it's a bug.
				All user input must be sanitized before being executed as shell script.
				
				The simplest way to achieve this is using environment variables to pass data as inputs to `run:` scripts.
				Shells know how to handle them: `${'$'}{XXX}`.
				With environment variables data travels in memory, rather than becoming part of the executable code.
				
				References:
				 * [Understanding the risk of script injections](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions#understanding-the-risk-of-script-injections)
				 * [Stealing the job's `GITHUB_TOKEN`](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions#stealing-the-jobs-github_token)
			""".trimIndent(),
			compliant = listOf(
				Example(
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Produce some output"
						        id: producer
						        run: |
						          echo 'result=Warning: Quotation mark (") needs a pair.' >> "${'$'}{GITHUB_OUTPUT}"
						
						      - name: "Consume some output"
						        env:
						          RESULT: ${'$'}{{ steps.producer.outputs.result }}
						        run: echo "${'$'}{RESULT}"
					""".trimIndent(),
					explanation = """
						Capturing the input in an environment variable prevents shell injection.
						
						The output is as expected:
						```
						Warning: Quotation mark (") needs a pair.
						```
					""".trimIndent()
				),
			),
			nonCompliant = listOf(
				Example(
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Produce some output"
						        id: producer
						        run: |
						          echo 'result=Warning: Quotation mark (") needs a pair.' >> "${'$'}{GITHUB_OUTPUT}"
						
						      - name: "Consume some output directly"
						        run: echo "${'$'}{{ steps.producer.outputs.result }}"
					""".trimIndent(),
					explanation = """
						Directly using the output in the shell script is vulnerable to script injection.
						Depending on the actual contents of the result
						 * in the best case, the script fails,
						 * in normal cases, the output is just wrong,
						 * in the worst case, this could lead to arbitrary code execution.
						
						In this example, the output is:
						```log
						/home/runner/work/_temp/d3ddaaab-5e34-4eb3-be73-4e830012fe4e.sh: line 1: syntax error near unexpected token `)'
						Error: Process completed with exit code 2.
						```
						because after resolving the `${'$'}{{ ... }}` expression, the script becomes:
						```shell
						echo "Warning: Quotation mark (") needs a pair."
						```
						where
						```shell
						echo "Warning: Quotation mark ("
						```
						is correct, but the remaining code after is meaningless for shells: `) needs a pair."`.
					""".trimIndent()
				),
			),
		)

		val JSScriptInjection = Issue(
			id = "JSScriptInjection",
			title = "JavaScript vulnerable to script injection.",
			description = """
				Using ${'$'}{{ }} in actions/github-script JavaScript is vulnerable to script injection.
				Script injection is when a user can control part of the script, and can inject arbitrary code.
				In most cases this is a security vulnerability, but at the very least it's a bug.
				All user input must be sanitized before being executed as JavaScript code.
				
				The simplest way to achieve this is using environment variables to pass data as inputs to `script` code.
				Node know how to handle them: `process.env.XXX`.
				With environment variables data travels in memory, rather than becoming part of the executable code.
				
				References:
				 * [Understanding the risk of script injections](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions#understanding-the-risk-of-script-injections)
				 * [Stealing the job's `GITHUB_TOKEN`](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions#stealing-the-jobs-github_token)
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = """
						Script injection is impossible, because the script is a static constant.
						The input is provided via an Environment Variable, already typed as string.
					""".trimIndent(),
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Remove prefix from PR title."
						        uses: actions/github-script@v7
						        env:
						          PR_TITLE: ${'$'}{{ github.event.pull_request.title }}
						        with:
						          script: |
						            const title = process.env.PR_TITLE;
						            return title.replaceAll(/JIRA-\d+ /, "");
					""".trimIndent()
				),
			),
			nonCompliant = listOf(
				Example(
					content = """
						on: pull_request
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Remove prefix from PR title."
						        uses: actions/github-script@v7
						        with:
						          script: |
						            const title = "${'$'}{{ github.event.pull_request.title }}";
						            return title.replaceAll(/JIRA-\d+ /, "");
					""".trimIndent(),
					explanation = """
						Script injection breaks JavaScript execution.
						
						The actual `script` input depends on the Pull Request's actual title.
						In most cases, it'll be ok:
						```javascript
						const title = "JIRA-1234 Fix the thing";
						return title.replaceAll(/JIRA-\d+ /, "");
						```
						
						But then there are a lot of titles which just break the syntax:
						
						In the best case, it's a straight-up compilation error:
						```javascript
						const title = "JIRA-1234 Remove " from logs";
						return title.replaceAll(/JIRA-\d+ /, "");
						```
						but in the worst case, this can expose secrets or execute arbitrary code.
					""".trimIndent()
				),
			),
		)
	}
}
