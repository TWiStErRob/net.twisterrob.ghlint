package net.twisterrob.ghlint.model

public sealed interface Scope {
	public val name: String
	public val access: Access
}

public enum class Access: Comparable<Access> {
	READ, WRITE;

	public companion object {
		public fun fromString(value: String): Access? = when (value) {
			"read" -> READ
			"write" -> WRITE
			else -> null
		}
	}
}

// https://docs.github.com/en/actions/security-guides/automatic-token-authentication#permissions-for-the-github_token
public sealed class Permission(public override val access: Access) : Scope {
	public companion object {
		@Suppress("detekt.CyclomaticComplexMethod")
		public fun fromString(value: String, access: Access) : Permission? {
			return when (value) {
				"actions" -> Actions(access)
				"attestations" -> Attestations(access)
				"checks" -> Checks(access)
				"contents" -> Contents(access)
				"deployments" -> Deployments(access)
				"id-token" -> IdToken(access)
				"issues" -> Issues(access)
				"metadata" -> Metadata(access)
				"packages" -> Packages(access)
				"pages" -> Pages(access)
				"pull-requests" -> PullRequests(access)
				"repository-projects" -> RepositoryProjects(access)
				"security-events" -> SecurityEvents(access)
				"statuses" -> Statuses(access)
				else -> null
			}
		}
	}
	public data class Actions(override val access: Access) : Permission(access) {
		public override val name: String = "actions"
	}

	public data class Attestations(override val access: Access) : Permission(access) {
		public override val name: String = "attestations"
	}

	public data class Checks(override val access: Access) : Permission(access) {
		public override val name: String = "checks"
	}

	public data class Contents(override val access: Access) : Permission(access) {
		public override val name: String = "contents"
	}

	public data class Deployments(override val access: Access) : Permission(access) {
		public override val name: String = "deployments"
	}

	public data class IdToken(override val access: Access) : Permission(access) {
		public override val name: String = "id-token"
	}

	public data class Issues(override val access: Access) : Permission(access) {
		public override val name: String = "issues"
	}

	public data class Metadata(override val access: Access) : Permission(access) {
		public override val name: String = "metadata"
	}

	public data class Packages(override val access: Access) : Permission(access) {
		public override val name: String = "packages"
	}

	public data class Pages(override val access: Access) : Permission(access) {
		public override val name: String = "pages"
	}

	public data class PullRequests(override val access: Access) : Permission(access) {
		public override val name: String = "pull-requests"
	}

	public data class RepositoryProjects(override val access: Access) : Permission(access) {
		public override val name: String = "repository-projects"
	}

	public data class SecurityEvents(override val access: Access) : Permission(access) {
		public override val name: String = "security-events"
	}

	public data class Statuses(override val access: Access) : Permission(access) {
		public override val name: String = "statuses"
	}
}
