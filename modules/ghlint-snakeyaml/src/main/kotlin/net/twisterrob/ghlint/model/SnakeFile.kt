package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.ErrorInvalidContent
import org.snakeyaml.engine.v2.nodes.MappingNode

public class SnakeFile internal constructor(
	private val rawFile: RawFile,
	factory: SnakeComponentFactory,
) : File {

	override val location: FileLocation = rawFile.location

	@Suppress("detekt.LabeledExpression")
	override val content: Content by lazy {
		val node = try {
			factory.loadYaml(rawFile)
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			return@lazy ErrorInvalidContent(this, rawFile.content, ex)
		}
		if (node !is MappingNode) {
			val error = IllegalArgumentException("Root node is not a mapping: ${node::class.java.simpleName}.")
			return@lazy ErrorInvalidContent(this, rawFile.content, error)
		}
		try {
			return@lazy factory.createContent(this, node)
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			return@lazy ErrorInvalidContent(this, rawFile.content, ex)
		}
	}
}
