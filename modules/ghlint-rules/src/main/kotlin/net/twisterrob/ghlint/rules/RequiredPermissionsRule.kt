package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.model.effectivePermissions
import net.twisterrob.ghlint.model.effectiveScopes
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor
import net.twisterrob.ghlint.rules.permissions.requirements.ActionsCheckoutPermissions
import net.twisterrob.ghlint.rules.permissions.requirements.ActionsStalePermissions
import net.twisterrob.ghlint.rules.permissions.requirements.EightBitJohhnyGetCurrentPrPermissions
import net.twisterrob.ghlint.rules.permissions.InferRequiredPermissions

public class RequiredPermissionsRule : VisitorRule, WorkflowVisitor {
	override val issues: List<Issue> = listOf(MissingRequiredActionPermissions)

	override fun visitWorkflowUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		super.visitWorkflowUsesStep(reporting, step)

		val definition = REQUIRED_PERMISSIONS_DEFINITIONS[step.uses.action] ?: return
		val expectedPermissions = definition.infer(step)
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

		private val REQUIRED_PERMISSIONS_DEFINITIONS: Map<String, InferRequiredPermissions> = listOf(
			ActionsCheckoutPermissions,
			ActionsStalePermissions,
			EightBitJohhnyGetCurrentPrPermissions,
		).associateBy { it.actionName }

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
