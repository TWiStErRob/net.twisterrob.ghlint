package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequiredText
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeActionInput internal constructor(
	override val id: String,
	override val node: MappingNode,
	override val target: Node,
) : Action.ActionInput, HasSnakeNode {

	override val description: String
		get() = node.getRequiredText("description")

	override val required: Boolean
		get() = node.getOptionalText("required")?.toBooleanStrict() ?: false

	override val default: String?
		get() = node.getOptionalText("default")

	public companion object {

		public fun from(id: String, node: MappingNode, target: Node): Action.ActionInput =
			SnakeActionInput(id, node, target)
	}
}
