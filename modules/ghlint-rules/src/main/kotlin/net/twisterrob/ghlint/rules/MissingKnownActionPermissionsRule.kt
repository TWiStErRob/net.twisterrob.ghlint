package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import net.twisterrob.ghlint.model.WorkflowStep
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
		val definedPermissions = step.parent.permissions ?: emptySet()

		val remaining = expectedPermissions.minus(definedPermissions)

		if (remaining.isEmpty()) {
			// All permissions are satisified
			return
		}

		// Need to check for permissions with higher access levels, e.g. `write` is more permissive than `read`.
		remaining.forEach { expected ->
			val defined = definedPermissions.find { it.name == expected.name }
			if (defined == null || defined.access < expected.access) {
				reporting.report(MissingRequiredActionPermissions, step) {
					"${it} requires ${expected.access} permission for ${step.uses.action} to work."
				}
			}
		}
	}

	private companion object {
		val KnownActionPermissions: Map<String, Set<Permission>> = mapOf(
				"actions/checkout" to setOf(Permission.Contents(Access.READ)),
		)

		val MissingRequiredActionPermissions = Issue(
				id = "MissingRequiredActionPermissions",
				title = "Required permissions are not declared for action.",
				description = """
				to be written
			""".trimIndent(),
				compliant = emptyList(),
				nonCompliant = emptyList(),
		)
	}
}
