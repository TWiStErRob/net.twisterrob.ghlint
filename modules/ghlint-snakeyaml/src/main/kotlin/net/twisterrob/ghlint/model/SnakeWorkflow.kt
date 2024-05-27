package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.toTextMap
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeWorkflow internal constructor(
	private val factory: SnakeComponentFactory,
	override val parent: File,
	override val node: MappingNode,
	override val target: Node,
) : Workflow, HasSnakeNode<MappingNode> {

	override val location: Location
		get() = super.location

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

	override val permissions: Map<String, String>?
		get() = node.getOptional("permissions")?.run { map.toTextMap() }

	override val defaults: Defaults?
		get() = node.getOptional("defaults")?.let { factory.createDefaults(it) }
}
