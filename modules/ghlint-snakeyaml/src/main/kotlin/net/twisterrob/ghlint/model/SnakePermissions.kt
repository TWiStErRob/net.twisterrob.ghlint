package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptionalText
import org.snakeyaml.engine.v2.nodes.MappingNode

public class SnakePermissions(public val node: MappingNode) : Permissions {
	override fun asMap(): Map<String, String> {
		TODO("Not yet implemented")
	}

	override val actions: Access
		get() = node.getOptionalText("actions")?.let { Access.fromString(it) } ?: Access.NONE

	override val attestations: Access
		get() = node.getOptionalText("attestations")?.let { Access.fromString(it) } ?: Access.NONE

	override val checks: Access
		get() = node.getOptionalText("checks")?.let { Access.fromString(it) } ?: Access.NONE

	override val contents: Access
		get() = node.getOptionalText("contents")?.let { Access.fromString(it) } ?: Access.NONE

	override val deployments: Access
		get() = node.getOptionalText("deployments")?.let { Access.fromString(it) } ?: Access.NONE

	override val idToken: Access
		get() = node.getOptionalText("id-token")?.let { Access.fromString(it) } ?: Access.NONE

	override val issues: Access
		get() = node.getOptionalText("issues")?.let { Access.fromString(it) } ?: Access.NONE

	override val metadata: Access
		get() = node.getOptionalText("metadata")?.let { Access.fromString(it) } ?: Access.NONE

	override val packages: Access
		get() = node.getOptionalText("packages")?.let { Access.fromString(it) } ?: Access.NONE

	override val pages: Access
		get() = node.getOptionalText("pages")?.let { Access.fromString(it) } ?: Access.NONE

	override val pullRequests: Access
		get() = node.getOptionalText("pull-requests")?.let { Access.fromString(it) } ?: Access.NONE

	override val repositoryProjects: Access
		get() = node.getOptionalText("repository-projects")?.let { Access.fromString(it) } ?: Access.NONE

	override val securityEvents: Access
		get() = node.getOptionalText("security-events")?.let { Access.fromString(it) } ?: Access.NONE

	override val statuses: Access
		get() = node.getOptionalText("statuses")?.let { Access.fromString(it) } ?: Access.NONE
}
