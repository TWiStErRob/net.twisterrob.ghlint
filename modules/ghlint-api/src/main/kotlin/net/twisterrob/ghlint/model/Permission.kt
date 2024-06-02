package net.twisterrob.ghlint.model

public sealed interface Scope {
	public val name: String
	public val access: Access
}

public enum class Access: Comparable<Access> {
	NONE, READ, WRITE;

	public companion object {
		public fun fromString(value: String): Access = when (value) {
			"read" -> READ
			"write" -> WRITE
			else -> NONE
		}
	}
}

// https://docs.github.com/en/actions/security-guides/automatic-token-authentication#permissions-for-the-github_token
public sealed class Permission(public override val access: Access) : Scope {
	public companion object {
		@Suppress("detekt.CyclomaticComplexMethod")
		public fun fromString(value: String, access: Access) : Permission {
			return when (value) {
				Actions.Name -> Actions(access)
				Attestations.Name -> Attestations(access)
				Checks.Name -> Checks(access)
				Contents.Name -> Contents(access)
				Deployments.Name -> Deployments(access)
				IdToken.Name -> IdToken(access)
				Issues.Name -> Issues(access)
				Metadata.Name -> Metadata(access)
				Packages.Name -> Packages(access)
				Pages.Name -> Pages(access)
				PullRequests.Name -> PullRequests(access)
				RepositoryProjects.Name -> RepositoryProjects(access)
				SecurityEvents.Name -> SecurityEvents(access)
				Statuses.Name -> Statuses(access)
				else -> Undefined(access)
			}
		}
	}
	public data class Actions(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "actions"
		}

		public override val name: String = Name
	}

	public data class Attestations(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "attestations"
		}

		public override val name: String = Name
	}

	public data class Checks(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "checks"
		}

		public override val name: String = Name
	}

	public data class Contents(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "contents"
		}

		public override val name: String = Name
	}

	public data class Deployments(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "deployments"
		}

		public override val name: String = Name
	}

	public data class IdToken(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "id-token"
		}

		public override val name: String = Name
	}

	public data class Issues(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "issues"
		}

		public override val name: String = Name
	}

	public data class Metadata(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "metadata"
		}

		public override val name: String = Name
	}

	public data class Packages(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "packages"
		}

		public override val name: String = Name
	}

	public data class Pages(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "pages"
		}

		public override val name: String = Name
	}

	public data class PullRequests(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "pull-requests"
		}

		public override val name: String = Name
	}

	public data class RepositoryProjects(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "repository-projects"
		}

		public override val name: String = Name
	}

	public data class SecurityEvents(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "security-events"
		}

		public override val name: String = Name
	}

	public data class Statuses(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "statuses"
		}

		public override val name: String = Name
	}

	public data class Undefined(override val access: Access) : Permission(access) {
		public companion object {
			public const val Name: String = "undefined"
		}

		public override val name: String = Name
	}
}
