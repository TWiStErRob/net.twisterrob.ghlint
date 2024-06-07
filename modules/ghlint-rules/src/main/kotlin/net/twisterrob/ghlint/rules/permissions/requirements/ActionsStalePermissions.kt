package net.twisterrob.ghlint.rules.permissions.requirements

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import net.twisterrob.ghlint.model.Scope
import net.twisterrob.ghlint.model.WorkflowStep
import net.twisterrob.ghlint.rules.permissions.InferRequiredPermissions
import net.twisterrob.ghlint.rules.permissions.RequiredScope
import net.twisterrob.ghlint.rules.permissions.isUsingGitHubToken
import java.net.URI

internal object ActionsStalePermissions : InferRequiredPermissions {
	override val actionName: String = "actions/stale"
	override val actionUrl: URI = URI.create("https://github.com/actions/stale/blob/main/action.yml")

	override fun infer(step: WorkflowStep.Uses): Set<RequiredScope> {
		if (!step.with.isUsingGitHubToken("repo-token")) {
			return emptySet()
		}
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
		return setOf(issues, prs) + listOfNotNull(deleteBranch)
	}
}
