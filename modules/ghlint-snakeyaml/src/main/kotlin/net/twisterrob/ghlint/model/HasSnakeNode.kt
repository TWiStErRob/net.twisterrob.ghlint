package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.nodes.Node

internal interface HasSnakeNode<N : Node> {

	/**
	 * The node that represents this object in the YAML document.
	 *
	 * This is used in implementations to drill down and extract more information from the document.
	 */
	val node: N
}
