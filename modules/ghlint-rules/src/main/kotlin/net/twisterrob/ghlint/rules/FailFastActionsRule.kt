package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class FailFastActionsRule : VisitorRule {

	override val issues: List<Issue> = listOf(FailFastUploadArtifact, FailFastPublishUnitTestResults)

	override fun visitUsesStep(reporting: Reporting, step: Step.Uses) {
		super.visitUsesStep(reporting, step)
		if (step.uses.startsWith("actions/upload-artifact@")) {
			val isSpecified = step.with.orEmpty().containsKey("if-no-files-found")
			if (!isSpecified) {
				reporting.report(FailFastUploadArtifact, step) {
					"${it} should have input `if-no-files-found: error`."
				}
			}
		}
		if (step.uses.startsWith("EnricoMi/publish-unit-test-result-action@")) {
			val isSpecified = step.with.orEmpty().containsKey("action_fail_on_inconclusive")
			if (!isSpecified) {
				reporting.report(FailFastPublishUnitTestResults, step) {
					"${it} should have input `action_fail_on_inconclusive: true`."
				}
			}
		}
	}

	internal companion object {

		val FailFastUploadArtifact = Issue(
			id = "FailFastUploadArtifact",
			title = "upload-artifact should fail fast.",
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
					""".trimIndent()
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
					""".trimIndent()
				),
			),
		)

		val FailFastPublishUnitTestResults = Issue(
			id = "FailFastPublishUnitTestResults",
			title = "publish-unit-test-result-action should fail fast.",
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
					""".trimIndent()
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
					""".trimIndent()
				),
			),
		)
	}
}
