package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import net.twisterrob.ghlint.model.Scope
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.model.asEffectivePermissionsSet
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
		val definedPermissions = step.parent.permissions?.asEffectivePermissionsSet()
				?: step.parent.parent.permissions?.asEffectivePermissionsSet()
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
					to be written
				""".trimIndent(),
				compliant = emptyList(),
				nonCompliant = emptyList(),
		)
	}
}
