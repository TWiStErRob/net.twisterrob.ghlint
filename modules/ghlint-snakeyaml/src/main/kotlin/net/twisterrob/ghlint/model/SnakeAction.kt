package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeAction internal constructor(
	private val factory: SnakeComponentFactory,
	override val node: MappingNode,
	override val target: Node,
) : Action, HasSnakeNode<MappingNode> {

	@Suppress("LateinitUsage") // Can't figure out a better way.
	override lateinit var parent: File
		internal set

	override val location: Location
		get() = super.location

	override val name: String
		get() = node.getRequiredText("name")

	override val description: String
		get() = node.getRequiredText("description")

	override val inputs: Map<String, Action.ActionInput>
		get() = node.getRequired("inputs").map
			.map { (key, node) ->
				factory.createActionInput(
					action = this,
					key = key,
					node = node,
				)
			}
			.associateBy { it.id }

	override val runs: Action.Runs
		get() = error("Not implemented yet.")
}
