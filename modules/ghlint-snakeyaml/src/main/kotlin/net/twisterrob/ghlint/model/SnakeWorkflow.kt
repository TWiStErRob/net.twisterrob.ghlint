package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.map
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeWorkflow internal constructor(
	override val factory: SnakeComponentFactory,
	override val parent: File,
	override val node: MappingNode,
	override val target: Node,
) : Workflow, HasSnakeNode<MappingNode>, SnakeElement {

	override val name: String?
		get() = node.getOptionalText("name")

	override val env: Env?
		get() = node.getOptional("env")?.let { factory.createEnv(it) }

	override val jobs: Map<String, Job>
		get() = node.getRequired("jobs").map
			.map { (key, node) ->
				factory.createJob(
					workflow = this,
					key = key,
					node = node,
				)
			}
			.associateBy { it.id }

	override val permissions: Permissions?
		get() = node.getOptional("permissions")?.let { factory.createPermissions(it) }

	override val defaults: Defaults?
		get() = node.getOptional("defaults")?.let { factory.createDefaults(it) }
}
