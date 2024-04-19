package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.Component
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.ActionVisitor
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class ScriptInjectionRule : VisitorRule, WorkflowVisitor, ActionVisitor {

	override val issues: List<Issue> = listOf(ShellScriptInjection, JSScriptInjection)

	override fun visitWorkflowRunStep(reporting: Reporting, step: WorkflowStep.Run) {
		super.visitWorkflowRunStep(reporting, step)
		checkForShellInjection(reporting, step, step)
	}

	override fun visitActionRunStep(reporting: Reporting, step: ActionStep.Run) {
		super.visitActionRunStep(reporting, step)
		checkForShellInjection(reporting, step, step)
	}

	private fun checkForShellInjection(reporting: Reporting, step: Step.Run, target: Component) {
		if (step.run.contains("\${{")) {
			reporting.report(ShellScriptInjection, target) { "${it} shell script contains GitHub Expressions." }
		}
	}

	override fun visitWorkflowUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		super.visitWorkflowUsesStep(reporting, step)
		checkForJavascriptInjection(reporting, step, step)
	}

	override fun visitActionUsesStep(reporting: Reporting, step: ActionStep.Uses) {
		super.visitActionUsesStep(reporting, step)
		checkForJavascriptInjection(reporting, step, step)
	}

	private fun checkForJavascriptInjection(reporting: Reporting, step: Step.Uses, target: Component) {
		if (step.uses.action == "actions/github-script"
			// Assuming script is required: https://github.com/actions/github-script/blob/v7.0.1/action.yml#L8-L10
			&& step.with.orEmpty().getValue("script").contains("\${{")
		) {
			reporting.report(JSScriptInjection, target) { "${it} JavaScript contains GitHub Expressions." }
		}
	}

	private companion object {

		val ShellScriptInjection = Issue(
			id = "ShellScriptInjection",
			title = "Shell script vulnerable to script injection.",
			description = """
				Using `${'$'}{{ }}` in shell scripts is vulnerable to script injection.
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
						```log
						Warning: Quotation mark (") needs a pair.
						```
					""".trimIndent(),
				),
				Example(
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: cp -r "${'$'}{GITHUB_WORKSPACE}" "${'$'}{RUNNER_TEMP}"
					""".trimIndent(),
					explanation = """
						A note on GitHub variables vs contexts.
						There are a few examples where using `${'$'}{{ github.* }}` would result in an unsafe script,
						for example:
						```yaml
						- run: cp "${'$'}{{ github.workspace }}" "${'$'}{{ runner.temp }}"
						```
						
						Instead of introducing an `env:` section like this:
						```yaml
						- env:
						    WS: ${'$'}{{ github.workspace }}
						    RT: ${'$'}{{ runner.temp }}
						  run: cp -r "${'$'}{WS}" "${'$'}{RT}"
						```
						consider using the `${'$'}{GITHUB_*}` and `${'$'}{RUNNER_*}` environment variables as shown in the example.
						
						Compare:
						
						 * [`GITHUB_*` and `RUNNER_*` environment variables](https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables)
						 * [`github.*` context](https://docs.github.com/en/actions/learn-github-actions/contexts#github-context)
						 * [`runner.*` context](https://docs.github.com/en/actions/learn-github-actions/contexts#runner-context)
						
						---
						
						An exception to this might be when there's a project-specific path that is appended and used multiple times:
						```yaml
						- env:
						    WS: ${'$'}{{ github.workspace }}/some/thing
						    RT: ${'$'}{{ runner.temp }}/other/place
						  run: |
						    unzip "${'$'}{WS}/foo.zip" -d "${'$'}{RT}"
						    rm -rf "${'$'}{WS}"
						    mv "${'$'}{RT}" "${'$'}{WS}"
						```
					""".trimIndent(),
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
					""".trimIndent(),
				),
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
						          echo 'result={"add":"one","remove":["two","three"]}' >> "${'$'}{GITHUB_OUTPUT}"
						
						      - name: "Consume some output directly"
						        run: echo "${'$'}{{ steps.producer.outputs.result }}"
					""".trimIndent(),
					explanation = """
						Directly using the output in the shell script is vulnerable to script injection.
						
						In this example, the output is:
						```log
						{add:one,remove:[two,three]}
						```
						instead of
						```log
						{"add":"one","remove":["two","three"]}
						```
						because after resolving the `${'$'}{{ ... }}` expression, the script becomes:
						```shell
						echo "{"add":"one","remove":["two","three"]}"
						```
						which looks OK at first glance, but shells can actually understand it differently than expected
						(spaces added for clarity):
						```shell
						echo "{" add ":" one "," remove ":[" two "," three "]}"
						```
					""".trimIndent(),
				),
			),
		)

		val JSScriptInjection = Issue(
			id = "JSScriptInjection",
			title = "JavaScript vulnerable to script injection.",
			description = """
				Using `${'$'}{{ }}` in actions/github-script JavaScript is vulnerable to script injection.
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
					""".trimIndent(),
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
					""".trimIndent(),
				),
			),
		)
	}
}
