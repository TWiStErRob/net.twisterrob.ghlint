package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.nodes.Node

public class SnakeFile internal constructor(
	private val factory: SnakeComponentFactory,
	override val origin: RawFile,
) : File {

	public val node: Node by lazy {
		factory.loadYaml(origin)
	}

	/**
	 * Resolves circular dependency between [File.content] and [Content.parent].
	 *
	 * More specifically [SnakeComponentFactory.createContent] needs a [File] instance.
	 * And [SnakeFile] constructor would need a [Content] instance if it wasn't [lazy].
	 */
	@Suppress("detekt.LabeledExpression")
	override val content: Content by lazy {
		val node = try {
			this.node
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			return@lazy SnakeSyntaxErrorContent(
				parent = this,
				error = ex
			)
		}
		factory.createContent(this, node)
	}
}
