package net.twisterrob.ghlint.model

public val Permissions.asEffectiveScopes: Set<Scope>
	get() {
		return mutableSetOf(
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
		).let { scopes ->
			scopes + scopes
					.filter { it.access == Access.WRITE }
					.map { Scope(it.permission, Access.READ) }
		}
	}

public val Job.effectiveScopes: Set<Scope>?
	get() = permissions?.asEffectiveScopes

public val Workflow.effectiveScopes: Set<Scope>?
	get() = permissions?.asEffectiveScopes
