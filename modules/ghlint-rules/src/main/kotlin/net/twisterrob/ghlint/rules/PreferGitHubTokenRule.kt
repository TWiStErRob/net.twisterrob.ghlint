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

public class PreferGitHubTokenRule : VisitorRule {

	override val issues: List<Issue> = listOf(PreferGitHubToken)

	override fun visitWorkflow(reporting: Reporting, workflow: Workflow) {
		super.visitWorkflow(reporting, workflow)
		workflow.env.check(reporting, "environment variable", workflow)
	}

	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		job.env.check(reporting, "environment variable", job)
	}

	override fun visitReusableWorkflowCallJob(reporting: Reporting, job: Job.ReusableWorkflowCallJob) {
		super.visitReusableWorkflowCallJob(reporting, job)
		job.with.check(reporting, "input", job)
		job.secrets.check(reporting, "secret", job)
	}

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		step.env.check(reporting, "environment variable", step)
	}

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		super.visitUsesStep(reporting, step)
		step.with.check(reporting, "input", step)
	}

	private fun Map<String, String>?.check(reporting: Reporting, type: String, target: Component) {
		secretReferences.forEach { (key, value) ->
			reporting.report(PreferGitHubToken, target) {
				"`${key}` ${type} in ${it} should use `github.token` in `${value}`."
			}
		}
	}

	private companion object {

		private val Map<String, String>?.secretReferences: Map<String, String>
			get() = orEmpty().filter { (_, value) -> value.contains("secrets.GITHUB_TOKEN") }

		val PreferGitHubToken = Issue(
			id = "PreferGitHubToken",
			title = "Prefer `github.token` instead of `secrets.GITHUB_TOKEN`.",
			description = """
				Mixing built-in `GITHUB_TOKEN` with repository/org-level secrets is confusing.
				
				Whenever the [`secrets` context](https://docs.github.com/en/actions/learn-github-actions/contexts#secrets-context)
				is used, the reference is expected to be defined in _Settings > Security > Secrets and variables > Actions_;
				*except*, when it's about `GITHUB_TOKEN`.
				
				`GITHUB_TOKEN` is very special in several aspects, a few more examples:
				
				 * [It's passed to forked workflow runs.](https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions#using-secrets-in-a-workflow)
				 * [It's accessible in actions, even when not passed in.](https://docs.github.com/en/actions/security-guides/automatic-token-authentication#using-the-github_token-in-a-workflow)
				 * [Actions don't have access to the `secrets` context](https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions#accessing-your-secrets),
				   but [they can access `github.token`](https://docs.github.com/en/actions/security-guides/automatic-token-authentication#:~:text=An%20action%20can%20access,action.). 
				
				To clarify this special case to the reader,
				it's recommended to use `github.token` instead of `secrets.GITHUB_TOKEN` everywhere.
				
				This will make the usages of `github.token` consistent across workflows and actions,
				resulting in better maintainability due to easier copy-paste-ability between them.
				
				In case of `gh` CLI, this preference will also help disambiguate between
				
				 * `GH_TOKEN` environment variable and `GITHUB_TOKEN` secret.
				 * `GITHUB_TOKEN` environment variable and `GITHUB_TOKEN` secret.
				
				See [gh environment](https://cli.github.com/manual/gh_help_environment) for more details.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "`github.token` is used.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: gh pr view
						        env:
						          GH_TOKEN: ${'$'}{{ github.token }}
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "`secrets.GITHUB_TOKEN` is used.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: gh pr view
						        env:
						          GH_TOKEN: ${'$'}{{ secrets.GITHUB_TOKEN }}
					""".trimIndent(),
				),
			),
		)
	}
}
