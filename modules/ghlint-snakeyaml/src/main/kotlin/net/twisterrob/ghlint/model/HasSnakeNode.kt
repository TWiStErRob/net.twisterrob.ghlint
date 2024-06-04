package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.nodes.Node

internal interface HasSnakeNode<N : Node> {

	/**
	 * The node that represents this object in the YAML document.
	 *
	 * This is used to drill down and extract more information from the document.
	 */
	val node: N

	/**
	 * The node that represents the location of this object in reporting.
	 *
	 * This might be the key, or parent of [node].
	 */
	val target: Node
}
