package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class FailFastActionsRule : VisitorRule {

	override val issues: List<Issue> = listOf(
		FailFastUploadArtifact,
		FailFastPublishUnitTestResults,
		FailFastPeterEvansCreatePullRequest,
	)

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		super.visitUsesStep(reporting, step)
		when (step.uses.action) {
			"actions/upload-artifact" -> {
				val isSpecified = step.with.orEmpty().containsKey("if-no-files-found")
				if (!isSpecified) {
					reporting.report(FailFastUploadArtifact, step) {
						"${it} should have input `if-no-files-found: error`."
					}
				}
			}

			"EnricoMi/publish-unit-test-result-action" -> {
				val isSpecified = step.with.orEmpty().containsKey("action_fail_on_inconclusive")
				if (!isSpecified) {
					reporting.report(FailFastPublishUnitTestResults, step) {
						"${it} should have input `action_fail_on_inconclusive: true`."
					}
				}
			}

			"peter-evans/create-pull-request" -> {
				reporting.report(FailFastPeterEvansCreatePullRequest, step) {
					"Use `gh pr create` to open a PR instead of ${it}."
				}
			}
		}
	}

	private companion object {

		val FailFastUploadArtifact = Issue(
			id = "FailFastUploadArtifact",
			title = "`upload-artifact` should fail fast.",
			description = """
				`actions/upload-artifact` should be configured to fail the CI when no files are found.
				
				When the action is not configured to fail on missing files,
				the action step will be successful even when the artifact is not uploaded.
				
				There are no branch protection rules to ensure that an artifact is produced,
				so the only way to ensure that the workflows are correct,
				is by failing the action step when the artifact input files are missing.
				
				See the [`if-no-files-found` input declaration](https://github.com/actions/upload-artifact/blob/v4.3.1/action.yml#L11-L19).
				
				In case you're certain this if acceptable behavior,
				disable this by explicitly setting `if-no-files-found: warn` or `if-no-files-found: ignore`.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "`if-no-files-found` input is specified.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/upload-artifact@v4
						        with:
						          if-no-files-found: error
						          path: |
						            build/some/report/
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "`if-no-files-found` input is not declared, so it uses the default `warn` value.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/upload-artifact@v4
						        with:
						          path: |
						            build/some/report/
					""".trimIndent(),
				),
			),
		)

		val FailFastPublishUnitTestResults = Issue(
			id = "FailFastPublishUnitTestResults",
			title = "`publish-unit-test-result-action` should fail fast.",
			description = """
				`EnricoMi/publish-unit-test-result-action` should be configured to fail the CI when no test results are found.
				
				When the action is not configured to fail on inconclusive results,
				the action step will be successful even when the test results are missing.
				
				It will emit a warning:
				> Warning: Could not find any JUnit XML files for .../TEST-*.xml
				
				which is easy to miss. This means that the CI could be broken by PRs,
				even if the job is required on a branch protection rule.
				
				See the [`action_fail_on_inconclusive` input declaration](https://github.com/EnricoMi/publish-unit-test-result-action/blob/v2.14.0/action.yml#L40-L43).
				
				In case you're certain this if acceptable behavior,
				disable this by explicitly setting `action_fail_on_inconclusive: false`.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "`action_fail_on_inconclusive` input is specified.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: EnricoMi/publish-unit-test-result-action@v2
						        with:
						          action_fail_on_inconclusive: true
						          junit_files: |
						            **/build/**/TEST-*.xml
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "`action_fail_on_inconclusive` is not declared, so it uses the default `false` value.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: EnricoMi/publish-unit-test-result-action@v2
						        with:
						          junit_files: |
						            **/build/**/TEST-*.xml
					""".trimIndent(),
				),
			),
		)

		val FailFastPeterEvansCreatePullRequest = Issue(
			id = "FailFastPeterEvansCreatePullRequest",
			title = "`peter-evans/create-pull-request` has unsafe edge cases, use `gh pr create` instead.",
			description = """
				Action doesn't allow fast fail, and therefore black-listed.
				
				From its documentation:
				> If there are no changes (i.e. no diff exists with the checked-out base branch),
				> no pull request will be created and the action **exits silently**.
				> -- [README](https://github.com/peter-evans/create-pull-request#action-behaviour)
				
				This is error-prone: if the PR content generation **accidentally** breaks, there's no way to detect it.
				PR creation step just passes as if everything is all right.
				There are outputs from the action which could be checked for null/empty/undefined,
				but any user of this action needs to be aware of this.
				This is akin to [C's numeric return codes](https://www.tutorialspoint.com/cprogramming/c_error_handling.htm),
				the world has moved away from that approach.
				
				There's also confusion as seen [in the issue list](https://github.com/peter-evans/create-pull-request/issues?q=is%3Aissue+%22is+not+ahead+of+base%22+%22will+not+be+created%22).
				
				The only way to notice this is by checking the logs of the action:
				```log
				Branch 'to-create' is not ahead of base 'main' and will not be created
				```
				and this line is not even a warning.
				
				Without the ability to fail fast, this action is not fit for production usage.
				
				The recommended replacement is [`gh pr create`](https://cli.github.com/manual/gh_pr_create),
				which is a first party GitHub CLI tool with behaviors fit for the GitHub Actions environment.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Use the `gh` CLI to create a PR.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Create Pull Request"
						        env:
						          TITLE: Example
						          BODY: |
						            Example PR description
						            - Updated foo
						            - Removed bar
						        run: >
						          gh pr create
						          --title "${'$'}{TITLE}"
						          --body "${'$'}{BODY}"
						          --draft
						          --label "report"
						          --label "automated pr"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Using create-pull-request action.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - name: "Create Pull Request"
						        uses: peter-evans/create-pull-request@v6
						        with:
						          title: 'Example'
						          body: |
						            Example PR description
						            - Updated foo
						            - Removed bar
						          labels: |
						            report
						            automated pr
						          draft: true
					""".trimIndent(),
				),
			),
		)
	}
}
