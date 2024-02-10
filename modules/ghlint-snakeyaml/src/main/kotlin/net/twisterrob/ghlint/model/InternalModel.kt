package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.nodes.MappingNode

@Suppress("SEALED_INHERITOR_IN_DIFFERENT_MODULE") // STOPSHIP
internal interface InternalModel : Model {

	val node: MappingNode
}
