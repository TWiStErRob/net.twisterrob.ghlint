package net.twisterrob.ghlint.yaml

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.InvalidContent
import net.twisterrob.ghlint.model.SnakeComponentFactory
import org.snakeyaml.engine.v2.nodes.Node

public class SnakeSyntaxErrorContent internal constructor(
	override val parent: File,
	override val error: Throwable,
) : InvalidContent

public class SnakeErrorContent internal constructor(
	override val parent: File,
	override val error: Throwable,
	public val node: Node,
	public val inferredType: SnakeComponentFactory.FileType,
) : InvalidContent

public class SnakeUnknownContent internal constructor(
	override val parent: File,
	override val error: Throwable,
	public val node: Node,
	public val inferredType: SnakeComponentFactory.FileType,
) : InvalidContent
