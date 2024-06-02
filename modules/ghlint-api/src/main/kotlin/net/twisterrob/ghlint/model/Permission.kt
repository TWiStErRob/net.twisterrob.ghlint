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

public enum class Permission(public val value: String) {
	ACTIONS("actions"),
	ATTESTATIONS("attestations"),
	CHECKS("checks"),
	CONTENTS("contents"),
	DEPLOYMENTS("deployments"),
	ID_TOKEN("id-token"),
	ISSUES("issues"),
	METADATA("metadata"),
	PACKAGES("packages"),
	PAGES("pages"),
	PULL_REQUESTS("pull-requests"),
	REPOSITORY_PROJECTS("repository-projects"),
	SECURITY_EVENTS("security-events"),
	STATUSES("statuses")
}

public enum class Access(public val value: String) : Comparable<Access> {
	// Order determines restrictiveness of permission
	NONE("none"),
	READ("read"),
	WRITE("write");

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
		return "`${permission.value}: ${access.value}`"
	}
}
