package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class MissingGhTokenRule : VisitorRule, WorkflowVisitor {

	override val issues: List<Issue> = listOf(MissingGhToken)

	override fun visitWorkflowRunStep(reporting: Reporting, step: WorkflowStep.Run) {
		super.visitWorkflowRunStep(reporting, step)
		if (usesGhCli(step.run)) {
			val hasGhToken = step.env.hasTokenVar || step.parent.env.hasTokenVar || step.parent.parent.env.hasTokenVar
			if (!hasGhToken) {
				reporting.report(MissingGhToken, step) { "${it} should see `${TOKEN_ENV_VAR}` environment variable." }
			}
		}
	}

	override fun visitWorkflowUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		super.visitWorkflowUsesStep(reporting, step)
		// TODO check if referenced composite action uses gh cli without GH_TOKEN
	}

	private fun usesGhCli(script: String): Boolean =
		script.contains(GH_CLI_START_OF_LINE)
				|| script.contains(GH_CLI_EMBEDDED)
				|| script.contains(GH_CLI_PIPE_CONDITIONAL)

	private companion object {

		private const val TOKEN_ENV_VAR = "GH_TOKEN"
		private val GH_CLI_START_OF_LINE = Regex("""^\s*gh\s+""", RegexOption.MULTILINE)
		private val GH_CLI_PIPE_CONDITIONAL = Regex("""(&&|\|\||\|)\s*gh\s+""")
		private val GH_CLI_EMBEDDED = Regex("""\$\(\s*gh\s+""")

		private val Map<String, String>?.hasTokenVar: Boolean
			get() = this.orEmpty().containsKey(TOKEN_ENV_VAR)

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
	}
}
