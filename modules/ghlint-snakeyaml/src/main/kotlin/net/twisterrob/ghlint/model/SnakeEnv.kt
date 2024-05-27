package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.ScalarNode

public class SnakeEnvExplicit internal constructor(
	override val node: MappingNode,
	override val target: Node,
	override val map: Map<String, String>,
) : Env.Explicit, HasSnakeNode<MappingNode>

public class SnakeEnvDynamic internal constructor(
	override val node: ScalarNode,
	override val target: Node,
	override val text: String,
) : Env.Dynamic, HasSnakeNode<ScalarNode>
