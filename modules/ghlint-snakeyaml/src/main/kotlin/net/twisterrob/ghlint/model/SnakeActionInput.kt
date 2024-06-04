package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequiredText
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeActionInput internal constructor(
	override val factory: SnakeComponentFactory,
	override val parent: Action,
	override val id: String,
	override val node: MappingNode,
	override val target: Node,
) : Action.ActionInput, HasSnakeNode<MappingNode> {

	override val description: String
		get() = node.getRequiredText("description")

	override val required: Boolean
		get() = node.getOptionalText("required")?.toBooleanStrict() == true

	override val default: String?
		get() = node.getOptionalText("default")

	override val deprecationMessage: String?
		get() = node.getOptionalText("deprecationMessage")
}
