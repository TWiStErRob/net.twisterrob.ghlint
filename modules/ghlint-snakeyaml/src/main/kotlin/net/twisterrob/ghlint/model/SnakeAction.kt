package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.Yaml
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.getRequiredKey
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.text
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeAction internal constructor(
	override val parent: File,
	override val node: MappingNode,
	override val target: Node,
) : Action, HasSnakeNode {

	override val location: Location
		get() = super.location

	override val name: String
		get() = node.getRequiredText("name")

	override val description: String
		get() = node.getRequiredText("description")

	override val inputs: Map<String, Action.ActionInput>
		get() = node.getRequired("jobs").map
			.map { (key, node) ->
				val id = key.text
				val job = SnakeActionInput.from(
					id = id,
					node = node as MappingNode,
					target = key,
				)
				id to job
			}
			.toMap()

	override val runs: Action.Runs
		get() = TODO()

	public companion object {

		public fun from(file: File): Action {
			val node = Yaml.load(file.content) as MappingNode
			return SnakeAction(file, node, node.getRequiredKey("name"))
		}
	}
}
