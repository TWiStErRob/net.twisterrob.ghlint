package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import net.twisterrob.ghlint.model.Scope
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.model.effectivePermissions
import net.twisterrob.ghlint.model.effectiveScopes
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

private data class RequiredPermissionsDefinition(val resolve: (WorkflowStep.Uses) -> Set<Scope>, val reason: String)

public class RequiredPermissionsRule : VisitorRule, WorkflowVisitor {
	override val issues: List<Issue> = listOf(MissingRequiredActionPermissions)

	override fun visitWorkflowUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		super.visitWorkflowUsesStep(reporting, step)

		val expectedPermissions = REQUIRED_PERMISSIONS_DEFINITIONS[step.uses.action]?.resolve?.invoke(step) ?: return
		val effectivePermissions = step.parent.effectivePermissions ?: return
		val definedPermissions = effectivePermissions.effectiveScopes

		val remaining = expectedPermissions - definedPermissions

		remaining.forEach { expected ->
			reporting.report(MissingRequiredActionPermissions, step) {
				"${it} requires `${expected}` permission for `${step.uses.action}` to work."
			}
		}
	}

	private companion object {

		private val REQUIRED_PERMISSIONS_DEFINITIONS: Map<String, RequiredPermissionsDefinition> = mapOf(
				"actions/checkout" to RequiredPermissionsDefinition(
						resolve = {
							val token = it.with?.get("token")
							if (
								token != null &&
								token != "\${{ github.token }}" &&
								token != "\${{ secrets.GITHUB_TOKEN }}"
							) {
								// Permissions are suppressed if a custom PAT is defined explicitly.
								emptySet()
							} else {
								setOf(Scope(Permission.CONTENTS, Access.READ))
							}
						},
						reason = "To read the repository contents during git clone/fetch."
				),
		)

		/**
		 * `github.token` is defined by default in many actions.
		 * Explicit `${{ github.token }}` / `${{ secrets.GITHUB_TOKEN }}` should be handled for these.
		 */
		private val REQUIRED_PERMISSIONS_OLD: Map<String, Set<Scope>> = mapOf(
				// Permissions are only required if `repo-token` is not defined, or it's using github.token explicitly.
				"actions/stale" to setOf(
						// https://github.com/actions/stale/blob/main/action.yml
						// Only when delete-branch == true, default is false.
						Scope(Permission.CONTENTS, Access.WRITE), // To delete HEAD branches when closing PRs.
						// These are required, unless repo-token is a secret.
						Scope(Permission.ISSUES, Access.WRITE),
						Scope(Permission.PULL_REQUESTS, Access.WRITE),
				),
				// Permissions are only required if `token` is not defined, or it's using github.token explicitly.
				"actions/deploy-pages" to setOf(
						// https://github.com/actions/deploy-pages/blob/main/action.yml
						// Only when `token` is not defined explicitly, or it's using github.token explicitly.
						Scope(Permission.PAGES, Access.WRITE), // To deploy to GitHub Pages.
						Scope(Permission.ID_TOKEN, Access.WRITE), // To verify the deployment originates from an appropriate source.
				),
				"github/codeql-action/upload-sarif" to setOf(
						// https://github.com/github/codeql-action/blob/main/upload-sarif/action.yml
						// Only when `github_token` is not defined, or it's using github.token explicitly.
						Scope(Permission.SECURITY_EVENTS, Access.WRITE), // To upload SARIF files.
						// Only in private repositories / internal organizations.
						Scope(Permission.ACTIONS, Access.WRITE),
				),
				"8BitJonny/gh-get-current-pr" to setOf(
						// https://github.com/8BitJonny/gh-get-current-pr/blob/master/action.yml
						// Only when `github-token` is not defined, or it's using github.token explicitly.
						Scope(Permission.PULL_REQUESTS, Access.READ), // To get the current PR.
				),
				// Permissions are only required if `github_token` is not defined, or it's using github.token explicitly.
				"EnricoMi/publish-unit-test-result-action" to setOf(
						// https://github.com/EnricoMi/publish-unit-test-result-action/blob/master/action.yml
						// Only when check_run == true, or not listed as default is true.
						Scope(Permission.CHECKS, Access.WRITE), // To publish check runs.
						// Only when comment_mode != off.
						// (i.e. always, changes, changes in failures, changes in errors, failures, errors; default is always)
						Scope(Permission.PULL_REQUESTS, Access.WRITE), // To comment on PRs.
						// Only in private repos:
						Scope(Permission.ISSUES, Access.READ),
						Scope(Permission.CONTENTS, Access.READ),
				),
		)

		val MissingRequiredActionPermissions = Issue(
				id = "MissingRequiredActionPermissions",
				title = "Required permissions are not declared for action.",
				description = """
				Certain GitHub Actions require specific permissions to be specified for the GITHUB_TOKEN to work correctly.
				
				For example, the `actions/checkout` action requires `contents: read` permission to be able to
				check out the repository.
				
				References:
				
				* [Documentation of `GITHUB_TOKEN` permissions](https://docs.github.com/en/actions/security-guides/automatic-token-authentication#modifying-the-permissions-for-the-github_token)
				* [List of Available permissions](https://docs.github.com/en/actions/security-guides/automatic-token-authentication#permissions-for-the-github_token)
				* [Syntax for workflow-level permissions](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#permissions)
				* [Syntax for job-level permissions](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idpermissions)
			""".trimIndent(),
				compliant = listOf(
						Example(
								explanation = "Required permissions are explicitly declared on the job.",
								content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    permissions:
						      contents: read
						    steps:
						      - uses: actions/checkout@v4
					""".trimIndent(),
						),
						Example(
								explanation = "Required permissions are explicitly declared on the workflow.",
								content = """
						on: push
						permissions:
						  contents: read
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
					""".trimIndent(),
						),
				),
				nonCompliant = listOf(
						Example(
								explanation = "Required permissions are not declared on the job or workflow.",
								content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    permissions:
						      pull-requests: write
						    steps:
						      - uses: actions/checkout@v4
					""".trimIndent(),
						),
				),
		)
	}
}
