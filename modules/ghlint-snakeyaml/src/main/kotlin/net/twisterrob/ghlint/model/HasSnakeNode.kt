package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.nodes.Node

internal interface HasSnakeNode {

	/**
	 * The node that represents this object in the YAML document.
	 *
	 * This is used to drill down and extract more information from the document.
	 */
	val node: Node
}
