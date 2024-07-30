package net.twisterrob.ghlint.model

public val Job.effectivePermissions: Permissions?
	get() = permissions ?: parent.permissions
