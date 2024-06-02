package net.twisterrob.ghlint.model

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
	override fun toString(): String = "`${permission.value}: ${access.value}`"
}
