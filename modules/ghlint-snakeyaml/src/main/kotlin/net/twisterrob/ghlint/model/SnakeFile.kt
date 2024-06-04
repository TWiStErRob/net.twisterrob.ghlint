package net.twisterrob.ghlint.model

public class SnakeFile internal constructor(
	private val factory: SnakeComponentFactory,
	override val origin: RawFile,
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
