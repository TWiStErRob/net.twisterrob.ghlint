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

private typealias InferRequiredPermissions = (step: WorkflowStep.Uses) -> Set<RequiredScope>

private data class RequiredScope(
	val scope: Scope,
	val reason: String,
)

public class RequiredPermissionsRule : VisitorRule, WorkflowVisitor {
	override val issues: List<Issue> = listOf(MissingRequiredActionPermissions)

	override fun visitWorkflowUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		super.visitWorkflowUsesStep(reporting, step)

		val definition = REQUIRED_PERMISSIONS_DEFINITIONS[step.uses.action] ?: return
		val expectedPermissions = definition(step)
		val effectivePermissions = step.parent.effectivePermissions ?: return
		val definedPermissions = effectivePermissions.effectiveScopes

		val remaining = expectedPermissions.filterNot { definedPermissions.contains(it.scope) }

		remaining.forEach { expected ->
			reporting.report(MissingRequiredActionPermissions, step) {
				"${it} requires `${expected.scope}` permission for `${step.uses.action}` to work: ${expected.reason}"
			}
		}
	}

	private companion object {

		private val REQUIRED_PERMISSIONS_DEFINITIONS: Map<String, InferRequiredPermissions> = mapOf(
			// https://github.com/actions/checkout/blob/main/action.yml
			"actions/checkout" to { step ->
				if (step.with.isUsingGitHubToken("token")) {
					setOf(
						RequiredScope(
								Scope(Permission.CONTENTS, Access.READ),
							"To read the repository contents during git clone/fetch.",
						)
					)
				} else {
					emptySet()
				}
			},
			// https://github.com/actions/stale/blob/main/action.yml
			"actions/stale" to { step ->
				if (step.with.isUsingGitHubToken("repo-token")) {
					// TODO support days-before-* inputs. (i.e. -1 would make these permissions NOT required.)
					val issues = RequiredScope(
						Scope(Permission.ISSUES, Access.WRITE),
						"To comment or close stale issues.",
					)
					val prs = RequiredScope(
						Scope(Permission.PULL_REQUESTS, Access.WRITE),
						"To comment or close stale PRs.",
					)
					val deleteBranch = if (step.with?.get("delete-branch") == "true") {
						RequiredScope(
								Scope(Permission.CONTENTS, Access.WRITE),
								"To delete HEAD branches when closing PRs.",
						)
					} else {
						null
					}
					setOf(issues, prs) + listOfNotNull(deleteBranch)
				} else {
					emptySet()
				}
			},
			// https://github.com/8BitJonny/gh-get-current-pr/blob/master/action.yml
			"8BitJonny/gh-get-current-pr" to { step ->
				if (step.with.isUsingGitHubToken("repo-token")) {
					setOf(
						RequiredScope(
								Scope(Permission.PULL_REQUESTS, Access.READ),
							"To get the current PR.",
						)
					)
				} else {
					emptySet()
				}
			}
		)

		@Suppress("detekt.UnusedPrivateProperty") // To have a clean build, TODO remove before merging.
		private val REQUIRED_PERMISSIONS_OLD: Map<String, Set<Scope>> = mapOf(
			// Permissions are only required if `token` is not defined, or it's using github.token explicitly.
			"actions/deploy-pages" to setOf(
				// https://github.com/actions/deploy-pages/blob/main/action.yml
				// Only when `token` is not defined explicitly, or it's using github.token explicitly.
				Scope(Permission.PAGES, Access.WRITE), // To deploy to GitHub Pages.
				Scope(
					Permission.ID_TOKEN,
					Access.WRITE
				), // To verify the deployment originates from an appropriate source.
			),
			"github/codeql-action/upload-sarif" to setOf(
				// https://github.com/github/codeql-action/blob/main/upload-sarif/action.yml
				// Only when `github_token` is not defined, or it's using github.token explicitly.
				Scope(Permission.SECURITY_EVENTS, Access.WRITE), // To upload SARIF files.
				// Only in private repositories / internal organizations.
				Scope(Permission.ACTIONS, Access.WRITE),
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

/**
 * `github.token` is defined by default in many actions, however their naming varies significantly.
 *
 * This function assumes that the [inputKey] is defined similar to this:
 * ```yaml
 * inputs:
 *   my-token:
 *     default: ${{ github.token }}
 * ```
 * ```yaml
 * uses: my-action
 * with:
 *   #my-token: ${{ github.token }} # Default, no need to list.
 * ```
 * It is possible that the user defined a custom, but default token, those should be all equivalent:
 *
 * ```yaml
 * uses: my-action
 * with:
 *   my-token: ${{ github.token }}
 * ```
 * ```yaml
 * uses: my-action
 * with:
 *   my-token: ${{ secrets.GITHUB_TOKEN }}
 * ```
 */
private fun Map<String, String>?.isUsingGitHubToken(inputKey: String): Boolean {
	val token = this?.get(inputKey)
	return token == null || GITHUB_TOKEN_REGEX.matches(token)
}

private val GITHUB_TOKEN_REGEX = Regex("""^\s*${"\\$"}\{\{\s*(github\.token|secrets.GITHUB_TOKEN)\s*\}\}\s*$""")
