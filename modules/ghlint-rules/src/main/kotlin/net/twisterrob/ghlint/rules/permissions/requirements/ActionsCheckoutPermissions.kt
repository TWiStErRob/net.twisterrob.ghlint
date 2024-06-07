package net.twisterrob.ghlint.rules.permissions.requirements

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import net.twisterrob.ghlint.model.Scope
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rules.permissions.InferRequiredPermissions
import net.twisterrob.ghlint.rules.permissions.RequiredScope
import net.twisterrob.ghlint.rules.permissions.isUsingGitHubToken
import java.net.URI

internal object ActionsCheckoutPermissions : InferRequiredPermissions {
	override val actionName: String = "actions/checkout"
	override val actionUrl: URI = URI.create("https://github.com/actions/checkout/blob/main/action.yml")

	override fun infer(step: WorkflowStep.Uses): Set<RequiredScope> {
		if (!step.with.isUsingGitHubToken("token")) {
			return emptySet()
		}

		return setOf(
			RequiredScope(
				Scope(Permission.CONTENTS, Access.READ),
				"To read the repository contents during git clone/fetch.",
			)
		)
	}
}
