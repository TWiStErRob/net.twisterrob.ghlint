package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.ActionStep
import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.model.isCheckout
import net.twisterrob.ghlint.model.seesEnvVar
import net.twisterrob.ghlint.model.stepsBefore
import net.twisterrob.ghlint.model.usesGhCli
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.ActionVisitor
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class MissingGhRepoRule : VisitorRule, WorkflowVisitor, ActionVisitor {

	override val issues: List<Issue> = listOf(MissingGhRepo)

	override fun visitWorkflowRunStep(reporting: Reporting, step: WorkflowStep.Run) {
		super.visitWorkflowRunStep(reporting, step)
		if (step.usesGhCli() && !step.hasRepositoryContext) {
			reporting.report(MissingGhRepo, step) {
				"${it} should see `$REPO_VAR` environment variable or have a repository cloned."
			}
		}
	}

	// TODO In the future when we can look into composite actions, this can be revisited in some way.
	@Suppress("RedundantOverride")
	override fun visitActionRunStep(reporting: Reporting, step: ActionStep.Run) {
		super.visitActionRunStep(reporting, step)
		// Do not run on action steps, because they shouldn't be checking out code,
		// so we can assume there's a checkout step before somewhere outside.
	}

	private companion object {

		private const val REPO_VAR = "GH_REPO"

		private val WorkflowStep.Run.hasRepositoryContext: Boolean
			// TODO this doesn't take into account `--repo` and `-R` flags.
			// https://github.com/TWiStErRob/net.twisterrob.ghlint/issues/280
			get() = seesEnvVar(REPO_VAR) || stepsBefore.any(Step::isCheckout)

		val MissingGhRepo = Issue(
			id = "MissingGhRepo",
			title = "`GH_REPO` or checkout is required for using the `gh` CLI tool.",
			description = """
				Using the `gh` CLI tool requires a GitHub repository to be cloned or defined in an environment variable.
				
				> `GH_REPO`: specify the GitHub repository in the `[HOST/]OWNER/REPO` format
				> for commands that otherwise operate on a local repository.
				> -- [gh help environment](https://cli.github.com/manual/gh_help_environment)
				
				---
				
				For most commands there's also a `--repo` or `-R` flag that can be used to specify the repository.
				
				> `-R`, `--repo` `<[HOST/]OWNER/REPO>`
				> Select another repository using the `[HOST/]OWNER/REPO` format
				> -- [gh pr](https://cli.github.com/manual/gh_pr)
				
				This is not supported yet and will be reported as a violation:
				https://github.com/TWiStErRob/net.twisterrob.ghlint/issues/280
				
				The rationale behind this is that we're running the `gh` CLI tool in a GitHub Actions environment,
				which is usually coupled with a repository hosting the workflow.
				Since this is an implicit context, it's more natural to use `GH_REPO: ${'$'}{{ github.repository }}`.
				In rare cases when we need to operate on a foreign repository,
				we might use `--repo`, but we can still use the same `GH_REPO` approach.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "`GH_REPO` is defined.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
						      - run: gh pr list
						        env:
						          GH_TOKEN: ${'$'}{{ github.token }}
						          GH_REPO: ${'$'}{{ github.repository }}
					""".trimIndent(),
				),
				Example(
					explanation = "Code is cloned, so repo is known.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
						      - run: gh pr list
						        env:
						          GH_TOKEN: ${'$'}{{ github.token }}
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Repository context is not known.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: gh pr list
						        env:
						          GH_TOKEN: ${'$'}{{ github.token }}
					""".trimIndent(),
				),
			),
		)
	}
}
