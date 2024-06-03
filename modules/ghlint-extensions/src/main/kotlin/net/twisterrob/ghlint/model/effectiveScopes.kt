package net.twisterrob.ghlint.model

public val Permissions.asEffectiveScopes: Set<Scope>
	get() {
		val scopes = mutableSetOf(
			Scope(Permission.ACTIONS, actions),
			Scope(Permission.ATTESTATIONS, attestations),
			Scope(Permission.CHECKS, checks),
			Scope(Permission.CONTENTS, contents),
			Scope(Permission.DEPLOYMENTS, deployments),
			Scope(Permission.ID_TOKEN, idToken),
			Scope(Permission.ISSUES, issues),
			Scope(Permission.METADATA, metadata),
			Scope(Permission.PACKAGES, packages),
			Scope(Permission.PAGES, pages),
			Scope(Permission.PULL_REQUESTS, pullRequests),
			Scope(Permission.REPOSITORY_PROJECTS, repositoryProjects),
			Scope(Permission.SECURITY_EVENTS, securityEvents),
			Scope(Permission.STATUSES, statuses)
		)

		val effectiveScopes = mutableSetOf<Scope>()

		for (scope in scopes) {
			if (scope.access == Access.WRITE) {
				effectiveScopes.add(Scope(scope.permission, Access.READ))
			}
		}

		return scopes.plus(effectiveScopes)
	}
