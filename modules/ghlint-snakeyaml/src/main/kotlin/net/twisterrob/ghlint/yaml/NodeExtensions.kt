package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.results.ColumnNumber
import net.twisterrob.ghlint.results.LineNumber
import net.twisterrob.ghlint.results.Location
import net.twisterrob.ghlint.results.Position
import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode
import java.util.Optional

// STOPSHIP public?
public fun MappingNode.getOptionalText(key: String): String? =
	this.value.singleOrNull { it.keyNode.text == key }?.valueNode?.text

public fun MappingNode.getOptional(key: String): Node? =
	this.value.singleOrNull { it.keyNode.text == key }?.valueNode

public fun MappingNode.getRequiredText(key: String): String =
	this.value.single { it.keyNode.text == key }.valueNode.text

public fun MappingNode.getRequired(key: String): Node =
	this.value.single { it.keyNode.text == key }.valueNode

public val Node.text: String
	get() = (this as ScalarNode).value

public val Node.array: List<Node>
	get() = (this as SequenceNode).value

public val Node.map: Map<Node, Node>
	get() = when (this) {
		is ScalarNode -> if (this.value.isEmpty()) emptyMap() else error("Not a map")
		is MappingNode -> this.value.associateBy({ it.keyNode }, { it.valueNode })
		else -> error("Not a map")
	}

public fun Map<Node, Node>.toTextMap(): Map<String, String> =
	this.entries.associate { (key, value) -> key.text to value.text }


public fun Node.toLocation(file: File): Location =
	Location(
		file = file.file,
		start = startMark.toPosition(),
		end = endMark.toPosition(),
	)

private fun Optional<Mark>.toPosition(): Position =
	this.get().toPosition()

private fun Mark.toPosition(): Position =
	Position(LineNumber(this.line), ColumnNumber(this.column))
