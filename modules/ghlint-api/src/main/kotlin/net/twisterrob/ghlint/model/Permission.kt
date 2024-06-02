package net.twisterrob.ghlint.model

import java.util.Locale

// https://docs.github.com/en/actions/security-guides/automatic-token-authentication#permissions-for-the-github_token
public interface Permissions {
	public val actions: Access
	public val attestations: Access
	public val checks: Access
	public val contents: Access
	public val deployments: Access
	public val idToken: Access
	public val issues: Access
	public val metadata: Access
	public val packages: Access
	public val pages: Access
	public val pullRequests: Access
	public val repositoryProjects: Access
	public val securityEvents: Access
	public val statuses: Access

	public fun asMap(): Map<String, String>
}

public enum class Permission {
	ACTIONS,
	ATTESTATIONS,
	CHECKS,
	CONTENTS,
	DEPLOYMENTS,
	ID_TOKEN,
	ISSUES,
	METADATA,
	PACKAGES,
	PAGES,
	PULL_REQUESTS,
	REPOSITORY_PROJECTS,
	SECURITY_EVENTS,
	STATUSES
}

public enum class Access : Comparable<Access> {
	// Order determines restrictiveness of permission
	NONE, READ, WRITE;

	public companion object {
		public fun fromString(value: String): Access = when (value) {
			"read" -> READ
			"write" -> WRITE
			else -> NONE
		}
	}
}

public data class Scope(val permission: Permission, val access: Access) {
	override fun toString(): String {
		return "`${permission.name.lowercase(Locale.getDefault())}: ${access.name.lowercase(Locale.getDefault())}`"
	}
}

public fun Permissions.asEffectivePermissionsSet(): Set<Scope> {
	val scopes = mutableSetOf<Scope>()
	scopes += Scope(Permission.ACTIONS, actions)
	scopes += Scope(Permission.ATTESTATIONS, attestations)
	scopes += Scope(Permission.CHECKS, checks)
	scopes += Scope(Permission.CONTENTS, contents)
	scopes += Scope(Permission.DEPLOYMENTS, deployments)
	scopes += Scope(Permission.ID_TOKEN, idToken)
	scopes += Scope(Permission.ISSUES, issues)
	scopes += Scope(Permission.METADATA, metadata)
	scopes += Scope(Permission.PACKAGES, packages)
	scopes += Scope(Permission.PAGES, pages)
	scopes += Scope(Permission.PULL_REQUESTS, pullRequests)
	scopes += Scope(Permission.REPOSITORY_PROJECTS, repositoryProjects)
	scopes += Scope(Permission.SECURITY_EVENTS, securityEvents)
	scopes += Scope(Permission.STATUSES, statuses)

	val effectiveScopes = mutableSetOf<Scope>()

	for (scope in scopes) {
		if (scope.access == Access.WRITE) {
			effectiveScopes.add(Scope(scope.permission, Access.READ))
		}
	}

	return scopes.plus(effectiveScopes)
}
