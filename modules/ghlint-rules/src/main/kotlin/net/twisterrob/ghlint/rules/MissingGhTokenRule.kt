package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.Component
import net.twisterrob.ghlint.model.Env
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.model.asMap
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.ActionVisitor
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class MissingGhTokenRule : VisitorRule, WorkflowVisitor, ActionVisitor {

	override val issues: List<Issue> = listOf(MissingGhToken, MissingGhHost)

	override fun visitWorkflowRunStep(reporting: Reporting, step: WorkflowStep.Run) {
		super.visitWorkflowRunStep(reporting, step)
		visitRunStep(reporting, step)
	}

	override fun visitActionRunStep(reporting: Reporting, step: ActionStep.Run) {
		super.visitActionRunStep(reporting, step)
		visitRunStep(reporting, step)
	}

	private fun <T> visitRunStep(reporting: Reporting, step: T) where T : Step.Run, T : Component {
		if (step.run.usesGhCli() && !step.hasEnvVar(TOKEN_VAR)) {
			when (step.hasEnvVar(ENTERPRISE_TOKEN_VAR) to step.hasEnvVar(ENTERPRISE_HOST_VAR)) {
				false to false -> {
					reporting.report(MissingGhToken, step) {
						"${it} should see `${TOKEN_VAR}` environment variable."
					}
				}

				true to false -> {
					reporting.report(MissingGhHost, step) {
						"${it} should see `${ENTERPRISE_HOST_VAR}` environment variable when using `${ENTERPRISE_TOKEN_VAR}`."
					}
				}

				false to true -> {
					reporting.report(MissingGhToken, step) {
						"${it} should see `${ENTERPRISE_TOKEN_VAR}` environment variable when using `${ENTERPRISE_HOST_VAR}`."
					}
				}
			}
		}
	}

	override fun visitWorkflowUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		super.visitWorkflowUsesStep(reporting, step)
		// TODO check if referenced composite action uses gh cli without GH_TOKEN
	}

	private fun String.usesGhCli(): Boolean =
		GH_CLI_START_OF_LINE in this || GH_CLI_EMBEDDED in this || GH_CLI_PIPE_CONDITIONAL in this

	private fun Step.Run.hasEnvVar(s: String): Boolean =
		when (this) {
			is WorkflowStep.Run ->
				this.env.hasVariable(s)
						|| this.parent.env.hasVariable(s)
						|| this.parent.parent.env.hasVariable(s)

			is ActionStep.Run ->
				this.env.hasVariable(s)
		}

	private fun Env?.hasVariable(varName: String): Boolean =
		this.asMap().containsKey(varName)

	private companion object {

		private const val TOKEN_VAR = "GH_TOKEN"
		private const val ENTERPRISE_TOKEN_VAR = "GH_ENTERPRISE_TOKEN"
		private const val ENTERPRISE_HOST_VAR = "GH_HOST"

		private val GH_CLI_START_OF_LINE = Regex("""^\s*gh\s+""", RegexOption.MULTILINE)
		private val GH_CLI_PIPE_CONDITIONAL = Regex("""(&&|\|\||\|)\s*gh\s+""")
		private val GH_CLI_EMBEDDED = Regex("""\$\(\s*gh\s+""")

		val MissingGhToken = Issue(
			id = "MissingGhToken",
			title = "`GH_TOKEN` is required for using the `gh` CLI tool.",
			description = """
				Using the `gh` CLI tool requires a GitHub token to be set.
				
				> GitHub CLI is preinstalled on all GitHub-hosted runners.
				> For each step that uses GitHub CLI,
				> you must set an environment variable called GH_TOKEN to a token with the required scopes.
				> -- [Using GitHub CLI in workflows](https://docs.github.com/en/actions/using-workflows/using-github-cli-in-workflows)
				
				Usually this token will be:
				```yaml
				env:
				  GH_TOKEN: ${'$'}{{ github.token }}
				```
				and the "required scopes" can be defined in the `permissions:` field of the job.
				
				Note: for GitHub Enterprise users, the token is named `GH_ENTERPRISE_TOKEN`,
				and it also requires `GH_HOST` to be set at the same time, see [`MissingGhHost`](MissingGhHost.md).
				
				References:
				
				 * [GitHub CLI Manual](https://cli.github.com/manual/#configuration)
				 * [Using GitHub CLI in workflows](https://docs.github.com/en/actions/using-workflows/using-github-cli-in-workflows)
				 * [gh help environment](https://cli.github.com/manual/gh_help_environment)
				
				---
				
				Note: it's possible to set both `GH_TOKEN` and `GITHUB_TOKEN` as environment variables,
				but to reduce confusion between
				
				 * `GITHUB_TOKEN` environment variables used by `gh` CLI
				 * `GITHUB_TOKEN` secret automatically defined by GitHub Actions
				
				it's recommended to always use `GH_TOKEN` and `${'$'}{{ github.token }}`, see [`PreferGitHubToken`](PreferGitHubToken.md).
				
				Using the `GH_` prefix also helps to understand that the variable is for the `gh` CLI.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "`GH_TOKEN` is defined.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
						      - run: gh pr view
						        env:
						          GH_TOKEN: ${'$'}{{ github.token }}
					""".trimIndent(),
				),
				Example(
					explanation = "`GH_ENTERPRISE_TOKEN` is defined.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
						      - run: gh pr view
						        env:
						          GH_ENTERPRISE_TOKEN: ${'$'}{{ github.token }}
						          GH_HOST: github.example.com
					""".trimIndent(),
				),
				Example(
					explanation = "`GH_TOKEN` is defined.",
					path = "action.yml",
					content = """
						name: Test
						description: Test
						inputs:
						  token:
						    description: 'GitHub token to authenticate to GitHub APIs.'
						    default: ${'$'}{{ github.token }}
						runs:
						  using: composite
						  steps:
						    - uses: actions/checkout@v4
						    - run: gh pr view
						      shell: bash
						      env:
						        GH_TOKEN: ${'$'}{{ inputs.token }}
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = """
						`GH_TOKEN` is not defined, command will fail with:
						```log
						gh: To use GitHub CLI in a GitHub Actions workflow, set the GH_TOKEN environment variable. Example:
						  env:
						    GH_TOKEN: ${'$'}{{ github.token }}
						Error: Process completed with exit code 4.
						```
					""".trimIndent(),
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: gh pr view
					""".trimIndent(),
				),
			),
		)

		val MissingGhHost = Issue(
			id = "MissingGhHost",
			title = "`GH_HOST` is required for using the `gh` CLI tool with `GH_ENTERPRISE_TOKEN`.",
			description = """
				Using the `gh` CLI tool requires a GitHub token to be set,
				and for Enterprise tokens, the GitHub Host must be also set.
				
				> `GH_ENTERPRISE_TOKEN`: an authentication token for API requests to GitHub Enterprise.
				> When setting this, also set `GH_HOST`.
				> `GH_HOST`: specify the GitHub hostname for commands that would otherwise assume the "github.com" host [...].
				> When setting this, also set `GH_ENTERPRISE_TOKEN`.
				> -- [gh help environment](https://cli.github.com/manual/gh_help_environment)
				
				References:
				
				 * [GitHub CLI Manual](https://cli.github.com/manual/#github-enterprise)
				 * [Using GitHub CLI in workflows](https://docs.github.com/en/actions/using-workflows/using-github-cli-in-workflows)
				 * [gh help environment](https://cli.github.com/manual/gh_help_environment)
				
				Note: it might be tempting to use `GH_HOST: ${'$'}{{ github.server_url }}`,
				but `gh` can't handle the protocol part of the URL.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "`GH_HOST` is defined.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
						      - run: gh pr view
						        env:
						          GH_ENTERPRISE_TOKEN: ${'$'}{{ github.token }}
						          GH_HOST: github.example.com
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "`GH_HOST` is not defined.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
						      - run: gh pr view
						        env:
						          GH_ENTERPRISE_TOKEN: ${'$'}{{ github.token }}
					""".trimIndent(),
				),
			),
		)
	}
}
