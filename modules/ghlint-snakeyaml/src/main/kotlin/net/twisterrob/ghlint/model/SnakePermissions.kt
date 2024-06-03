package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.toTextMap
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakePermissions internal constructor(
		override val node: MappingNode,
		override val target: Node,
) : Permissions, HasSnakeNode<MappingNode> {
	override val actions: Access get() = node.access(Permission.ACTIONS)

	override val attestations: Access get() = node.access(Permission.ATTESTATIONS)

	override val checks: Access get() = node.access(Permission.CHECKS)

	override val contents: Access get() = node.access(Permission.CONTENTS)

	override val deployments: Access get() = node.access(Permission.DEPLOYMENTS)

	override val idToken: Access get() = node.access(Permission.ID_TOKEN)

	override val issues: Access get() = node.access(Permission.ISSUES)

	override val metadata: Access get() = node.access(Permission.METADATA)

	override val packages: Access get() = node.access(Permission.PACKAGES)

	override val pages: Access get() = node.access(Permission.PAGES)

	override val pullRequests: Access get() = node.access(Permission.PULL_REQUESTS)

	override val repositoryProjects: Access get() = node.access(Permission.REPOSITORY_PROJECTS)

	override val securityEvents: Access get() = node.access(Permission.SECURITY_EVENTS)

	override val statuses: Access get() = node.access(Permission.STATUSES)

	override val map: Map<String, String>
		get() = node.map.toTextMap()
}

private fun MappingNode.access(actions: Permission): Access =
		getOptionalText(actions.value)?.let { Access.fromString(it) } ?: Access.NONE
