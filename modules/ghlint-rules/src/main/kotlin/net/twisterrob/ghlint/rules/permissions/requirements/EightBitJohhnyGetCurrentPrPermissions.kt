package net.twisterrob.ghlint.rules.permissions.requirements

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import net.twisterrob.ghlint.model.Scope
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rules.permissions.InferRequiredPermissions
import net.twisterrob.ghlint.rules.permissions.RequiredScope
import net.twisterrob.ghlint.rules.permissions.isUsingGitHubToken
import java.net.URI

internal object EightBitJohhnyGetCurrentPrPermissions : InferRequiredPermissions {
	override val actionName: String = "8BitJonny/gh-get-current-pr"
	override val actionUrl: URI = URI.create("https://github.com/8BitJonny/gh-get-current-pr/blob/master/action.yml")

	override fun infer(step: WorkflowStep.Uses): Set<RequiredScope> {
		if (!step.with.isUsingGitHubToken("repo-token")) {
			return emptySet()
		}
		return setOf(
			RequiredScope(
				Scope(Permission.PULL_REQUESTS, Access.READ),
				"To get the current PR.",
			)
		)
	}
}
