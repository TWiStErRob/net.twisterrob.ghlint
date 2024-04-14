package net.twisterrob.ghlint.model

import org.snakeyaml.engine.v2.nodes.Node

public class SnakeSyntaxErrorContent internal constructor(
	override val parent: File,
	override val error: Throwable,
) : InvalidContent

public class SnakeErrorContent internal constructor(
	override val parent: File,
	override val error: Throwable,
	public val node: Node,
) : InvalidContent

public class SnakeUnknownContent internal constructor(
	override val parent: File,
	override val error: Throwable,
	public val node: Node,
) : InvalidContent
