package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import net.twisterrob.ghlint.model.Scope
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.model.effectiveScopes
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class MissingKnownActionPermissionsRule : VisitorRule, WorkflowVisitor {
	override val issues: List<Issue> = listOf(MissingRequiredActionPermissions)

	override fun visitWorkflowUsesStep(reporting: Reporting, step: WorkflowStep.Uses) {
		super.visitWorkflowUsesStep(reporting, step)

		val expectedPermissions = KnownActionPermissions[step.uses.action] ?: return
		val definedPermissions = step.parent.effectiveScopes
				?: step.parent.parent.effectiveScopes
				?: return

		val remaining = expectedPermissions.minus(definedPermissions)

		remaining.forEach { expected ->
			reporting.report(MissingRequiredActionPermissions, step) {
				"${it} requires ${expected} permission for ${step.uses.action} to work."
			}
		}
	}

	private companion object {
		val KnownActionPermissions: Map<String, Set<Scope>> = mapOf(
				"actions/checkout" to setOf(Scope(Permission.CONTENTS, Access.READ)),
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
