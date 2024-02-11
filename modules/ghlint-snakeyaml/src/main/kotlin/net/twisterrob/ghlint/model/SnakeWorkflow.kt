package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.Yaml
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.text
import net.twisterrob.ghlint.yaml.toTextMap
import org.snakeyaml.engine.v2.nodes.MappingNode

public class SnakeWorkflow internal constructor(
	override val parent: File,
	override val node: MappingNode,
) : Workflow, HasSnakeNode {

	override val location: Location
		get() = super.location

	override val name: String?
		get() = node.getOptionalText("name")

	override val env: Map<String, String>?
		get() = node.getOptional("env")?.run { map.toTextMap() }

	override val jobs: Map<String, Job>
		get() = node.getRequired("jobs").map
			.mapKeys { (key, _) -> key.text }
			.mapValues { (key, node) -> SnakeJob.from(this, key, node as MappingNode) }

	override val permissions: Map<String, String>?
		get() = node.getOptional("permissions")?.run { map.toTextMap() }

	override val defaults: Defaults?
		get() = node.getOptional("defaults")?.let { SnakeDefaults(it as MappingNode) }

	public companion object {

		public fun from(file: File): Workflow =
			SnakeWorkflow(file, Yaml.load(file.content) as MappingNode)
	}
}
