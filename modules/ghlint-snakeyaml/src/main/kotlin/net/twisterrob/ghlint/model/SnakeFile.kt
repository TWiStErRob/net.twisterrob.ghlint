package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.SnakeSyntaxErrorContent

public class SnakeFile internal constructor(
	override val origin: RawFile,
	factory: SnakeComponentFactory,
) : File {

	@Suppress("detekt.LabeledExpression")
	override val content: Content by lazy {
		val node = try {
			factory.loadYaml(origin)
		} catch (@Suppress("detekt.TooGenericExceptionCaught") ex: Exception) {
			return@lazy SnakeSyntaxErrorContent(
				parent = this,
				error = ex
			)
		}
		factory.createContent(this, node)
	}
}
