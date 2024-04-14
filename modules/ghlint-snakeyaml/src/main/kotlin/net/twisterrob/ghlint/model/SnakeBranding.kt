package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptionalText
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeBranding internal constructor(
	override val parent: Action,
	override val node: MappingNode,
	override val target: Node,
) : Action.Branding, HasSnakeNode<MappingNode> {

	override val icon: String?
		get() = node.getOptionalText("icon")

	override val color: String?
		get() = node.getOptionalText("color")
}
